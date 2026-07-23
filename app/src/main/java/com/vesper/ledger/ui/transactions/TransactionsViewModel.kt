package com.vesper.ledger.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, EXPENSE_DESC, INCOME_DESC
}

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategories = MutableStateFlow<Set<Long>>(emptySet())
    val isMultiSelectCategory = MutableStateFlow(false)
    val selectedType = MutableStateFlow<TransactionType?>(null)
    val sortBy = MutableStateFlow(SortOption.DATE_DESC)

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dynamically calculate the top 3 most frequently used categories
    val adaptiveCategories: StateFlow<List<Category>> = combine(
        transactionRepository.allTransactions,
        transactionRepository.allCategories
    ) { transactions, categories ->
        val frequencies = transactions.groupBy { it.categoryId }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        frequencies.mapNotNull { (catId, _) ->
            categories.find { it.id == catId }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactionRepository.allTransactions,
        searchQuery,
        selectedCategories,
        selectedType,
        sortBy
    ) { transactions, query, catIds, type, sort ->
        transactions.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesCategory = catIds.isEmpty() || catIds.contains(tx.categoryId)
            val matchesType = type == null || tx.type == type
            matchesQuery && matchesCategory && matchesType
        }.sortedWith(
            when (sort) {
                SortOption.DATE_DESC -> compareByDescending { it.dateEpochMillis }
                SortOption.DATE_ASC -> compareBy { it.dateEpochMillis }
                SortOption.AMOUNT_DESC -> compareByDescending { it.amount }
                SortOption.AMOUNT_ASC -> compareBy { it.amount }
                SortOption.EXPENSE_DESC -> compareByDescending { if (it.type == TransactionType.EXPENSE) it.amount else 0.0 }
                SortOption.INCOME_DESC -> compareByDescending { if (it.type == TransactionType.INCOME) it.amount else 0.0 }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategories.value = emptySet()
        isMultiSelectCategory.value = false
        selectedType.value = null
        sortBy.value = SortOption.DATE_DESC
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountName: String = "Cash Wallet",
        note: String = ""
    ) {
        viewModelScope.launch {
            val tx = Transaction(
                title = title,
                amount = amount,
                type = type,
                categoryId = categoryId,
                dateEpochMillis = System.currentTimeMillis(),
                note = note,
                accountName = accountName
            )
            transactionRepository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}

class TransactionsViewModelFactory(
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            return TransactionsViewModel(transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
