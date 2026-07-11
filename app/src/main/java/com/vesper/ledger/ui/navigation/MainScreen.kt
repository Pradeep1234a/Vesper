package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.ui.dashboard.DashboardScreen
import com.vesper.ledger.ui.dashboard.DashboardViewModel
import com.vesper.ledger.ui.dashboard.DashboardViewModelFactory
import com.vesper.ledger.ui.reports.ReportsScreen
import com.vesper.ledger.ui.reports.ReportsViewModel
import com.vesper.ledger.ui.reports.ReportsViewModelFactory
import com.vesper.ledger.ui.settings.SettingsScreen
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.SettingsViewModelFactory
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    onAddTransactionClick: () -> Unit,
    onSavingsClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val dashboardFactory = DashboardViewModelFactory(app.transactionRepository, app.savingsRepository)
    val transactionsFactory = TransactionsViewModelFactory(app.transactionRepository)
    val reportsFactory = ReportsViewModelFactory(app.transactionRepository)

    val currencySymbol by settingsViewModel.currency.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp,
                modifier = Modifier.height(72.dp)
            ) {
                NavigationBarItem(
                    selected = currentRoute == Screen.Dashboard.route,
                    onClick = {
                        if (currentRoute != Screen.Dashboard.route) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Dashboard.route) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Transactions.route,
                    onClick = {
                        if (currentRoute != Screen.Transactions.route) {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Transactions.route) Icons.Filled.ListAlt else Icons.Outlined.ListAlt,
                            contentDescription = "Transactions"
                        )
                    },
                    label = { Text("Transactions", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.width(72.dp))

                NavigationBarItem(
                    selected = currentRoute == Screen.Reports.route,
                    onClick = {
                        if (currentRoute != Screen.Reports.route) {
                            navController.navigate(Screen.Reports.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Reports.route) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Reports"
                        )
                    },
                    label = { Text("Reports", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.Settings.route,
                    onClick = {
                        if (currentRoute != Screen.Settings.route) {
                            navController.navigate(Screen.Settings.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentRoute == Screen.Settings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        selectedTextColor = MaterialTheme.colorScheme.onBackground,
                        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .offset(y = 36.dp)
                    .size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
                    onAddTransactionClick = onAddTransactionClick,
                    onSeeAllTransactionsClick = {
                        navController.navigate(Screen.Transactions.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSavingsClick = onSavingsClick
                )
            }

            composable(Screen.Transactions.route) {
                val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                TransactionsScreen(
                    viewModel = transactionsViewModel,
                    currencySymbol = currencySymbol,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel(factory = reportsFactory)
                ReportsScreen(
                    viewModel = reportsViewModel,
                    currencySymbol = currencySymbol
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
