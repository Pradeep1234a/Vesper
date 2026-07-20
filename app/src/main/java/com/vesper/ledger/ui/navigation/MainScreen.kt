package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.dashboard.DashboardScreen
import com.vesper.ledger.ui.dashboard.DashboardViewModel
import com.vesper.ledger.ui.dashboard.DashboardViewModelFactory
import com.vesper.ledger.ui.reports.ReportsScreen
import com.vesper.ledger.ui.reports.ReportsViewModel
import com.vesper.ledger.ui.reports.ReportsViewModelFactory
import com.vesper.ledger.ui.settings.SettingsScreen
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory
import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import com.vesper.ledger.ui.account.AccountsScreen
import com.vesper.ledger.ui.account.AccountsViewModel
import com.vesper.ledger.ui.account.AccountsViewModelFactory
import com.vesper.ledger.ui.budget.BudgetScreen
import com.vesper.ledger.ui.budget.BudgetsViewModel
import com.vesper.ledger.ui.budget.BudgetsViewModelFactory
import com.vesper.ledger.ui.recurring.RecurringScreen
import com.vesper.ledger.ui.recurring.RecurringViewModel
import com.vesper.ledger.ui.recurring.RecurringViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    onAddTransactionClick: (type: String?, id: Long?) -> Unit,
    onSavingsClick: () -> Unit,
    onCategoryManagementClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val dashboardFactory = DashboardViewModelFactory(app.transactionRepository, app.savingsRepository, app.accountRepository)
    val transactionsFactory = TransactionsViewModelFactory(app.transactionRepository)
    val reportsFactory = ReportsViewModelFactory(app.transactionRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val accountsFactory = AccountsViewModelFactory(app)
    val budgetsFactory = BudgetsViewModelFactory(app)
    val recurringFactory = RecurringViewModelFactory(app)

    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()
    val userName by settingsViewModel.userName.collectAsState()

    // Bottom navigation bar tabs
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        BottomNavItem(Screen.Transactions.route, "Transactions", Icons.Outlined.ListAlt, Icons.Filled.ListAlt),
        BottomNavItem(Screen.Reports.route, "Analytics", Icons.Outlined.BarChart, Icons.Filled.BarChart),
        BottomNavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    currencySymbol = currencySymbol,
                    userName = userName,
                    onAddTransactionClick = onAddTransactionClick,
                    onSeeAllTransactionsClick = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSavingsClick = {
                        navController.navigate(Screen.Savings.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onReportsClick = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Transactions.route) {
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                TransactionsScreen(
                    viewModel = transactionsViewModel,
                    currencySymbol = currencySymbol,
                    onBackClick = { navController.popBackStack() },
                    onAddTransactionClick = onAddTransactionClick,
                    onAddCategoryClick = onCategoryManagementClick,
                    onAddAccountClick = {
                        navController.navigate(Screen.Accounts.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.Budgets.route) {
                val budgetsViewModel: BudgetsViewModel = viewModel(factory = budgetsFactory)
                BudgetScreen(
                    viewModel = budgetsViewModel,
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

            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(factory = reportsFactory)
                ReportsScreen(
                    viewModel = reportsViewModel,
                    currencySymbol = currencySymbol
                )
            }

            composable(Screen.Accounts.route) {
                val accountsViewModel: AccountsViewModel = viewModel(factory = accountsFactory)
                AccountsScreen(
                    viewModel = accountsViewModel,
                    currencySymbol = currencySymbol,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Recurring.route) {
                val recurringViewModel: RecurringViewModel = viewModel(factory = recurringFactory)
                RecurringScreen(
                    viewModel = recurringViewModel,
                    currencySymbol = currencySymbol,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    updateViewModel = updateViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCategoriesClick = onCategoryManagementClick,
                    onSignOutClick = onSignOutClick
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)
