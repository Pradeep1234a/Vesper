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
    val language = MutableStateFlow(sharedPrefs.getString("language", "English") ?: "English")
    val dynamicColors = MutableStateFlow(sharedPrefs.getBoolean("dynamicColors", true))
    val defaultTransactionType = MutableStateFlow(sharedPrefs.getString("defaultTransactionType", "Expense") ?: "Expense")
    val quickAddPreferences = MutableStateFlow(sharedPrefs.getBoolean("quickAddPreferences", true))
    val defaultAccount = MutableStateFlow(sharedPrefs.getString("defaultAccount", "Cash") ?: "Cash")
    val dailyReminder = MutableStateFlow(sharedPrefs.getBoolean("dailyReminder", false))
    val missedEntryReminder = MutableStateFlow(sharedPrefs.getBoolean("missedEntryReminder", false))
    val budgetReminder = MutableStateFlow(sharedPrefs.getBoolean("budgetReminder", false))
    val recurringReminder = MutableStateFlow(sharedPrefs.getBoolean("recurringReminder", false))
    val appLock = MutableStateFlow(sharedPrefs.getBoolean("appLock", false))
    val biometricAuth = MutableStateFlow(sharedPrefs.getBoolean("biometricAuth", false))
    val isProUser = MutableStateFlow(sharedPrefs.getBoolean("isProUser", false))
    val userName = MutableStateFlow(sharedPrefs.getString("userName", "User") ?: "User")
    val isFirstLaunch = MutableStateFlow(sharedPrefs.getBoolean("isFirstLaunch", true))

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

    fun saveLanguage(newValue: String) {
        language.value = newValue
        sharedPrefs.edit().putString("language", newValue).apply()
    }

    fun saveDynamicColors(newValue: Boolean) {
        dynamicColors.value = newValue
        sharedPrefs.edit().putBoolean("dynamicColors", newValue).apply()
    }

    fun saveDefaultTransactionType(newValue: String) {
        defaultTransactionType.value = newValue
        sharedPrefs.edit().putString("defaultTransactionType", newValue).apply()
    }

    fun saveQuickAddPreferences(newValue: Boolean) {
        quickAddPreferences.value = newValue
        sharedPrefs.edit().putBoolean("quickAddPreferences", newValue).apply()
    }

    fun saveDefaultAccount(newValue: String) {
        defaultAccount.value = newValue
        sharedPrefs.edit().putString("defaultAccount", newValue).apply()
    }

    fun saveDailyReminder(newValue: Boolean) {
        dailyReminder.value = newValue
        sharedPrefs.edit().putBoolean("dailyReminder", newValue).apply()
    }

    fun saveMissedEntryReminder(newValue: Boolean) {
        missedEntryReminder.value = newValue
        sharedPrefs.edit().putBoolean("missedEntryReminder", newValue).apply()
    }

    fun saveBudgetReminder(newValue: Boolean) {
        budgetReminder.value = newValue
        sharedPrefs.edit().putBoolean("budgetReminder", newValue).apply()
    }

    fun saveRecurringReminder(newValue: Boolean) {
        recurringReminder.value = newValue
        sharedPrefs.edit().putBoolean("recurringReminder", newValue).apply()
    }

    fun saveAppLock(newValue: Boolean) {
        appLock.value = newValue
        sharedPrefs.edit().putBoolean("appLock", newValue).apply()
    }

    fun saveBiometricAuth(newValue: Boolean) {
        biometricAuth.value = newValue
        sharedPrefs.edit().putBoolean("biometricAuth", newValue).apply()
    }

    fun saveIsProUser(newValue: Boolean) {
        isProUser.value = newValue
        sharedPrefs.edit().putBoolean("isProUser", newValue).apply()
    }

    fun saveUserName(newValue: String) {
        userName.value = newValue
        sharedPrefs.edit().putString("userName", newValue).apply()
    }

    fun saveFirstLaunch(newValue: Boolean) {
        isFirstLaunch.value = newValue
        sharedPrefs.edit().putBoolean("isFirstLaunch", newValue).apply()
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
