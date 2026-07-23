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

import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.category.CategoriesScreen
import com.vesper.ledger.ui.category.CategoryViewModel
import com.vesper.ledger.ui.category.CategoryViewModelFactory
import com.vesper.ledger.ui.auth.WelcomeScreen
import com.vesper.ledger.ui.auth.SignInScreen
import com.vesper.ledger.ui.auth.CreateAccountScreen
import com.vesper.ledger.ui.auth.ForgotPasswordScreen
import com.vesper.ledger.data.util.PasswordHasher
import com.vesper.ledger.data.model.UserAccount
import com.vesper.ledger.data.local.AppDatabase
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

@Composable
fun NavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val categoryFactory = CategoryViewModelFactory(app, app.transactionRepository)
    val categoryViewModel: CategoryViewModel = viewModel(factory = categoryFactory)

    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier.fillMaxSize(),
        enterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) + androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(220)) { it / 6 } },
        exitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(220)) + androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(220)) { -it / 6 } },
        popEnterTransition = { androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(220)) + androidx.compose.animation.slideInHorizontally(androidx.compose.animation.core.tween(220)) { -it / 6 } },
        popExitTransition = { androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(220)) + androidx.compose.animation.slideOutHorizontally(androidx.compose.animation.core.tween(220)) { it / 6 } }
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
            val scope = rememberCoroutineScope()
            MainScreen(
                settingsViewModel = settingsViewModel,
                updateViewModel = updateViewModel,
                onSavingsClick = { navController.navigate(Screen.Savings.route) },
                onCategoryManagementClick = { navController.navigate("categories") },
                onSignOutClick = {
                    scope.launch(Dispatchers.IO) {
                        val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                        sharedPrefs.edit()
                            .putBoolean("isAuthenticated", false)
                            .putString("user_email", "")
                            .putString("userName", "User")
                            .commit() // Commit synchronously

                        // Reset Viewmodel state values
                        settingsViewModel.userName.value = "User"
                        settingsViewModel.userEmail.value = ""

                        // Close active user database connection and reset Application caches
                        app.clearDatabaseCaches()

                        withContext(Dispatchers.Main) {
                            navController.navigate(Screen.AuthWelcome.route) {
                                popUpTo("main_screen") { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        composable("categories") {
            CategoriesScreen(
                viewModel = categoryViewModel,
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
            val scope = rememberCoroutineScope()
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { emailInput, passwordInput, onCallback ->
                    scope.launch(Dispatchers.IO) {
                        val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                        val oldEmail = sharedPrefs.getString("user_email", "") ?: ""

                        // Switch database temporarily to target user to query their credentials
                        sharedPrefs.edit().putString("user_email", emailInput).commit()
                        val userDb = AppDatabase.getDatabase(context)
                        val user = userDb.userDao().getUserByEmail(emailInput)

                        if (user == null) {
                            // Revert back
                            sharedPrefs.edit().putString("user_email", oldEmail).commit()
                            withContext(Dispatchers.Main) {
                                onCallback("User account not found.")
                            }
                        } else {
                            val isValid = PasswordHasher.verifyPassword(passwordInput, user.salt, user.passwordHash)
                            if (isValid) {
                                sharedPrefs.edit()
                                    .putBoolean("isAuthenticated", true)
                                    .putString("user_email", emailInput)
                                    .putString("userName", user.fullName)
                                    .commit()

                                settingsViewModel.userName.value = user.fullName
                                settingsViewModel.userEmail.value = emailInput

                                // Clear local caches
                                app.clearDatabaseCaches()

                                withContext(Dispatchers.Main) {
                                    onCallback(null)
                                    navController.navigate("main_screen") {
                                        popUpTo(Screen.AuthWelcome.route) { inclusive = true }
                                    }
                                }
                            } else {
                                sharedPrefs.edit().putString("user_email", oldEmail).commit()
                                withContext(Dispatchers.Main) {
                                    onCallback("Incorrect password.")
                                }
                            }
                        }
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
            val scope = rememberCoroutineScope()
            CreateAccountScreen(
                onBackClick = { navController.popBackStack() },
                onCreateAccountClick = { fullNameInput, emailInput, passwordInput, onCallback ->
                    scope.launch(Dispatchers.IO) {
                        val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                        val oldEmail = sharedPrefs.getString("user_email", "") ?: ""

                        // Switch database temporarily to this email to check if database already exists
                        sharedPrefs.edit().putString("user_email", emailInput).commit()
                        val userDb = AppDatabase.getDatabase(context)
                        val existingUser = userDb.userDao().getUserByEmail(emailInput)

                        if (existingUser != null) {
                            sharedPrefs.edit().putString("user_email", oldEmail).commit()
                            withContext(Dispatchers.Main) {
                                onCallback("An account with this email already exists.")
                            }
                        } else {
                            val salt = PasswordHasher.generateSalt()
                            val hash = PasswordHasher.hashPassword(passwordInput, salt)
                            val newUser = UserAccount(
                                email = emailInput,
                                fullName = fullNameInput,
                                passwordHash = hash,
                                salt = salt
                            )
                            userDb.userDao().insertUser(newUser)

                            sharedPrefs.edit()
                                .putBoolean("isAuthenticated", true)
                                .putString("user_email", emailInput)
                                .putString("userName", fullNameInput)
                                .commit()

                            settingsViewModel.userName.value = fullNameInput
                            settingsViewModel.userEmail.value = emailInput

                            app.clearDatabaseCaches()

                            withContext(Dispatchers.Main) {
                                onCallback(null)
                                navController.navigate("main_screen") {
                                    popUpTo(Screen.AuthWelcome.route) { inclusive = true }
                                }
                            }
                        }
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
            val scope = rememberCoroutineScope()
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendResetLinkClick = { emailInput, newPasswordInput, onCallback ->
                    scope.launch(Dispatchers.IO) {
                        val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                        val oldEmail = sharedPrefs.getString("user_email", "") ?: ""

                        sharedPrefs.edit().putString("user_email", emailInput).commit()
                        val userDb = AppDatabase.getDatabase(context)
                        val user = userDb.userDao().getUserByEmail(emailInput)

                        if (user == null) {
                            sharedPrefs.edit().putString("user_email", oldEmail).commit()
                            withContext(Dispatchers.Main) {
                                onCallback("User account not found.")
                            }
                        } else {
                            val newSalt = PasswordHasher.generateSalt()
                            val newHash = PasswordHasher.hashPassword(newPasswordInput, newSalt)
                            userDb.userDao().updatePassword(emailInput, newHash, newSalt)

                            sharedPrefs.edit().putString("user_email", oldEmail).commit()
                            withContext(Dispatchers.Main) {
                                onCallback(null)
                            }
                        }
                    }
                },
                onBackToSignInClick = { navController.popBackStack() }
            )
        }
    }
}
