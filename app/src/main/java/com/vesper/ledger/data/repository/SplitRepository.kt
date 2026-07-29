package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.AccountDao
import com.vesper.ledger.data.local.SplitDao
import com.vesper.ledger.data.local.TransactionDao
import com.vesper.ledger.data.model.DebtSettlement
import com.vesper.ledger.data.model.SplitExpense
import com.vesper.ledger.data.model.SplitExpenseShare
import com.vesper.ledger.data.model.SplitGroup
import com.vesper.ledger.data.model.SplitMember
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.min

class SplitRepository(
    private val splitDao: SplitDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {

    // ── Groups & Members ──
    fun getAllGroups(): Flow<List<SplitGroup>> = splitDao.getAllGroups()
    fun getMembersForGroup(groupId: Long): Flow<List<SplitMember>> = splitDao.getMembersForGroup(groupId)
    fun getExpensesForGroup(groupId: Long): Flow<List<SplitExpense>> = splitDao.getExpensesForGroup(groupId)
    fun getAllExpenses(): Flow<List<SplitExpense>> = splitDao.getAllExpenses()
    fun getAllShares(): Flow<List<SplitExpenseShare>> = splitDao.getAllShares()

    suspend fun createGroup(title: String, category: String, memberNames: List<String>): Long {
        val group = SplitGroup(title = title, category = category)
        val groupId = splitDao.insertGroup(group)

        val members = mutableListOf<SplitMember>()
        // First member is always "Me"
        members.add(SplitMember(groupId = groupId, name = "Me", isCurrentUser = true))
        memberNames.forEach { name ->
            if (name.isNotBlank() && !name.equals("Me", ignoreCase = true)) {
                members.add(SplitMember(groupId = groupId, name = name, isCurrentUser = false))
            }
        }
        splitDao.insertMembers(members)
        return groupId
    }

    suspend fun deleteGroup(groupId: Long) {
        splitDao.deleteGroupById(groupId)
    }

    // ── Add Split Expense with Main Account & Transaction Sync ──
    suspend fun addSplitExpense(
        groupId: Long,
        title: String,
        totalAmount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        paymentMethod: String,
        accountId: Long,
        categoryName: String,
        shares: Map<Long, Double>, // memberId -> shareAmount
        isPaidByCurrentUser: Boolean
    ): Long {
        val expense = SplitExpense(
            groupId = groupId,
            title = title,
            totalAmount = totalAmount,
            paidByMemberId = paidByMemberId,
            paidByMemberName = paidByMemberName,
            paymentMethod = paymentMethod,
            accountId = accountId,
            categoryName = categoryName,
            dateEpochMillis = System.currentTimeMillis()
        )

        val expenseId = splitDao.insertExpense(expense)

        // Insert member shares
        val groupMembers = splitDao.getMembersForGroupSync(groupId)
        val shareList = groupMembers.map { member ->
            val shareAmt = shares[member.id] ?: (totalAmount / groupMembers.size)
            SplitExpenseShare(
                expenseId = expenseId,
                memberId = member.id,
                memberName = member.name,
                shareAmount = shareAmt,
                isPaid = member.id == paidByMemberId // Paid upfront by the payer
            )
        }
        splitDao.insertShares(shareList)

        // If paid upfront by current user and an account is selected, sync with main transactions
        if (isPaidByCurrentUser && accountId != 0L) {
            val mainTx = Transaction(
                title = "Split: $title",
                amount = totalAmount,
                type = TransactionType.EXPENSE,
                categoryId = 5, // Default Food/Groceries or general
                dateEpochMillis = System.currentTimeMillis(),
                note = "Group expense split with ${groupMembers.size - 1} members",
                accountName = paidByMemberName,
                paymentMethod = paymentMethod,
                accountId = accountId
            )
            transactionDao.insertTransaction(mainTx)

            // Update account balance
            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                val updatedBal = account.initialBalance - totalAmount
                accountDao.updateAccount(account.copy(initialBalance = updatedBal))
            }
        }

        return expenseId
    }

    // ── Debt Calculation Engine (Who Owes Whom Algorithm) ──
    fun getGroupDebtSettlements(groupId: Long): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getMembersForGroup(groupId),
            splitDao.getExpensesForGroup(groupId),
            splitDao.getAllShares()
        ) { members, expenses, allShares ->
            calculateDebts(members, expenses, allShares)
        }
    }

    fun getOverallDebtSettlements(): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getAllMembers(),
            splitDao.getAllExpenses(),
            splitDao.getAllShares()
        ) { members, expenses, allShares ->
            calculateDebts(members, expenses, allShares)
        }
    }

    private fun calculateDebts(
        members: List<SplitMember>,
        expenses: List<SplitExpense>,
        allShares: List<SplitExpenseShare>
    ): List<DebtSettlement> {
        if (members.isEmpty() || expenses.isEmpty()) return emptyList()

        val memberMap = members.associateBy { it.id }
        val netBalances = mutableMapOf<Long, Double>()

        // Initialize balances
        members.forEach { netBalances[it.id] = 0.0 }

        // Process expenses (Payer gets +, consumers get -)
        expenses.forEach { exp ->
            netBalances[exp.paidByMemberId] = (netBalances[exp.paidByMemberId] ?: 0.0) + exp.totalAmount

            val expShares = allShares.filter { it.expenseId == exp.id }
            expShares.forEach { share ->
                netBalances[share.memberId] = (netBalances[share.memberId] ?: 0.0) - share.shareAmount
            }
        }

        // Debt Simplification Algorithm (Greedy Matching)
        val debtors = mutableListOf<Pair<Long, Double>>() // (memberId, amountOwedPos)
        val creditors = mutableListOf<Pair<Long, Double>>() // (memberId, amountOwedPos)

        netBalances.forEach { (mId, bal) ->
            if (bal < -0.01) debtors.add(mId to abs(bal))
            else if (bal > 0.01) creditors.add(mId to bal)
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val settlements = mutableListOf<DebtSettlement>()
        var dIdx = 0
        var cIdx = 0

        val dList = debtors.map { it.second }.toMutableList()
        val cList = creditors.map { it.second }.toMutableList()

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtorId = debtors[dIdx].first
            val creditorId = creditors[cIdx].first
            val dMember = memberMap[debtorId]
            val cMember = memberMap[creditorId]

            val settleAmt = min(dList[dIdx], cList[cIdx])

            if (dMember != null && cMember != null && settleAmt > 0.01) {
                settlements.add(
                    DebtSettlement(
                        debtorId = debtorId,
                        debtorName = dMember.name,
                        creditorId = creditorId,
                        creditorName = cMember.name,
                        amount = settleAmt,
                        isDebtorCurrentUser = dMember.isCurrentUser,
                        isCreditorCurrentUser = cMember.isCurrentUser
                    )
                )
            }

            dList[dIdx] -= settleAmt
            cList[cIdx] -= settleAmt

            if (dList[dIdx] < 0.01) dIdx++
            if (cList[cIdx] < 0.01) cIdx++
        }

        return settlements
    }
}
