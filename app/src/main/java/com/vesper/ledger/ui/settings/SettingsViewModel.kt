package com.vesper.ledger.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)

    val currency = MutableStateFlow(sharedPrefs.getString("currency", "$") ?: "$")
    val theme = MutableStateFlow(sharedPrefs.getString("theme", "system") ?: "system")

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCurrency(newCurrency: String) {
        currency.value = newCurrency
        sharedPrefs.edit().putString("currency", newCurrency).apply()
    }

    fun saveTheme(newTheme: String) {
        theme.value = newTheme
        sharedPrefs.edit().putString("theme", newTheme).apply()
    }

    fun addCategory(name: String, type: TransactionType, colorHex: String) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                iconName = "category",
                type = type,
                colorHex = colorHex
            )
            transactionRepository.insertCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            transactionRepository.deleteCategory(category)
        }
    }
}

class SettingsViewModelFactory(
    private val application: Application,
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(application, transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
