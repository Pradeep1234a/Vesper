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

    // Advanced Filters
    val selectedDate = MutableStateFlow<Long?>(null)
    val startDate = MutableStateFlow<Long?>(null)
    val endDate = MutableStateFlow<Long?>(null)
    val selectedMonth = MutableStateFlow<Int?>(null)
    val selectedYear = MutableStateFlow<Int?>(null)
    val minAmount = MutableStateFlow<Double?>(null)
    val maxAmount = MutableStateFlow<Double?>(null)
    val singleAmount = MutableStateFlow<Double?>(null)

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactionRepository.allTransactions,
        searchQuery,
        selectedCategory,
        selectedType,
        sortBy,
        selectedDate,
        startDate,
        endDate,
        selectedMonth,
        selectedYear,
        minAmount,
        maxAmount,
        singleAmount
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val query = args[1] as String
        val catId = args[2] as Long?
        val type = args[3] as TransactionType?
        val sort = args[4] as SortOption
        val date = args[5] as Long?
        val start = args[6] as Long?
        val end = args[7] as Long?
        val month = args[8] as Int?
        val year = args[9] as Int?
        val min = args[10] as Double?
        val max = args[11] as Double?
        val singleAmt = args[12] as Double?

        transactions.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesCategory = catId == null || tx.categoryId == catId
            val matchesType = type == null || tx.type == type
            
            val matchesDate = if (date == null) true else {
                val cal1 = java.util.Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
                val cal2 = java.util.Calendar.getInstance().apply { timeInMillis = date }
                cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
            }
            
            val matchesDateRange = if (start == null || end == null) true else {
                tx.dateEpochMillis in start..end
            }
            
            val matchesMonth = if (month == null) true else {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
                cal.get(java.util.Calendar.MONTH) == month &&
                (year == null || cal.get(java.util.Calendar.YEAR) == year)
            }
            
            val matchesAmountRange = if (min == null || max == null) true else {
                tx.amount in min..max
            }
            
            val matchesSingleAmount = if (singleAmt == null) true else {
                Math.abs(tx.amount - singleAmt) < 0.01
            }

            matchesQuery && matchesCategory && matchesType && matchesDate && matchesDateRange && matchesMonth && matchesAmountRange && matchesSingleAmount
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

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategory.value = null
        selectedType.value = null
        selectedDate.value = null
        startDate.value = null
        endDate.value = null
        selectedMonth.value = null
        selectedYear.value = null
        minAmount.value = null
        maxAmount.value = null
        singleAmount.value = null
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
