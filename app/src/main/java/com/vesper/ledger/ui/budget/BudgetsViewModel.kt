package com.vesper.ledger.ui.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetWithStatus(
    val budget: Budget,
    val categoryName: String,
    val categoryColor: String,
    val categoryIcon: String = "category",
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Float
)

class BudgetsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val budgetRepository = (application as VesperApplication).budgetRepository
    private val transactionRepository = (application as VesperApplication).transactionRepository

    val budgets: StateFlow<List<Budget>> = budgetRepository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = transactionRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetsWithStatus: StateFlow<List<BudgetWithStatus>> = combine(budgets, categories, transactions) { budgetList, catList, txList ->
        budgetList.map { budget ->
            val category = catList.find { it.id == budget.categoryId }
            val catName = category?.name ?: "Unknown"
            val catColor = category?.colorHex ?: "#71717A"
            val catIcon = category?.iconName ?: "category"

            // Filter transactions of type EXPENSE for this category within budget dates
            val spent = txList.filter {
                it.type == TransactionType.EXPENSE &&
                it.categoryId == budget.categoryId &&
                it.dateEpochMillis >= budget.startDate &&
                it.dateEpochMillis <= budget.endDate
            }.sumOf { it.amount }

            val remaining = budget.amount - spent
            val prog = if (budget.amount > 0) (spent / budget.amount).toFloat().coerceIn(0f, 1f) else 0f

            BudgetWithStatus(
                budget = budget,
                categoryName = catName,
                categoryColor = catColor,
                categoryIcon = catIcon,
                spentAmount = spent,
                remainingAmount = remaining,
                progress = prog
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addBudget(name: String, amount: Double, period: String, categoryId: Long, startDate: Long, endDate: Long, notes: String?) {
        viewModelScope.launch {
            val budget = Budget(
                name = name,
                amount = amount,
                period = period,
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
            budgetRepository.insertBudget(budget)
        }
    }

    fun updateBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget)
        }
    }
}

class BudgetsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetsViewModel::class.java)) {
            return BudgetsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
