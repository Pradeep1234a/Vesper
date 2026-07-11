package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.ui.addtransaction.AddTransactionScreen
import com.vesper.ledger.ui.addtransaction.AddTransactionViewModel
import com.vesper.ledger.ui.addtransaction.AddTransactionViewModelFactory
import com.vesper.ledger.ui.dashboard.DashboardScreen
import com.vesper.ledger.ui.dashboard.DashboardViewModel
import com.vesper.ledger.ui.dashboard.DashboardViewModelFactory
import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import com.vesper.ledger.ui.settings.SettingsScreen
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val dashboardFactory = DashboardViewModelFactory(app.transactionRepository, app.savingsRepository)
    val transactionsFactory = TransactionsViewModelFactory(app.transactionRepository)
    val addTransactionFactory = AddTransactionViewModelFactory(app.transactionRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val settingsFactory = SettingsViewModelFactory(app, app.transactionRepository)

    val settingsViewModel: SettingsViewModel = viewModel(factory = settingsFactory)
    val currencySymbol by settingsViewModel.currency.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                currencySymbol = currencySymbol,
                onAddTransactionClick = { navController.navigate(Screen.AddTransaction.route) },
                onSeeAllTransactionsClick = { navController.navigate(Screen.Transactions.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onSavingsClick = { navController.navigate(Screen.Savings.route) }
            )
        }

        composable(Screen.Transactions.route) {
            val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
            TransactionsScreen(
                viewModel = transactionsViewModel,
                currencySymbol = currencySymbol,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddTransaction.route) {
            val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = addTransactionFactory)
            AddTransactionScreen(
                viewModel = addTransactionViewModel,
                currencySymbol = currencySymbol,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Savings.route) {
            val savingsViewModel: SavingsViewModel = viewModel(factory = savingsFactory)
            SavingsScreen(
                viewModel = savingsViewModel,
                currencySymbol = currencySymbol,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
