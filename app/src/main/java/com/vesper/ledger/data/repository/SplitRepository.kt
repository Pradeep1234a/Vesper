package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.AccountDao
import com.vesper.ledger.data.local.SplitDao
import com.vesper.ledger.data.local.TransactionDao
import com.vesper.ledger.data.model.*
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
    fun getSharesForExpense(expenseId: Long): Flow<List<SplitExpenseShare>> = splitDao.getSharesForExpense(expenseId)
    fun getSettlementsForGroup(groupId: Long): Flow<List<SplitSettlement>> = splitDao.getSettlementsForGroup(groupId)
    fun getAllSettlements(): Flow<List<SplitSettlement>> = splitDao.getAllSettlements()

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

    // ── Add & Delete Split Expenses with Account Reversal Sync ──
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
                isPaid = member.id == paidByMemberId
            )
        }
        splitDao.insertShares(shareList)

        // If paid upfront by current user and an account is selected, sync with main transactions
        if (isPaidByCurrentUser && accountId != 0L) {
            val mainTx = Transaction(
                title = "Split: $title",
                amount = totalAmount,
                type = TransactionType.EXPENSE,
                categoryId = 5,
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

    suspend fun deleteSplitExpense(expenseId: Long) {
        val expense = splitDao.getExpenseById(expenseId)
        if (expense != null) {
            // If paid by current user and linked account exists, restore account balance
            if (expense.paidByMemberName.equals("Me", ignoreCase = true) && expense.accountId != 0L) {
                val account = accountDao.getAccountById(expense.accountId)
                if (account != null) {
                    accountDao.updateAccount(account.copy(initialBalance = account.initialBalance + expense.totalAmount))
                }
            }
            splitDao.deleteSharesForExpense(expenseId)
            splitDao.deleteExpenseById(expenseId)
        }
    }

    suspend fun toggleSharePaymentStatus(shareId: Long, isPaid: Boolean) {
        splitDao.updateSharePaymentStatus(shareId, isPaid)
    }

    // ── Settle Up Workflow ──
    suspend fun recordSettlement(
        groupId: Long,
        debtorId: Long,
        debtorName: String,
        creditorId: Long,
        creditorName: String,
        amount: Double,
        paymentMethod: String,
        accountId: Long,
        isDebtorCurrentUser: Boolean,
        isCreditorCurrentUser: Boolean
    ): Long {
        val settlement = SplitSettlement(
            groupId = groupId,
            debtorId = debtorId,
            debtorName = debtorName,
            creditorId = creditorId,
            creditorName = creditorName,
            amount = amount,
            paymentMethod = paymentMethod,
            accountId = accountId,
            dateEpochMillis = System.currentTimeMillis()
        )

        val settlementId = splitDao.insertSettlement(settlement)

        // Main ledger sync
        if (accountId != 0L) {
            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                if (isCreditorCurrentUser) {
                    // You received settlement money from debtor -> INCOME
                    val mainTx = Transaction(
                        title = "Settlement Received from $debtorName",
                        amount = amount,
                        type = TransactionType.INCOME,
                        categoryId = 4, // Other Income
                        dateEpochMillis = System.currentTimeMillis(),
                        note = "Group settlement received via $paymentMethod",
                        accountName = account.name,
                        paymentMethod = paymentMethod,
                        accountId = accountId
                    )
                    transactionDao.insertTransaction(mainTx)
                    accountDao.updateAccount(account.copy(initialBalance = account.initialBalance + amount))
                } else if (isDebtorCurrentUser) {
                    // You paid settlement money to creditor -> EXPENSE
                    val mainTx = Transaction(
                        title = "Settlement Paid to $creditorName",
                        amount = amount,
                        type = TransactionType.EXPENSE,
                        categoryId = 12, // Other Expense
                        dateEpochMillis = System.currentTimeMillis(),
                        note = "Group settlement paid via $paymentMethod",
                        accountName = account.name,
                        paymentMethod = paymentMethod,
                        accountId = accountId
                    )
                    transactionDao.insertTransaction(mainTx)
                    accountDao.updateAccount(account.copy(initialBalance = account.initialBalance - amount))
                }
            }
        }

        return settlementId
    }

    // ── Debt Calculation Engine (With Settlements Included) ──
    fun getGroupDebtSettlements(groupId: Long): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getMembersForGroup(groupId),
            splitDao.getExpensesForGroup(groupId),
            splitDao.getAllShares(),
            splitDao.getSettlementsForGroup(groupId)
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, allShares: List<SplitExpenseShare>, settlements: List<SplitSettlement> ->
            calculateDebts(groupId, members, expenses, allShares, settlements)
        }
    }

    fun getOverallDebtSettlements(): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getAllMembers(),
            splitDao.getAllExpenses(),
            splitDao.getAllShares(),
            splitDao.getAllSettlements()
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, allShares: List<SplitExpenseShare>, settlements: List<SplitSettlement> ->
            calculateDebts(0L, members, expenses, allShares, settlements)
        }
    }

    private fun calculateDebts(
        groupId: Long,
        members: List<SplitMember>,
        expenses: List<SplitExpense>,
        allShares: List<SplitExpenseShare>,
        settlements: List<SplitSettlement>
    ): List<DebtSettlement> {
        if (members.isEmpty() || (expenses.isEmpty() && settlements.isEmpty())) return emptyList()

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

        // Incorporate settlements (Debtor paid Creditor)
        settlements.forEach { st ->
            // Debtor balance increases (less negative/debt reduced)
            netBalances[st.debtorId] = (netBalances[st.debtorId] ?: 0.0) + st.amount
            // Creditor balance decreases (receivable reduced)
            netBalances[st.creditorId] = (netBalances[st.creditorId] ?: 0.0) - st.amount
        }

        // Debt Simplification Algorithm (Greedy Matching)
        val debtors = mutableListOf<Pair<Long, Double>>()
        val creditors = mutableListOf<Pair<Long, Double>>()

        netBalances.forEach { (mId, bal) ->
            if (bal < -0.01) debtors.add(mId to abs(bal))
            else if (bal > 0.01) creditors.add(mId to bal)
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val debtList = mutableListOf<DebtSettlement>()
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
                debtList.add(
                    DebtSettlement(
                        groupId = groupId,
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

        return debtList
    }

    // ── Group Analytics Summary ──
    fun getGroupAnalyticsSummary(groupId: Long): Flow<GroupAnalyticsSummary?> {
        val groupFlow: Flow<SplitGroup?> = splitDao.getAllGroups().map { list: List<SplitGroup> -> list.find { it.id == groupId } }
        return combine(
            splitDao.getMembersForGroup(groupId),
            splitDao.getExpensesForGroup(groupId),
            groupFlow
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, group: SplitGroup? ->
            if (group == null) return@combine null

            val totalVolume = expenses.sumOf { it.totalAmount }
            val count = expenses.size

            val catMap = expenses.groupBy { it.categoryName }.mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            val topCategory = catMap.maxByOrNull { it.value }?.key ?: "General"

            val spenderMap = expenses.groupBy { it.paidByMemberName }.mapValues { entry -> entry.value.sumOf { it.totalAmount } }
            val topSpender = spenderMap.maxByOrNull { it.value }
            val topSpenderName = topSpender?.key ?: "None"
            val topSpenderAmount = topSpender?.value ?: 0.0

            val pmMap = expenses.groupBy { it.paymentMethod }.mapValues { entry -> entry.value.sumOf { it.totalAmount } }

            val memberBalances = members.associate { m ->
                val paid = expenses.filter { it.paidByMemberId == m.id }.sumOf { it.totalAmount }
                val consumed = expenses.sumOf { it.totalAmount / (if (members.isNotEmpty()) members.size else 1) }
                m.name to (paid - consumed)
            }

            GroupAnalyticsSummary(
                groupId = groupId,
                groupTitle = group.title,
                totalExpenseVolume = totalVolume,
                totalExpensesCount = count,
                topCategory = topCategory,
                topSpenderName = topSpenderName,
                topSpenderAmount = topSpenderAmount,
                memberBalances = memberBalances,
                categoryBreakdown = catMap,
                paymentMethodBreakdown = pmMap
            )
        }
    }
}
