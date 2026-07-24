package com.vesper.ledger.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.AccountRepository
import com.vesper.ledger.data.repository.BudgetRepository
import com.vesper.ledger.data.repository.SavingsRepository
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategorySpending(
    val categoryName: String,
    val iconName: String,
    val amount: Double
)

data class DashboardUiState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSaved: Double = 0.0,
    val totalTarget: Double = 0.0,
    val totalBudget: Double = 0.0,
    val availableBalance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val topCategories: List<CategorySpending> = emptyList(),
    val categories: List<Category> = emptyList()
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        transactionRepository.allTransactions,
        savingsRepository.allSavingsGoals,
        transactionRepository.allCategories,
        accountRepository.allAccounts,
        budgetRepository.allBudgets
    ) { transactions, savingsGoals, categories, accounts, budgets ->
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val saved = savingsGoals.sumOf { it.currentAmount }
        val target = savingsGoals.sumOf { it.targetAmount }
        val activeBudgetTotal = budgets.filter { !it.isArchived }.sumOf { it.limitAmount }
        val recent = transactions.take(5)

        // Calculate total balance from included active accounts (includes opening balances)
        val available = accounts.filter { it.includeInTotal && !it.isHidden }.sumOf { account ->
            val acctIncome = transactions.filter { it.accountId == account.id && it.type == TransactionType.INCOME }.sumOf { it.amount }
            val acctExpense = transactions.filter { it.accountId == account.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val transfersOut = transactions.filter { it.accountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            val transfersIn = transactions.filter { it.targetAccountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            account.initialBalance + acctIncome - acctExpense - transfersOut + transfersIn
        }

        // Calculate top 3 categories by expense spending
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val categorySpendMap = expenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val sortedSpend = categorySpendMap.toList()
            .sortedByDescending { it.second }
            .take(3)

        val topCats = sortedSpend.map { (catId, amt) ->
            val cat = categories.find { it.id == catId }
            CategorySpending(
                categoryName = cat?.name ?: "Other",
                iconName = cat?.iconName ?: "category",
                amount = amt
            )
        }

        DashboardUiState(
            totalIncome = income,
            totalExpense = expense,
            totalSaved = saved,
            totalTarget = target,
            totalBudget = activeBudgetTotal,
            availableBalance = available,
            recentTransactions = recent,
            topCategories = topCats,
            categories = categories
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

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountName: String,
        note: String
    ) {
        viewModelScope.launch {
            val newTx = Transaction(
                title = title.ifBlank { if (type == TransactionType.INCOME) "Income" else "Expense" },
                amount = amount,
                type = type,
                categoryId = categoryId,
                accountId = 1L,
                accountName = accountName.ifBlank { "Cash / Wallet" },
                dateEpochMillis = System.currentTimeMillis(),
                note = note
            )
            transactionRepository.insertTransaction(newTx)
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, type: TransactionType = TransactionType.EXPENSE) {
        viewModelScope.launch {
            val newCat = Category(
                name = name,
                iconName = iconName,
                type = type,
                colorHex = colorHex
            )
            transactionRepository.insertCategory(newCat)
        }
    }
}

class DashboardViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val savingsRepository: SavingsRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(transactionRepository, savingsRepository, accountRepository, budgetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
