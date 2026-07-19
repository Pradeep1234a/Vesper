package com.vesper.ledger.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import com.vesper.ledger.data.repository.AccountRepository
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    val title = MutableStateFlow("")
    val amount = MutableStateFlow("") // e.g. "12.50"
    val type = MutableStateFlow(TransactionType.EXPENSE)
    val categoryId = MutableStateFlow<Long?>(null)
    val dateEpochMillis = MutableStateFlow(System.currentTimeMillis())
    val note = MutableStateFlow("")
    val accountName = MutableStateFlow("Cash Wallet")
    val accountId = MutableStateFlow<Long>(0)
    val targetAccountName = MutableStateFlow("Select Account")
    val targetAccountId = MutableStateFlow<Long?>(null)
    val paymentMethod = MutableStateFlow("Cash")
    val recurringPattern = MutableStateFlow("One Time")
    val location = MutableStateFlow("")

    var isEditMode by mutableStateOf(false)
        private set

    private var editingTransactionId: Long? = null

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<com.vesper.ledger.data.model.Account>> = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<com.vesper.ledger.data.model.PaymentMethod>> = accountRepository.allPaymentMethods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCategories: StateFlow<List<Category>> = combine(
        categories,
        type
    ) { allCats, selectedType ->
        val targetType = if (selectedType == TransactionType.TRANSFER) TransactionType.EXPENSE else selectedType
        val filtered = allCats.filter { it.type == targetType }
        val currentCat = allCats.find { it.id == categoryId.value }
        if (currentCat == null || (selectedType != TransactionType.TRANSFER && currentCat.type != selectedType)) {
            categoryId.value = filtered.firstOrNull()?.id
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadTransaction(id: Long, fallbackType: TransactionType?) {
        if (id != -1L) {
            editingTransactionId = id
            isEditMode = true
            viewModelScope.launch {
                val tx = transactionRepository.getTransactionById(id)
                if (tx != null) {
                    title.value = tx.title
                    amount.value = tx.amount.toString()
                    type.value = tx.type
                    categoryId.value = tx.categoryId
                    dateEpochMillis.value = tx.dateEpochMillis
                    note.value = tx.note
                    accountName.value = tx.accountName
                    accountId.value = tx.accountId
                    targetAccountId.value = tx.targetAccountId
                    paymentMethod.value = tx.paymentMethod
                    recurringPattern.value = tx.recurringPattern
                    location.value = tx.location
                    
                    // Fetch target account name
                    val targetAcc = tx.targetAccountId?.let { accountRepository.getAccountById(it) }
                    targetAccountName.value = targetAcc?.name ?: "Select Account"
                }
            }
        } else {
            editingTransactionId = null
            isEditMode = false
            title.value = ""
            amount.value = ""
            if (fallbackType != null) {
                type.value = fallbackType
            }
            categoryId.value = null
            dateEpochMillis.value = System.currentTimeMillis()
            note.value = ""
            accountName.value = "Cash Wallet"
            accountId.value = 0L
            targetAccountName.value = "Select Account"
            targetAccountId.value = null
            paymentMethod.value = "Cash"
            recurringPattern.value = "One Time"
            location.value = ""

            // Prepopulate default account & payment method from settings
            viewModelScope.launch {
                val accList = accountRepository.allAccounts.stateIn(viewModelScope).value
                val defaultAcc = accList.find { it.name == accountName.value }
                if (defaultAcc != null) {
                    accountId.value = defaultAcc.id
                } else if (accList.isNotEmpty()) {
                    accountName.value = accList.first().name
                    accountId.value = accList.first().id
                }
            }
        }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val amtValue = amount.value.toDoubleOrNull() ?: 0.0
        val catId = categoryId.value ?: 1L
        val txTitle = title.value.ifBlank { "Untitled Transaction" }

        viewModelScope.launch {
            val txId = editingTransactionId
            if (txId != null) {
                val transaction = Transaction(
                    id = txId,
                    title = txTitle,
                    amount = amtValue,
                    type = type.value,
                    categoryId = catId,
                    dateEpochMillis = dateEpochMillis.value,
                    note = note.value,
                    accountName = accountName.value,
                    accountId = accountId.value,
                    targetAccountId = if (type.value == TransactionType.TRANSFER) targetAccountId.value else null,
                    paymentMethod = paymentMethod.value,
                    recurringPattern = recurringPattern.value,
                    location = location.value
                )
                transactionRepository.updateTransaction(transaction)
            } else {
                val transaction = Transaction(
                    title = txTitle,
                    amount = amtValue,
                    type = type.value,
                    categoryId = catId,
                    dateEpochMillis = dateEpochMillis.value,
                    note = note.value,
                    accountName = accountName.value,
                    accountId = accountId.value,
                    targetAccountId = if (type.value == TransactionType.TRANSFER) targetAccountId.value else null,
                    paymentMethod = paymentMethod.value,
                    recurringPattern = recurringPattern.value,
                    location = location.value
                )
                transactionRepository.insertTransaction(transaction)
            }
            onSuccess()
        }
    }
}

class AddTransactionViewModelFactory(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            return AddTransactionViewModel(transactionRepository, accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
