package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    onAddTransactionClick: (type: String?) -> Unit,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RectangleShape)
                    .navigationBarsPadding()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItem(
                    icon = if (currentRoute == Screen.Dashboard.route) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                    label = "Dashboard",
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
                    modifier = Modifier.weight(1f)
                )

                TabItem(
                    icon = if (currentRoute == Screen.Transactions.route) Icons.Filled.ListAlt else Icons.Outlined.ListAlt,
                    label = "Transactions",
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
                    modifier = Modifier.weight(1f)
                )



                TabItem(
                    icon = if (currentRoute == Screen.Reports.route) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    label = "Reports",
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
                    modifier = Modifier.weight(1f)
                )

                TabItem(
                    icon = if (currentRoute == Screen.Settings.route) Icons.Filled.Settings else Icons.Outlined.Settings,
                    label = "Settings",
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
                    modifier = Modifier.weight(1f)
                )
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
                    onSavingsClick = onSavingsClick,
                    onReportsClick = {
                        navController.navigate(Screen.Reports.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddTransactionClick = { onAddTransactionClick(null) }
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

@Composable
fun RowScope.TabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = color
            )
        )
    }
}
