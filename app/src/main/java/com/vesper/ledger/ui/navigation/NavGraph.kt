package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
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
import com.vesper.ledger.ui.auth.AuthScreen
import com.vesper.ledger.ui.auth.AuthViewModel
import com.vesper.ledger.ui.auth.AuthViewModelFactory
import com.vesper.ledger.ui.components.CurrencySelectorMode
import com.vesper.ledger.ui.components.CurrencySelectorScreen

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
    val authFactory = AuthViewModelFactory(app, app.database.userDao())

    val authViewModel: AuthViewModel = viewModel(factory = authFactory)

    val currencySymbol by settingsViewModel.currency.collectAsState()
    val isFirstLaunch by settingsViewModel.isFirstLaunch.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier.fillMaxSize()
    ) {
        composable("splash") {
            val sessionActive by authViewModel.sessionActive.collectAsState()
            SplashScreen(
                isFirstLaunch = isFirstLaunch,
                isSessionActive = sessionActive,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateNext = {
                    navController.navigate("auth") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("auth") {
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { isNewUser ->
                    if (isNewUser) {
                        navController.navigate("personalization") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main_screen") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                },
                onContinueAsGuest = {
                    navController.navigate("main_screen") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("personalization") {
            PersonalizationScreen(
                viewModel = settingsViewModel,
                onSetupComplete = {
                    navController.navigate("main_screen") {
                        popUpTo("personalization") { inclusive = true }
                    }
                }
            )
        }

        composable("settings_currency") {
            val currentCurrency by settingsViewModel.currency.collectAsState()
            CurrencySelectorScreen(
                mode = CurrencySelectorMode.SETTINGS,
                currentSelection = currentCurrency,
                onCurrencySelected = { code ->
                    settingsViewModel.saveCurrency(code)
                },
                onContinueClick = {},
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("main_screen") {
            MainScreen(
                settingsViewModel = settingsViewModel,
                onAddTransactionClick = { type, id -> navController.navigate(Screen.AddTransaction.createRoute(type ?: "EXPENSE", id)) },
                onSavingsClick = { navController.navigate(Screen.Savings.route) },
                onCurrencyClick = { navController.navigate("settings_currency") }
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type")
            val txId = backStackEntry.arguments?.getLong("id") ?: -1L
            val initialType = when (typeStr) {
                "INCOME" -> TransactionType.INCOME
                "EXPENSE" -> TransactionType.EXPENSE
                else -> null
            }
            val addTransactionViewModel: AddTransactionViewModel = viewModel(factory = addTransactionFactory)
            
            // Load the transaction for editing or reset state if new
            LaunchedEffect(txId) {
                addTransactionViewModel.loadTransaction(txId, initialType)
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
