package com.vesper.ledger.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CategoryReport(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

data class ReportsUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val categoryReports: List<CategoryReport> = emptyList()
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        transactionRepository.allTransactions,
        transactionRepository.allCategories
    ) { transactions, categories ->
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
        val reports = categories.filter { it.type == TransactionType.EXPENSE }.map { cat ->
            val total = expenseTransactions.filter { it.categoryId == cat.id }.sumOf { it.amount }
            val pct = if (expense > 0) (total / expense).toFloat() else 0f
            CategoryReport(cat, total, pct)
        }.filter { it.totalAmount > 0 }.sortedByDescending { it.totalAmount }

        ReportsUiState(
            totalIncome = income,
            totalExpense = expense,
            categoryReports = reports
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )
}

class ReportsViewModelFactory(
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
