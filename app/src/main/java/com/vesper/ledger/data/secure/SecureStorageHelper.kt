package com.vesper.ledger.data.secure

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorageHelper private constructor(context: Context) {

    private var sharedPrefs: SharedPreferences? = null

    init {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            sharedPrefs = EncryptedSharedPreferences.create(
                "vesper_secure_settings",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("SecureStorageHelper", "EncryptedSharedPreferences initialization failed, falling back to standard prefs", e)
            try {
                // In case of Keystore corruption, clear key and retry or fallback
                context.deleteSharedPreferences("vesper_secure_settings")
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                sharedPrefs = EncryptedSharedPreferences.create(
                    "vesper_secure_settings",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (ex: Exception) {
                Log.e("SecureStorageHelper", "Critical fallback recovery failed, using unencrypted backup", ex)
                sharedPrefs = context.getSharedPreferences("vesper_secure_backup_prefs", Context.MODE_PRIVATE)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SecureStorageHelper? = null

        fun getInstance(context: Context): SecureStorageHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureStorageHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    var isAppLockEnabled: Boolean
        get() = sharedPrefs?.getBoolean("app_lock_enabled", false) ?: false
        set(value) {
            sharedPrefs?.edit()?.putBoolean("app_lock_enabled", value)?.apply()
        }

    var isBiometricEnabled: Boolean
        get() = sharedPrefs?.getBoolean("biometric_enabled", false) ?: false
        set(value) {
            sharedPrefs?.edit()?.putBoolean("biometric_enabled", value)?.apply()
        }

    var lockTimeoutMs: Long
        get() = sharedPrefs?.getLong("lock_timeout_ms", 0L) ?: 0L
        set(value) {
            sharedPrefs?.edit()?.putLong("lock_timeout_ms", value)?.apply()
        }

    var hideAppPreview: Boolean
        get() = sharedPrefs?.getBoolean("hide_app_preview", false) ?: false
        set(value) {
            sharedPrefs?.edit()?.putBoolean("hide_app_preview", value)?.apply()
        }
}
