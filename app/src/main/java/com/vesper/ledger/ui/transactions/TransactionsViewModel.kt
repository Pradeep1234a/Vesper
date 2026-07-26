package com.vesper.ledger.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.repository.AccountRepository
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

enum class DatePreset(val label: String) {
    ALL("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom")
}

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategories = MutableStateFlow<Set<Long>>(emptySet())
    val isMultiSelectCategory = MutableStateFlow(false)
    val selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedAccount = MutableStateFlow<Long?>(null)
    val selectedPaymentMethod = MutableStateFlow<String?>(null)
    val startDateFilter = MutableStateFlow<Long?>(null)
    val endDateFilter = MutableStateFlow<Long?>(null)
    val selectedDatePreset = MutableStateFlow(DatePreset.ALL)
    val minAmountFilter = MutableStateFlow<Double?>(null)
    val maxAmountFilter = MutableStateFlow<Double?>(null)
    val sortBy = MutableStateFlow(SortOption.DATE_DESC)

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDatePreset(preset: DatePreset, customStart: Long? = null, customEnd: Long? = null) {
        selectedDatePreset.value = preset
        val cal = java.util.Calendar.getInstance()
        when (preset) {
            DatePreset.ALL -> {
                startDateFilter.value = null
                endDateFilter.value = null
            }
            DatePreset.TODAY -> {
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                startDateFilter.value = cal.timeInMillis

                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                cal.set(java.util.Calendar.MILLISECOND, 999)
                endDateFilter.value = cal.timeInMillis
            }
            DatePreset.THIS_WEEK -> {
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                startDateFilter.value = cal.timeInMillis

                cal.add(java.util.Calendar.DAY_OF_WEEK, 6)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                cal.set(java.util.Calendar.MILLISECOND, 999)
                endDateFilter.value = cal.timeInMillis
            }
            DatePreset.THIS_MONTH -> {
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                startDateFilter.value = cal.timeInMillis

                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                cal.set(java.util.Calendar.MINUTE, 59)
                cal.set(java.util.Calendar.SECOND, 59)
                cal.set(java.util.Calendar.MILLISECOND, 999)
                endDateFilter.value = cal.timeInMillis
            }
            DatePreset.CUSTOM -> {
                startDateFilter.value = customStart
                endDateFilter.value = customEnd
            }
        }
    }

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
        selectedAccount,
        selectedPaymentMethod,
        startDateFilter,
        endDateFilter,
        minAmountFilter,
        maxAmountFilter,
        sortBy
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val transactions = args[0] as List<Transaction>
        val query = args[1] as String
        val catIds = args[2] as Set<Long>
        val type = args[3] as TransactionType?
        val acctId = args[4] as Long?
        val pMethod = args[5] as String?
        val startEpoch = args[6] as Long?
        val endEpoch = args[7] as Long?
        val minAmt = args[8] as Double?
        val maxAmt = args[9] as Double?
        val sort = args[10] as SortOption

        transactions.filter { tx ->
            val matchesQuery = query.isBlank() || tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesCategory = catIds.isEmpty() || catIds.contains(tx.categoryId)
            val matchesType = type == null || tx.type == type
            val matchesAccount = acctId == null || tx.accountId == acctId
            val matchesPaymentMethod = pMethod == null || tx.paymentMethod.equals(pMethod, ignoreCase = true)
            val matchesStartDate = startEpoch == null || tx.dateEpochMillis >= startEpoch
            val matchesEndDate = endEpoch == null || tx.dateEpochMillis <= endEpoch
            val matchesMinAmount = minAmt == null || tx.amount >= minAmt
            val matchesMaxAmount = maxAmt == null || tx.amount <= maxAmt

            matchesQuery && matchesCategory && matchesType && matchesAccount &&
                    matchesPaymentMethod && matchesStartDate && matchesEndDate &&
                    matchesMinAmount && matchesMaxAmount
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

    val activeFilterCount: StateFlow<Int> = combine(
        selectedCategories,
        selectedType,
        selectedAccount,
        selectedPaymentMethod,
        startDateFilter,
        endDateFilter,
        minAmountFilter,
        maxAmountFilter
    ) { args ->
        var count = 0
        if ((args[0] as Set<*>).isNotEmpty()) count++
        if (args[1] != null) count++
        if (args[2] != null) count++
        if (args[3] != null) count++
        if (args[4] != null || args[5] != null) count++
        if (args[6] != null || args[7] != null) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun clearAllFilters() {
        searchQuery.value = ""
        selectedCategories.value = emptySet()
        isMultiSelectCategory.value = false
        selectedType.value = null
        selectedAccount.value = null
        selectedPaymentMethod.value = null
        startDateFilter.value = null
        endDateFilter.value = null
        selectedDatePreset.value = DatePreset.ALL
        minAmountFilter.value = null
        maxAmountFilter.value = null
        sortBy.value = SortOption.DATE_DESC
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }
}

class TransactionsViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            return TransactionsViewModel(transactionRepository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
