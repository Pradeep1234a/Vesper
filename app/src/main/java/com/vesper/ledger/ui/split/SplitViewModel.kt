package com.vesper.ledger.ui.split

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.DebtSettlement
import com.vesper.ledger.data.model.GroupAnalyticsSummary
import com.vesper.ledger.data.model.SplitExpense
import com.vesper.ledger.data.model.SplitExpenseShare
import com.vesper.ledger.data.model.SplitGroup
import com.vesper.ledger.data.model.SplitMember
import com.vesper.ledger.data.repository.SplitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SplitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SplitRepository(
        splitDao = db.splitDao(),
        transactionDao = db.transactionDao(),
        accountDao = db.accountDao()
    )

    val allGroups: StateFlow<List<SplitGroup>> = repository.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<SplitExpense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallDebts: StateFlow<List<DebtSettlement>> = repository.getOverallDebtSettlements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = db.accountDao().getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculated overall totals
    val totalOwedToUser: StateFlow<Double> = overallDebts.map { list ->
        list.filter { it.isCreditorCurrentUser }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalUserOwes: StateFlow<Double> = overallDebts.map { list ->
        list.filter { it.isDebtorCurrentUser }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun createGroup(title: String, category: String, memberNames: List<String>, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createGroup(title, category, memberNames)
            onCreated(id)
        }
    }

    fun deleteGroup(groupId: Long) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
        }
    }

    fun addExpense(
        groupId: Long,
        title: String,
        totalAmount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        paymentMethod: String,
        accountId: Long,
        categoryName: String,
        shares: Map<Long, Double>,
        isPaidByCurrentUser: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.addSplitExpense(
                groupId = groupId,
                title = title,
                totalAmount = totalAmount,
                paidByMemberId = paidByMemberId,
                paidByMemberName = paidByMemberName,
                paymentMethod = paymentMethod,
                accountId = accountId,
                categoryName = categoryName,
                shares = shares,
                isPaidByCurrentUser = isPaidByCurrentUser
            )
            onSuccess()
        }
    }

    fun deleteExpense(expenseId: Long, onDelete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteSplitExpense(expenseId)
            onDelete()
        }
    }

    fun toggleSharePaymentStatus(shareId: Long, isPaid: Boolean) {
        viewModelScope.launch {
            repository.toggleSharePaymentStatus(shareId, isPaid)
        }
    }

    fun recordSettlement(
        groupId: Long,
        debtorId: Long,
        debtorName: String,
        creditorId: Long,
        creditorName: String,
        amount: Double,
        paymentMethod: String,
        accountId: Long,
        isDebtorCurrentUser: Boolean,
        isCreditorCurrentUser: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.recordSettlement(
                groupId = groupId,
                debtorId = debtorId,
                debtorName = debtorName,
                creditorId = creditorId,
                creditorName = creditorName,
                amount = amount,
                paymentMethod = paymentMethod,
                accountId = accountId,
                isDebtorCurrentUser = isDebtorCurrentUser,
                isCreditorCurrentUser = isCreditorCurrentUser
            )
            onSuccess()
        }
    }

    fun getMembersForGroup(groupId: Long): Flow<List<SplitMember>> = repository.getMembersForGroup(groupId)
    fun getGroupDebts(groupId: Long): Flow<List<DebtSettlement>> = repository.getGroupDebtSettlements(groupId)
    fun getExpensesForGroup(groupId: Long): Flow<List<SplitExpense>> = repository.getExpensesForGroup(groupId)
    fun getSharesForExpense(expenseId: Long): Flow<List<SplitExpenseShare>> = repository.getSharesForExpense(expenseId)
    fun getGroupAnalyticsSummary(groupId: Long): Flow<GroupAnalyticsSummary?> = repository.getGroupAnalyticsSummary(groupId)
}
