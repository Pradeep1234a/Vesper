package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
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
import com.vesper.ledger.ui.category.AddEditCategoryScreen
import com.vesper.ledger.ui.category.CategoryViewModel
import com.vesper.ledger.ui.category.CategoryViewModelFactory
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.ui.accounts.AccountsScreen
import com.vesper.ledger.ui.accounts.AddEditAccountScreen
import com.vesper.ledger.ui.transactions.AddTransactionScreen
import com.vesper.ledger.ui.budget.AddEditBudgetScreen
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.ui.auth.WelcomeScreen
import com.vesper.ledger.ui.auth.SignInScreen
import com.vesper.ledger.ui.auth.CreateAccountScreen
import com.vesper.ledger.ui.auth.ForgotPasswordScreen
import com.vesper.ledger.ui.settings.CurrencySelectorScreen
import com.vesper.ledger.ui.settings.CurrencyFlowMode
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
    val accounts by app.accountRepository.allAccounts.collectAsState(initial = emptyList())
    val paymentMethods by app.accountRepository.allPaymentMethods.collectAsState(initial = emptyList())
    val transactions by app.transactionRepository.allTransactions.collectAsState(initial = emptyList())
    val categories by app.transactionRepository.allCategories.collectAsState(initial = emptyList())

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
            val scope = rememberCoroutineScope()
            MainScreen(
                settingsViewModel = settingsViewModel,
                updateViewModel = updateViewModel,
                onSavingsClick = { navController.navigate(Screen.Savings.route) },
                onCategoryManagementClick = { navController.navigate("categories") },
                onAccountsClick = { navController.navigate("accounts") },
                onAddTransactionClick = { navController.navigate("add_transaction") },
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
            var editingCategoryState by remember { mutableStateOf<Category?>(null) }
            var isAddingCategory by remember { mutableStateOf(false) }

            if (isAddingCategory || editingCategoryState != null) {
                AddEditCategoryScreen(
                    categoryToEdit = editingCategoryState,
                    onBackClick = {
                        isAddingCategory = false
                        editingCategoryState = null
                    },
                    onSaveCategory = { name, iconName, type, colorHex, idToUpdate ->
                        if (idToUpdate != null) {
                            categoryViewModel.updateCategory(
                                Category(id = idToUpdate, name = name, iconName = iconName, type = type, colorHex = colorHex)
                            )
                        } else {
                            categoryViewModel.addCategory(name, iconName, type, colorHex)
                        }
                    },
                    onDeleteCategory = { cat ->
                        categoryViewModel.deleteCategory(cat)
                    }
                )
            } else {
                CategoriesScreen(
                    viewModel = categoryViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddCategoryClick = { isAddingCategory = true },
                    onEditCategoryClick = { cat -> editingCategoryState = cat }
                )
            }
        }

        composable("accounts") {
            val scope = rememberCoroutineScope()
            var editingAccountState by remember { mutableStateOf<Account?>(null) }
            var isAddingAccount by remember { mutableStateOf(false) }

            if (isAddingAccount || editingAccountState != null) {
                AddEditAccountScreen(
                    accountToEdit = editingAccountState,
                    currencySymbol = currencySymbol,
                    onBackClick = {
                        isAddingAccount = false
                        editingAccountState = null
                    },
                    onSaveAccount = { name, type, initialBalance, iconName, notes, isHidden, idToUpdate ->
                        scope.launch(Dispatchers.IO) {
                            if (idToUpdate != null) {
                                app.accountRepository.updateAccount(
                                    Account(
                                        id = idToUpdate,
                                        name = name,
                                        type = type,
                                        initialBalance = initialBalance,
                                        currency = "USD",
                                        bankInfo = null,
                                        notes = notes,
                                        iconName = iconName,
                                        isHidden = isHidden
                                    )
                                )
                            } else {
                                app.accountRepository.insertAccount(
                                    Account(
                                        name = name,
                                        type = type,
                                        initialBalance = initialBalance,
                                        currency = "USD",
                                        bankInfo = null,
                                        notes = notes,
                                        iconName = iconName,
                                        isHidden = isHidden
                                    )
                                )
                            }
                        }
                    },
                    onDeleteAccount = { acct ->
                        scope.launch(Dispatchers.IO) {
                            app.accountRepository.deleteAccount(acct)
                        }
                    }
                )
            } else {
                AccountsScreen(
                    accounts = accounts,
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    onBackClick = { navController.popBackStack() },
                    onAddAccountClick = { isAddingAccount = true },
                    onEditAccountClick = { acct -> editingAccountState = acct },
                    onToggleHideAccount = { acct ->
                        scope.launch(Dispatchers.IO) {
                            app.accountRepository.updateAccount(acct.copy(isHidden = !acct.isHidden))
                        }
                    }
                )
            }
        }

        composable("add_transaction") {
            val scope = rememberCoroutineScope()
            AddTransactionScreen(
                currencySymbol = currencySymbol,
                categories = categories,
                accounts = accounts,
                paymentMethods = paymentMethods,
                onBackClick = { navController.popBackStack() },
                onAddCategoryClick = { navController.navigate("categories") },
                onAddAccountClick = { navController.navigate("accounts") },
                onSaveTransaction = { title, amount, type, categoryId, accountId, accountName, paymentMethod, dateEpochMillis, note ->
                    scope.launch(Dispatchers.IO) {
                        app.transactionRepository.insertTransaction(
                            com.vesper.ledger.data.model.Transaction(
                                title = title.ifBlank { if (type == TransactionType.INCOME) "Income" else "Expense" },
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                accountId = accountId,
                                accountName = accountName,
                                paymentMethod = paymentMethod,
                                dateEpochMillis = dateEpochMillis,
                                note = note
                            )
                        )
                    }
                }
            )
        }

        composable(Screen.AddBudget.route) {
            val scope = rememberCoroutineScope()
            var editingBudgetState by remember { mutableStateOf<Budget?>(null) }

            AddEditBudgetScreen(
                budgetToEdit = editingBudgetState,
                categories = categories,
                currencySymbol = currencySymbol,
                onBackClick = { navController.popBackStack() },
                onSaveBudget = { name, amount, period, categoryId, startDate, endDate, notes, idToUpdate ->
                    scope.launch(Dispatchers.IO) {
                        if (idToUpdate != null) {
                            app.budgetRepository.updateBudget(
                                Budget(
                                    id = idToUpdate,
                                    name = name,
                                    amount = amount,
                                    period = period,
                                    categoryId = categoryId,
                                    startDate = startDate,
                                    endDate = endDate,
                                    notes = notes
                                )
                            )
                        } else {
                            app.budgetRepository.insertBudget(
                                Budget(
                                    name = name,
                                    amount = amount,
                                    period = period,
                                    categoryId = categoryId,
                                    startDate = startDate,
                                    endDate = endDate,
                                    notes = notes
                                )
                            )
                        }
                    }
                },
                onDeleteBudget = { budget ->
                    scope.launch(Dispatchers.IO) {
                        app.budgetRepository.deleteBudget(budget)
                    }
                }
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
                                navController.navigate("onboarding_currency") {
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

        composable("onboarding_currency") {
            CurrencySelectorScreen(
                viewModel = settingsViewModel,
                flowMode = CurrencyFlowMode.ONBOARDING,
                onCompleteOnboarding = {
                    navController.navigate("main_screen") {
                        popUpTo(Screen.AuthWelcome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
