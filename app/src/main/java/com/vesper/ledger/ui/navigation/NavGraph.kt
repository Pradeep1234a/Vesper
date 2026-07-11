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
import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val addTransactionFactory = AddTransactionViewModelFactory(app.transactionRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)

    val currencySymbol by settingsViewModel.currency.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "main_screen",
        modifier = modifier.fillMaxSize()
    ) {
        composable("main_screen") {
            MainScreen(
                settingsViewModel = settingsViewModel,
                onAddTransactionClick = { type -> navController.navigate(Screen.AddTransaction.createRoute(type ?: "EXPENSE")) },
                onSavingsClick = { navController.navigate(Screen.Savings.route) }
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type")
            val initialType = when (typeStr) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> null
            }
            val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = addTransactionFactory)
            if (initialType != null) {
                addTransactionViewModel.type.value = initialType
            }
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
    }
}
