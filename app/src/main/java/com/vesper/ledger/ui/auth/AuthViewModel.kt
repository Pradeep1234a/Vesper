package com.vesper.ledger.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.local.UserDao
import com.vesper.ledger.data.model.UserAccount
import com.vesper.ledger.data.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    application: Application,
    private val userDao: UserDao
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("vesper_auth_session", Context.MODE_PRIVATE)

    val sessionActive = MutableStateFlow(sharedPrefs.getBoolean("sessionActive", false))
    val loggedInEmail = MutableStateFlow(sharedPrefs.getString("loggedInEmail", "") ?: "")
    val loggedInName = MutableStateFlow(sharedPrefs.getString("loggedInName", "") ?: "")

    suspend fun checkEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email.trim()) != null
    }

    fun signUp(
        email: String,
        fullName: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (checkEmailExists(email)) {
                    onResult(false, "An account with this email already exists.")
                    return@launch
                }

                val salt = PasswordHasher.generateSalt()
                val passwordHash = PasswordHasher.hashPassword(password, salt)

                val user = UserAccount(
                    email = email.trim(),
                    fullName = fullName.trim(),
                    passwordHash = passwordHash,
                    salt = salt
                )

                userDao.insertUser(user)
                saveSession(email, fullName)
                onResult(true, "Success")
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email.trim())
            if (user == null) {
                onResult(false, "No account found with this email.")
                return@launch
            }

            val isValid = PasswordHasher.verifyPassword(password, user.salt, user.passwordHash)
            if (isValid) {
                saveSession(email, user.fullName)
                onResult(true, "Success")
            } else {
                onResult(false, "Incorrect password.")
            }
        }
    }

    fun verifyRecovery(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email.trim())
            if (user == null) {
                onResult(false, "No account found with this email.")
            } else {
                onResult(true, "Success")
            }
        }
    }

    fun resetPassword(email: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email.trim())
            if (user == null) {
                onResult(false, "User account not found.")
                return@launch
            }

            val salt = PasswordHasher.generateSalt()
            val newHash = PasswordHasher.hashPassword(newPassword, salt)

            userDao.updatePassword(email.trim(), newHash, salt)
            onResult(true, "Password updated successfully.")
        }
    }

    fun logout() {
        sessionActive.value = false
        loggedInEmail.value = ""
        loggedInName.value = ""
        sharedPrefs.edit()
            .putBoolean("sessionActive", false)
            .putString("loggedInEmail", "")
            .putString("loggedInName", "")
            .apply()
    }

    private fun saveSession(email: String, fullName: String) {
        sessionActive.value = true
        loggedInEmail.value = email
        loggedInName.value = fullName
        sharedPrefs.edit()
            .putBoolean("sessionActive", true)
            .putString("loggedInEmail", email)
            .putString("loggedInName", fullName)
            .apply()
    }
}

class AuthViewModelFactory(
    private val application: Application,
    private val userDao: UserDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(application, userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
