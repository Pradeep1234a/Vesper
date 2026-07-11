package com.vesper.ledger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.SavingsRepository
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSaved: Double = 0.0,
    val availableBalance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList()
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.allTransactions,
        savingsRepository.allSavingsGoals
    ) { transactions, savingsGoals ->
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val saved = savingsGoals.sumOf { it.currentAmount }
        val available = income - expense
        val recent = transactions.take(5)

        DashboardUiState(
            totalIncome = income,
            totalExpense = expense,
            totalSaved = saved,
            availableBalance = available,
            recentTransactions = recent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}

class DashboardViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(transactionRepository, savingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
