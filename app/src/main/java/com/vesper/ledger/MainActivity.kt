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
import com.vesper.ledger.ui.navigation.NavGraph
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.theme.VesperLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = applicationContext as VesperApplication
            val settingsFactory = SettingsViewModelFactory(app, app.transactionRepository)
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
            val themeState by settingsViewModel.theme.collectAsState()

            val darkTheme = when (themeState) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            VesperLedgerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController, settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
