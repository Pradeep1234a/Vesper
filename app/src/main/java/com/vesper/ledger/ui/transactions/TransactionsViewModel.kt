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
import java.util.Calendar

enum class SortOption {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, EXPENSE_DESC, INCOME_DESC
}

enum class DateFilterOption {
    ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, LAST_MONTH, LAST_3_MONTHS, LAST_6_MONTHS, THIS_YEAR, CUSTOM_RANGE, SPECIFIC_DATE, SPECIFIC_MONTH
}

enum class AmountFilterOption {
    ALL, UNDER_50, UNDER_100, OVER_500, OVER_1000, CUSTOM
}

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategories = MutableStateFlow<Set<Long>>(emptySet())
    val isMultiSelectCategory = MutableStateFlow(false)
    val selectedType = MutableStateFlow<TransactionType?>(null)
    val sortBy = MutableStateFlow(SortOption.DATE_DESC)

    // Advanced Filters
    val dateFilterOption = MutableStateFlow(DateFilterOption.ALL)
    val selectedDate = MutableStateFlow<Long?>(null)
    val startDate = MutableStateFlow<Long?>(null)
    val endDate = MutableStateFlow<Long?>(null)
    val selectedMonth = MutableStateFlow<Int?>(null)
    val selectedYear = MutableStateFlow<Int?>(null)
    
    val amountFilterOption = MutableStateFlow(AmountFilterOption.ALL)
    val minAmount = MutableStateFlow<Double?>(null)
    val maxAmount = MutableStateFlow<Double?>(null)

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
        sortBy,
        dateFilterOption,
        selectedDate,
        startDate,
        endDate,
        selectedMonth,
        selectedYear,
        amountFilterOption,
        minAmount,
        maxAmount
    ) { args ->
        val transactions = args[0] as List<Transaction>
        val query = args[1] as String
        val catIds = args[2] as Set<Long>
        val type = args[3] as TransactionType?
        val sort = args[4] as SortOption
        val dateOption = args[5] as DateFilterOption
        val date = args[6] as Long?
        val start = args[7] as Long?
        val end = args[8] as Long?
        val month = args[9] as Int?
        val year = args[10] as Int?
        val amtOption = args[11] as AmountFilterOption
        val min = args[12] as Double?
        val max = args[13] as Double?

        transactions.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesCategory = catIds.isEmpty() || catIds.contains(tx.categoryId)
            val matchesType = type == null || tx.type == type
            
            // Date option boundaries
            val matchesDate = when (dateOption) {
                DateFilterOption.ALL -> true
                DateFilterOption.SPECIFIC_DATE -> if (date == null) true else {
                    val cal1 = Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
                    val cal2 = Calendar.getInstance().apply { timeInMillis = date }
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                }
                DateFilterOption.CUSTOM_RANGE -> if (start == null || end == null) true else {
                    tx.dateEpochMillis in start..end
                }
                DateFilterOption.SPECIFIC_MONTH -> if (month == null) true else {
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
                    cal.get(Calendar.MONTH) == month &&
                    (year == null || cal.get(Calendar.YEAR) == year)
                }
                else -> {
                    val range = getPeriodRange(dateOption)
                    if (range == null) true else tx.dateEpochMillis in range.first..range.second
                }
            }
            
            // Amount options
            val matchesAmount = when (amtOption) {
                AmountFilterOption.ALL -> true
                AmountFilterOption.UNDER_50 -> tx.amount < 50.0
                AmountFilterOption.UNDER_100 -> tx.amount < 100.0
                AmountFilterOption.OVER_500 -> tx.amount > 500.0
                AmountFilterOption.OVER_1000 -> tx.amount > 1000.0
                AmountFilterOption.CUSTOM -> {
                    val minVal = min ?: 0.0
                    val maxVal = max ?: Double.MAX_VALUE
                    tx.amount in minVal..maxVal
                }
            }

            matchesQuery && matchesCategory && matchesType && matchesDate && matchesAmount
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

    private fun getPeriodRange(option: DateFilterOption): Pair<Long, Long>? {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis
        
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        
        return when (option) {
            DateFilterOption.TODAY -> Pair(startOfToday, now)
            DateFilterOption.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val startOfYesterday = cal.timeInMillis
                val endOfYesterday = startOfYesterday + 24 * 60 * 60 * 1000 - 1
                Pair(startOfYesterday, endOfYesterday)
            }
            DateFilterOption.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                Pair(cal.timeInMillis, now)
            }
            DateFilterOption.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                Pair(cal.timeInMillis, now)
            }
            DateFilterOption.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startOfLastMonth = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                Pair(startOfLastMonth, cal.timeInMillis)
            }
            DateFilterOption.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                Pair(cal.timeInMillis, now)
            }
            DateFilterOption.LAST_6_MONTHS -> {
                cal.add(Calendar.MONTH, -6)
                Pair(cal.timeInMillis, now)
            }
            DateFilterOption.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                Pair(cal.timeInMillis, now)
            }
            else -> null
        }
    }

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategories.value = emptySet()
        isMultiSelectCategory.value = false
        selectedType.value = null
        sortBy.value = SortOption.DATE_DESC
        dateFilterOption.value = DateFilterOption.ALL
        selectedDate.value = null
        startDate.value = null
        endDate.value = null
        selectedMonth.value = null
        selectedYear.value = null
        amountFilterOption.value = AmountFilterOption.ALL
        minAmount.value = null
        maxAmount.value = null
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
