package com.vesper.ledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.vesper.ledger.data.update.UpdateType
import com.vesper.ledger.ui.navigation.NavGraph
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.theme.VesperLedgerTheme
import com.vesper.ledger.ui.update.UpdateBottomSheet
import com.vesper.ledger.ui.update.UpdateDialog
import com.vesper.ledger.ui.update.UpdateViewModel
import com.vesper.ledger.ui.update.UpdateViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        settingsViewModel = settingsViewModel,
                        updateViewModel = updateViewModel
                    )
                }

                // Global Update Dialog/BottomSheet overlay
                val updateUiState by updateViewModel.uiState.collectAsState()
                if (updateUiState.showUpdateDialog && updateUiState.updateInfo != null) {
                    val info = updateUiState.updateInfo!!
                    if (info.updateType == UpdateType.STABILITY || info.updateType == UpdateType.HOTFIX) {
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
}
