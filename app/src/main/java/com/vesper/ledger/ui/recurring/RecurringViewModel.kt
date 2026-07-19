package com.vesper.ledger.ui.recurring

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.data.model.RecurringTransaction
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Account
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecurringWithDetails(
    val recurring: RecurringTransaction,
    val categoryName: String,
    val accountName: String,
    val targetAccountName: String?
)

class RecurringViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val recurringRepository = (application as VesperApplication).recurringRepository
    private val transactionRepository = (application as VesperApplication).transactionRepository
    private val accountRepository = (application as VesperApplication).accountRepository

    val recurringList: StateFlow<List<RecurringTransaction>> = recurringRepository.allRecurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringWithDetails: StateFlow<List<RecurringWithDetails>> = combine(recurringList, categories, accounts) { recList, catList, accList ->
        recList.map { rec ->
            val catName = catList.find { it.id == rec.categoryId }?.name ?: "Unknown"
            val accName = accList.find { it.id == rec.accountId }?.name ?: "Unknown"
            val targetAccName = rec.targetAccountId?.let { targetId ->
                accList.find { it.id == targetId }?.name
            }

            RecurringWithDetails(
                recurring = rec,
                categoryName = catName,
                accountName = accName,
                targetAccountName = targetAccName
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurringTransaction(
        title: String,
        amount: Double,
        type: String,
        accountId: Long,
        targetAccountId: Long?,
        categoryId: Long,
        paymentMethod: String,
        frequency: String,
        startDate: Long,
        endDate: Long?,
        notes: String?,
        autoCreate: Boolean
    ) {
        viewModelScope.launch {
            val item = RecurringTransaction(
                title = title,
                amount = amount,
                type = type,
                accountId = accountId,
                targetAccountId = targetAccountId,
                categoryId = categoryId,
                paymentMethod = paymentMethod,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                autoCreate = autoCreate
            )
            recurringRepository.insertRecurringTransaction(item)
        }
    }

    fun togglePause(recurring: RecurringTransaction) {
        viewModelScope.launch {
            recurringRepository.updateRecurringTransaction(recurring.copy(isPaused = !recurring.isPaused))
        }
    }

    fun deleteRecurring(recurring: RecurringTransaction) {
        viewModelScope.launch {
            recurringRepository.deleteRecurringTransaction(recurring)
        }
    }
}

class RecurringViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecurringViewModel::class.java)) {
            return RecurringViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
