package com.vesper.ledger

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vesper.ledger.data.secure.SecureStorageHelper
import com.vesper.ledger.data.secure.SensitiveActionAuthenticator
import com.vesper.ledger.data.update.UpdateType
import com.vesper.ledger.ui.navigation.NavGraph
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.theme.VesperLedgerTheme
import com.vesper.ledger.ui.update.UpdateBottomSheet
import com.vesper.ledger.ui.update.UpdateDialog
import com.vesper.ledger.ui.update.UpdateViewModel
import com.vesper.ledger.ui.update.UpdateViewModelFactory

object PreviewProtectionNotifier {
    private var callback: (() -> Unit)? = null
    fun setCallback(cb: () -> Unit) { callback = cb }
    fun clearCallback() { callback = null }
    fun notifyChanged() { callback?.invoke() }
}

class MainActivity : FragmentActivity() {

    private val isAppLockedState = mutableStateOf(false)
    private var lastActiveTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register preview protection callback
        PreviewProtectionNotifier.setCallback {
            updateWindowSecureFlag()
        }
        updateWindowSecureFlag()

        // Register sensitive action authenticator
        SensitiveActionAuthenticator.setCallback { onSuccess ->
            triggerSensitiveActionAuth(onSuccess)
        }

        // Lock App initially on launch if App Lock is enabled
        val helper = SecureStorageHelper.getInstance(this)
        if (helper.isAppLockEnabled) {
            isAppLockedState.value = true
            triggerBiometricUnlock()
        }

        setContent {
            val app = applicationContext as VesperApplication
            
            // ViewModels
            val settingsFactory = SettingsViewModelFactory(app, app.transactionRepository)
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            
            val updateFactory = UpdateViewModelFactory(app, app.updateRepository)
            val updateViewModel: UpdateViewModel = viewModel(factory = updateFactory)
            
            val themeState by settingsViewModel.theme.collectAsState()
            val accentColorState by settingsViewModel.accentColor.collectAsState()

            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            VesperLedgerTheme(darkTheme = darkTheme, accentColor = accentColorState) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isAppLocked by isAppLockedState

                    if (isAppLocked) {
                        // Premium visual-blocking App Lock Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "App Locked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                Text(
                                    text = "Vesper Ledger Locked",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Confirm identity to access your ledger",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(48.dp))
                                
                                Button(
                                    onClick = { triggerBiometricUnlock() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text(
                                        text = "Tap to Unlock",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            settingsViewModel = settingsViewModel,
                            updateViewModel = updateViewModel
                        )
                    }
                }

                // Global Update Dialog/BottomSheet overlay
                val updateUiState by updateViewModel.uiState.collectAsState()
                if (updateUiState.showUpdateDialog && updateUiState.updateInfo != null) {
                    val info = updateUiState.updateInfo!!
                    if (!info.updateAvailable) {
                        UpdateDialog(
                            updateInfo = info,
                            downloadState = updateUiState.downloadState,
                            progress = updateUiState.downloadProgress,
                            onDownloadClick = { updateViewModel.startDownload() },
                            onInstallClick = { updateViewModel.installUpdate() },
                            onLaterClick = { updateViewModel.pressLater() },
                            onDismissRequest = { updateViewModel.dismissUpdateDialog() }
                        )
                    } else if (info.updateType == UpdateType.STABILITY || info.updateType == UpdateType.HOTFIX) {
                        UpdateBottomSheet(
                            updateInfo = info,
                            downloadState = updateUiState.downloadState,
                            progress = updateUiState.downloadProgress,
                            onDownloadClick = { updateViewModel.startDownload() },
                            onInstallClick = { updateViewModel.installUpdate() },
                            onDismissRequest = { updateViewModel.dismissUpdateDialog() }
                        )
                    } else {
                        UpdateDialog(
                            updateInfo = info,
                            downloadState = updateUiState.downloadState,
                            progress = updateUiState.downloadProgress,
                            onDownloadClick = { updateViewModel.startDownload() },
                            onInstallClick = { updateViewModel.installUpdate() },
                            onLaterClick = { updateViewModel.pressLater() },
                            onDismissRequest = { updateViewModel.dismissUpdateDialog() }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val helper = SecureStorageHelper.getInstance(this)
        if (helper.isAppLockEnabled) {
            val elapsed = System.currentTimeMillis() - lastActiveTime
            val timeout = helper.lockTimeoutMs
            // If timeout is -1L (Never), we do not lock on background returns.
            // If timeout is 0L (Immediately), we lock immediately.
            if (timeout >= 0L && elapsed > timeout) {
                isAppLockedState.value = true
                triggerBiometricUnlock()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lastActiveTime = System.currentTimeMillis()
    }

    override fun onDestroy() {
        super.onDestroy()
        PreviewProtectionNotifier.clearCallback()
        SensitiveActionAuthenticator.clearCallback()
    }

    private fun triggerBiometricUnlock() {
        val helper = SecureStorageHelper.getInstance(this)
        if (!helper.isAppLockEnabled) {
            isAppLockedState.value = false
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // Authentication failed or cancelled -> keep app locked.
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                isAppLockedState.value = false
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vesper Ledger")
            .setSubtitle("Confirm identity to unlock the application")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to authenticate biometrics", e)
            // If no secure lock screen enrolled, bypass lock to prevent locking user out
            isAppLockedState.value = false
        }
    }

    private fun triggerSensitiveActionAuth(onSuccess: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Confirm credentials to authorize this action")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        try {
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to authenticate sensitive action", e)
            // If lock screen not configured, allow access
            onSuccess()
        }
    }

    private fun updateWindowSecureFlag() {
        val helper = SecureStorageHelper.getInstance(this)
        if (helper.hideAppPreview) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
