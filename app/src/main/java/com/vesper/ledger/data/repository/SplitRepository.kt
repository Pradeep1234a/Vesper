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

    // ── Add & Delete Split Expenses (Supports Multi-Payer & Account Sync) ──
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
        multiplePayers: Map<Long, Double> = emptyMap(), // memberId -> amountPaid (for multi-payer)
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

        val groupMembers = splitDao.getMembersForGroupSync(groupId)
        val memberMap = groupMembers.associateBy { it.id }

        // Insert Multi-Payers
        if (multiplePayers.isNotEmpty()) {
            val payerList = multiplePayers.map { (mId, paidAmt) ->
                SplitExpensePayer(
                    expenseId = expenseId,
                    memberId = mId,
                    memberName = memberMap[mId]?.name ?: "Member",
                    amountPaid = paidAmt
                )
            }
            splitDao.insertPayers(payerList)
        } else {
            splitDao.insertPayers(
                listOf(
                    SplitExpensePayer(
                        expenseId = expenseId,
                        memberId = paidByMemberId,
                        memberName = paidByMemberName,
                        amountPaid = totalAmount
                    )
                )
            )
        }

        // Insert member shares
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

        // If current user paid upfront (single or multi-payer portion) and account selected, sync ledger
        val userPaidAmount = if (multiplePayers.isNotEmpty()) {
            val userMember = groupMembers.find { it.isCurrentUser }
            multiplePayers[userMember?.id] ?: 0.0
        } else if (isPaidByCurrentUser) {
            totalAmount
        } else 0.0

        if (userPaidAmount > 0 && accountId != 0L) {
            val mainTx = Transaction(
                title = "Split: $title",
                amount = userPaidAmount,
                type = TransactionType.EXPENSE,
                categoryId = 5,
                dateEpochMillis = System.currentTimeMillis(),
                note = "Group expense split with ${groupMembers.size - 1} members",
                accountName = paidByMemberName,
                paymentMethod = paymentMethod,
                accountId = accountId
            )
            transactionDao.insertTransaction(mainTx)

            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                accountDao.updateAccount(account.copy(initialBalance = account.initialBalance - userPaidAmount))
            }
        }

        return expenseId
    }

    suspend fun deleteSplitExpense(expenseId: Long) {
        val expense = splitDao.getExpenseById(expenseId)
        if (expense != null) {
            if (expense.paidByMemberName.equals("Me", ignoreCase = true) && expense.accountId != 0L) {
                val account = accountDao.getAccountById(expense.accountId)
                if (account != null) {
                    accountDao.updateAccount(account.copy(initialBalance = account.initialBalance + expense.totalAmount))
                }
            }
            splitDao.deletePayersForExpense(expenseId)
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

        if (accountId != 0L) {
            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                if (isCreditorCurrentUser) {
                    val mainTx = Transaction(
                        title = "Settlement Received from $debtorName",
                        amount = amount,
                        type = TransactionType.INCOME,
                        categoryId = 4,
                        dateEpochMillis = System.currentTimeMillis(),
                        note = "Group settlement received via $paymentMethod",
                        accountName = account.name,
                        paymentMethod = paymentMethod,
                        accountId = accountId
                    )
                    transactionDao.insertTransaction(mainTx)
                    accountDao.updateAccount(account.copy(initialBalance = account.initialBalance + amount))
                } else if (isDebtorCurrentUser) {
                    val mainTx = Transaction(
                        title = "Settlement Paid to $creditorName",
                        amount = amount,
                        type = TransactionType.EXPENSE,
                        categoryId = 12,
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

    // ── Debt Calculation Engine (Supports Multi-Payer & Settlements) ──
    fun getGroupDebtSettlements(groupId: Long): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getMembersForGroup(groupId),
            splitDao.getExpensesForGroup(groupId),
            splitDao.getAllShares(),
            splitDao.getAllPayers(),
            splitDao.getSettlementsForGroup(groupId)
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, allShares: List<SplitExpenseShare>, allPayers: List<SplitExpensePayer>, settlements: List<SplitSettlement> ->
            calculateDebts(groupId, members, expenses, allShares, allPayers, settlements)
        }
    }

    fun getOverallDebtSettlements(): Flow<List<DebtSettlement>> {
        return combine(
            splitDao.getAllMembers(),
            splitDao.getAllExpenses(),
            splitDao.getAllShares(),
            splitDao.getAllPayers(),
            splitDao.getAllSettlements()
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, allShares: List<SplitExpenseShare>, allPayers: List<SplitExpensePayer>, settlements: List<SplitSettlement> ->
            calculateDebts(0L, members, expenses, allShares, allPayers, settlements)
        }
    }

    private fun calculateDebts(
        groupId: Long,
        members: List<SplitMember>,
        expenses: List<SplitExpense>,
        allShares: List<SplitExpenseShare>,
        allPayers: List<SplitExpensePayer>,
        settlements: List<SplitSettlement>
    ): List<DebtSettlement> {
        if (members.isEmpty() || (expenses.isEmpty() && settlements.isEmpty())) return emptyList()

        val memberMap = members.associateBy { it.id }
        val netBalances = mutableMapOf<Long, Double>()

        members.forEach { netBalances[it.id] = 0.0 }

        // Process multi-payer upfront payments & consumed shares
        expenses.forEach { exp ->
            val expPayers = allPayers.filter { it.expenseId == exp.id }
            if (expPayers.isNotEmpty()) {
                expPayers.forEach { payer ->
                    netBalances[payer.memberId] = (netBalances[payer.memberId] ?: 0.0) + payer.amountPaid
                }
            } else {
                netBalances[exp.paidByMemberId] = (netBalances[exp.paidByMemberId] ?: 0.0) + exp.totalAmount
            }

            val expShares = allShares.filter { it.expenseId == exp.id }
            expShares.forEach { share ->
                netBalances[share.memberId] = (netBalances[share.memberId] ?: 0.0) - share.shareAmount
            }
        }

        // Incorporate settlements
        settlements.forEach { st ->
            netBalances[st.debtorId] = (netBalances[st.debtorId] ?: 0.0) + st.amount
            netBalances[st.creditorId] = (netBalances[st.creditorId] ?: 0.0) - st.amount
        }

        // Greedy Debt Simplification
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

    // ── Smart Group Settlement Matrix ──
    fun getGroupSettlementMatrix(groupId: Long): Flow<GroupSettlementMatrix?> {
        return combine(
            splitDao.getMembersForGroup(groupId),
            splitDao.getExpensesForGroup(groupId),
            getGroupDebtSettlements(groupId)
        ) { members: List<SplitMember>, expenses: List<SplitExpense>, debtList: List<DebtSettlement> ->
            if (members.isEmpty()) return@combine null

            val totalVolume = expenses.sumOf { it.totalAmount }
            val fairShare = totalVolume / members.size

            val memberPaidMap = mutableMapOf<Long, Double>()
            members.forEach { memberPaidMap[it.id] = 0.0 }
            expenses.forEach { exp ->
                memberPaidMap[exp.paidByMemberId] = (memberPaidMap[exp.paidByMemberId] ?: 0.0) + exp.totalAmount
            }

            val overpaid = mutableListOf<Pair<String, Double>>()
            val underpaid = mutableListOf<Pair<String, Double>>()
            val allSquare = mutableListOf<String>()

            members.forEach { m ->
                val paid = memberPaidMap[m.id] ?: 0.0
                val net = paid - fairShare
                if (net > 0.01) overpaid.add(m.name to net)
                else if (net < -0.01) underpaid.add(m.name to abs(net))
                else allSquare.add(m.name)
            }

            GroupSettlementMatrix(
                totalGroupVolume = totalVolume,
                fairSharePerMember = fairShare,
                overpaidMembers = overpaid,
                underpaidMembers = underpaid,
                allSquareMembers = allSquare,
                simplifiedTransfers = debtList
            )
        }
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
