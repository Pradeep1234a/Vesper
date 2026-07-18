package com.vesper.ledger.ui.settings

import android.app.Application
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import com.vesper.ledger.data.secure.SecureStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BiometricSupportType {
    AVAILABLE,
    NO_HARDWARE,
    UNAVAILABLE,
    NONE_ENROLLED
}

class SettingsViewModel(
    application: Application,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
    private val secureStorage = SecureStorageHelper.getInstance(application)

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
    val dynamicColors = MutableStateFlow(sharedPrefs.getBoolean("dynamicColors", true))
    val defaultTransactionType = MutableStateFlow(sharedPrefs.getString("defaultTransactionType", "Expense") ?: "Expense")
    val quickAddPreferences = MutableStateFlow(sharedPrefs.getBoolean("quickAddPreferences", true))
    val defaultAccount = MutableStateFlow(sharedPrefs.getString("defaultAccount", "Cash") ?: "Cash")
    val dailyReminder = MutableStateFlow(sharedPrefs.getBoolean("dailyReminder", true))
    val missedEntryReminder = MutableStateFlow(sharedPrefs.getBoolean("missedEntryReminder", true))
    val budgetReminder = MutableStateFlow(sharedPrefs.getBoolean("budgetReminder", true))
    val recurringReminder = MutableStateFlow(sharedPrefs.getBoolean("recurringReminder", true))

    // Secure App Lock & Biometrics states
    val appLock = MutableStateFlow(secureStorage.isAppLockEnabled)
    val biometricAuth = MutableStateFlow(secureStorage.isBiometricEnabled)
    val lockTimeout = MutableStateFlow(secureStorage.lockTimeoutMs)
    val hideAppPreview = MutableStateFlow(secureStorage.hideAppPreview)
    val appIcon = MutableStateFlow(sharedPrefs.getString("appIcon", "default") ?: "default")
    val biometricSupport = MutableStateFlow(BiometricSupportType.UNAVAILABLE)

    val isProUser = MutableStateFlow(sharedPrefs.getBoolean("isProUser", false))
    val userName = MutableStateFlow(sharedPrefs.getString("userName", "User") ?: "User")
    val userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "") ?: "")
    val isFirstLaunch = MutableStateFlow(sharedPrefs.getBoolean("isFirstLaunch", true))

    // High Fidelity Personalization parameters
    val accentColor = MutableStateFlow(sharedPrefs.getString("accentColor", "rose") ?: "rose")
    val appStyle = MutableStateFlow(sharedPrefs.getString("appStyle", "comfortable") ?: "comfortable")
    val startScreen = MutableStateFlow(sharedPrefs.getString("startScreen", "dashboard") ?: "dashboard")
    val weeklySummaryReminder = MutableStateFlow(sharedPrefs.getBoolean("weeklySummaryReminder", true))
    val monthlySummaryReminder = MutableStateFlow(sharedPrefs.getBoolean("monthlySummaryReminder", true))

    val categories: StateFlow<List<Category>> = transactionRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Detect biometric hardware capabilities
        val biometricManager = BiometricManager.from(application)
        val status = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        biometricSupport.value = when (status) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricSupportType.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricSupportType.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricSupportType.UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricSupportType.NONE_ENROLLED
            else -> BiometricSupportType.UNAVAILABLE
        }
    }

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
        updateNotificationSchedule()
    }

    fun saveMissedEntryReminder(newValue: Boolean) {
        missedEntryReminder.value = newValue
        sharedPrefs.edit().putBoolean("missedEntryReminder", newValue).apply()
        updateNotificationSchedule()
    }

    fun saveBudgetReminder(newValue: Boolean) {
        budgetReminder.value = newValue
        sharedPrefs.edit().putBoolean("budgetReminder", newValue).apply()
        updateNotificationSchedule()
    }

    fun saveRecurringReminder(newValue: Boolean) {
        recurringReminder.value = newValue
        sharedPrefs.edit().putBoolean("recurringReminder", newValue).apply()
        updateNotificationSchedule()
    }

    fun saveAppLock(newValue: Boolean) {
        secureStorage.isAppLockEnabled = newValue
        appLock.value = newValue
    }

    fun saveBiometricAuth(newValue: Boolean) {
        secureStorage.isBiometricEnabled = newValue
        biometricAuth.value = newValue
    }

    fun saveLockTimeout(newValue: Long) {
        secureStorage.lockTimeoutMs = newValue
        lockTimeout.value = newValue
    }

    fun saveHideAppPreview(newValue: Boolean) {
        secureStorage.hideAppPreview = newValue
        hideAppPreview.value = newValue
        // Notify window parameter update immediately
        com.vesper.ledger.PreviewProtectionNotifier.notifyChanged()
    }

    fun saveAppIcon(newValue: String) {
        appIcon.value = newValue
        sharedPrefs.edit().putString("appIcon", newValue).apply()
        com.vesper.ledger.data.secure.AppIconManager.setAppIcon(getApplication(), newValue)
    }

    fun saveIsProUser(newValue: Boolean) {
        isProUser.value = newValue
        sharedPrefs.edit().putBoolean("isProUser", newValue).apply()
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

    fun saveWeeklySummaryReminder(newValue: Boolean) {
        weeklySummaryReminder.value = newValue
        sharedPrefs.edit().putBoolean("weeklySummaryReminder", newValue).apply()
        updateNotificationSchedule()
    }

    fun saveMonthlySummaryReminder(newValue: Boolean) {
        monthlySummaryReminder.value = newValue
        sharedPrefs.edit().putBoolean("monthlySummaryReminder", newValue).apply()
        updateNotificationSchedule()
    }

    private fun updateNotificationSchedule() {
        val anyEnabled = dailyReminder.value || 
                         missedEntryReminder.value || 
                         budgetReminder.value || 
                         recurringReminder.value || 
                         weeklySummaryReminder.value || 
                         monthlySummaryReminder.value

        val context = getApplication<Application>()
        if (anyEnabled) {
            val workManager = androidx.work.WorkManager.getInstance(context)
            val request = androidx.work.PeriodicWorkRequestBuilder<com.vesper.ledger.data.notification.IntelligentNotificationWorker>(
                4, java.util.concurrent.TimeUnit.HOURS
            ).setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            ).build()
            
            workManager.enqueueUniquePeriodicWork(
                "vesper_intelligent_notifications",
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            android.util.Log.d("SettingsViewModel", "Enqueued intelligent notifications worker.")
        } else {
            val workManager = androidx.work.WorkManager.getInstance(context)
            workManager.cancelUniqueWork("vesper_intelligent_notifications")
            com.vesper.ledger.data.notification.NotificationHelper.cancelAllBudgetAlerts(context)
            android.util.Log.d("SettingsViewModel", "Cancelled intelligent notifications worker.")
        }
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
