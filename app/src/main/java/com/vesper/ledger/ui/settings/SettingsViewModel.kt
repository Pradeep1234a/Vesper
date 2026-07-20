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

    private fun getCurrencySymbol(code: String): String {
        return when (code) {
            "AED" -> "د.إ"
            "ARS" -> "$"
            "AUD" -> "$"
            "BDT" -> "৳"
            "BRL" -> "R$"
            "CAD" -> "CA$"
            "CHF" -> "CHF"
            "CNY" -> "¥"
            "DKK" -> "kr"
            "EUR" -> "€"
            "GBP" -> "£"
            "ILS" -> "₪"
            "INR" -> "₹"
            "JPY" -> "¥"
            "MXN" -> "$"
            "NZD" -> "$"
            "RUB" -> "₽"
            "SAR" -> "ر.س"
            "SGD" -> "$"
            "USD" -> "$"
            "ZAR" -> "R"
            else -> code
        }
    }

    val currency = MutableStateFlow(sharedPrefs.getString("currency", "$") ?: "$")
    val currencySymbol = MutableStateFlow(getCurrencySymbol(sharedPrefs.getString("currency", "$") ?: "$"))
    val theme = MutableStateFlow(sharedPrefs.getString("theme", "system") ?: "system")
    val language = MutableStateFlow(sharedPrefs.getString("language", "English") ?: "English")
    val defaultTransactionType = MutableStateFlow(sharedPrefs.getString("defaultTransactionType", "Expense") ?: "Expense")
    val quickAddPreferences = MutableStateFlow(sharedPrefs.getBoolean("quickAddPreferences", true))
    val defaultAccount = MutableStateFlow(sharedPrefs.getString("defaultAccount", "Cash Wallet") ?: "Cash Wallet")
    val defaultPaymentMethod = MutableStateFlow(sharedPrefs.getString("defaultPaymentMethod", "Cash") ?: "Cash")

    val accounts: StateFlow<List<com.vesper.ledger.data.model.Account>> = (application as com.vesper.ledger.VesperApplication).accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentMethods: StateFlow<List<com.vesper.ledger.data.model.PaymentMethod>> = (application as com.vesper.ledger.VesperApplication).accountRepository.allPaymentMethods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appIcon = MutableStateFlow(sharedPrefs.getString("appIcon", "default") ?: "default")
    val userName = MutableStateFlow(sharedPrefs.getString("userName", "User") ?: "User")
    val userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "") ?: "")
    val isFirstLaunch = MutableStateFlow(sharedPrefs.getBoolean("isFirstLaunch", true))

    // High Fidelity Personalization parameters
    val accentColor = MutableStateFlow(sharedPrefs.getString("accentColor", "rose") ?: "rose")
    val appStyle = MutableStateFlow(sharedPrefs.getString("appStyle", "comfortable") ?: "comfortable")
    val startScreen = MutableStateFlow(sharedPrefs.getString("startScreen", "dashboard") ?: "dashboard")

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveCurrency(newCurrency: String) {
        currency.value = newCurrency
        currencySymbol.value = getCurrencySymbol(newCurrency)
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

    fun saveDefaultPaymentMethod(newValue: String) {
        defaultPaymentMethod.value = newValue
        sharedPrefs.edit().putString("defaultPaymentMethod", newValue).apply()
    }

    fun saveAppIcon(newValue: String) {
        appIcon.value = newValue
        sharedPrefs.edit().putString("appIcon", newValue).apply()
        com.vesper.ledger.data.secure.AppIconManager.setAppIcon(getApplication(), newValue)
    }

    fun saveUserName(newValue: String) {
        userName.value = newValue
        sharedPrefs.edit().putString("userName", newValue).apply()
        
        // Write the updated name to the user's active database
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val email = sharedPrefs.getString("user_email", "") ?: ""
            if (email.isNotBlank()) {
                val db = com.vesper.ledger.data.local.AppDatabase.getDatabase(getApplication())
                db.userDao().updateFullName(email, newValue)
            }
        }
    }

    fun saveFirstLaunch(newValue: Boolean) {
        isFirstLaunch.value = newValue
        sharedPrefs.edit().putBoolean("isFirstLaunch", newValue).apply()
    }

    fun saveAccentColor(newValue: String) {
        accentColor.value = newValue
        sharedPrefs.edit().putString("accentColor", newValue).apply()
    }

    fun saveAppStyle(newValue: String) {
        appStyle.value = newValue
        sharedPrefs.edit().putString("appStyle", newValue).apply()
    }

    fun saveStartScreen(newValue: String) {
        startScreen.value = newValue
        sharedPrefs.edit().putString("startScreen", newValue).apply()
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
