package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
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
import com.vesper.ledger.ui.category.CategoriesScreen
import com.vesper.ledger.ui.category.AddCategoryScreen
import com.vesper.ledger.ui.category.CategoryViewModel
import com.vesper.ledger.ui.category.CategoryViewModelFactory
import com.vesper.ledger.ui.auth.WelcomeScreen
import com.vesper.ledger.ui.auth.SignInScreen
import com.vesper.ledger.ui.auth.CreateAccountScreen
import com.vesper.ledger.ui.auth.ForgotPasswordScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val addTransactionFactory = AddTransactionViewModelFactory(app.transactionRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val categoryFactory = CategoryViewModelFactory(app, app.transactionRepository)
    val categoryViewModel: CategoryViewModel = viewModel(factory = categoryFactory)

    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("main_screen") {
            MainScreen(
                settingsViewModel = settingsViewModel,
                updateViewModel = updateViewModel,
                onAddTransactionClick = { type, id -> navController.navigate(Screen.AddTransaction.createRoute(type ?: "EXPENSE", id)) },
                onSavingsClick = { navController.navigate(Screen.Savings.route) },
                onCategoryManagementClick = { navController.navigate("categories") }
            )
        }

        composable("categories") {
            CategoriesScreen(
                viewModel = categoryViewModel,
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { id ->
                    if (id != null) {
                        navController.navigate("add_category?id=$id")
                    } else {
                        navController.navigate("add_category")
                    }
                }
            )
        }

        composable(
            route = "add_category?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val catId = backStackEntry.arguments?.getLong("id") ?: -1L
            val finalId = if (catId == -1L) null else catId
            AddCategoryScreen(
                viewModel = categoryViewModel,
                categoryId = finalId,
                onBackClick = { navController.popBackStack() }
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

        // ─── Authentication Flow ─────────────────────────────────────────

        composable(Screen.AuthWelcome.route) {
            WelcomeScreen(
                onCreateAccountClick = { navController.navigate(Screen.AuthCreateAccount.route) },
                onSignInClick = { navController.navigate(Screen.AuthSignIn.route) }
            )
        }

        composable(Screen.AuthSignIn.route) {
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = {
                    // Mark user as authenticated
                    val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("isAuthenticated", true).apply()
                    navController.navigate("main_screen") {
                        popUpTo(Screen.AuthWelcome.route) { inclusive = true }
                    }
                },
                onForgotPasswordClick = { navController.navigate(Screen.AuthForgotPassword.route) },
                onCreateAccountClick = {
                    navController.navigate(Screen.AuthCreateAccount.route) {
                        popUpTo(Screen.AuthSignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AuthCreateAccount.route) {
            CreateAccountScreen(
                onBackClick = { navController.popBackStack() },
                onCreateAccountClick = {
                    // Mark user as authenticated
                    val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("isAuthenticated", true).apply()
                    navController.navigate("main_screen") {
                        popUpTo(Screen.AuthWelcome.route) { inclusive = true }
                    }
                },
                onSignInClick = {
                    navController.navigate(Screen.AuthSignIn.route) {
                        popUpTo(Screen.AuthCreateAccount.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AuthForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendResetLinkClick = {
                    // Simulate sending reset link — navigate back to sign in
                    navController.popBackStack()
                },
                onBackToSignInClick = { navController.popBackStack() }
            )
        }
    }
}
