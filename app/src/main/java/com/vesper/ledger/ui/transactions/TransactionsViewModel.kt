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
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC
}

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<Long?>(null)
    val selectedType = MutableStateFlow<TransactionType?>(null)
    val sortBy = MutableStateFlow(SortOption.DATE_DESC)

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactionRepository.allTransactions,
        searchQuery,
        selectedCategory,
        selectedType,
        sortBy
    ) { transactions, query, catId, type, sort ->
        transactions.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesCategory = catId == null || tx.categoryId == catId
            val matchesType = type == null || tx.type == type
            matchesQuery && matchesCategory && matchesType
        }.sortedWith(
            when (sort) {
                SortOption.DATE_DESC -> compareByDescending { it.dateEpochMillis }
                SortOption.DATE_ASC -> compareBy { it.dateEpochMillis }
                SortOption.AMOUNT_DESC -> compareByDescending { it.amount }
                SortOption.AMOUNT_ASC -> compareBy { it.amount }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
