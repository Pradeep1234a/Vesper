package com.vesper.ledger.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
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
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val title = MutableStateFlow("")
    val amount = MutableStateFlow("") // e.g. "12.50"
    val type = MutableStateFlow(TransactionType.EXPENSE)
    val categoryId = MutableStateFlow<Long?>(null)
    val dateEpochMillis = MutableStateFlow(System.currentTimeMillis())
    val note = MutableStateFlow("")

    var isEditMode by mutableStateOf(false)
        private set

    private var editingTransactionId: Long? = null

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCategories: StateFlow<List<Category>> = combine(
        categories,
        type
    ) { allCats, selectedType ->
        val filtered = allCats.filter { it.type == selectedType }
        val currentCat = allCats.find { it.id == categoryId.value }
        if (currentCat == null || currentCat.type != selectedType) {
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
                    note = note.value
                )
                transactionRepository.updateTransaction(transaction)
            } else {
                val transaction = Transaction(
                    title = txTitle,
                    amount = amtValue,
                    type = type.value,
                    categoryId = catId,
                    dateEpochMillis = dateEpochMillis.value,
                    note = note.value
                )
                transactionRepository.insertTransaction(transaction)
            }
            onSuccess()
        }
    }
}

class AddTransactionViewModelFactory(
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            return AddTransactionViewModel(transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
