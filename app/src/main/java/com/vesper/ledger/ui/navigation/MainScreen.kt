package com.vesper.ledger.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.budget.BudgetScreen
import com.vesper.ledger.ui.budget.BudgetsViewModel
import com.vesper.ledger.ui.budget.BudgetsViewModelFactory
import com.vesper.ledger.ui.dashboard.DashboardScreen
import com.vesper.ledger.ui.dashboard.DashboardViewModel
import com.vesper.ledger.ui.dashboard.DashboardViewModelFactory
import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import com.vesper.ledger.ui.settings.SettingsScreen
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.transactions.AddTransactionScreen
import com.vesper.ledger.ui.transactions.CategoryOption
import com.vesper.ledger.ui.transactions.CategorySelectionScreen
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory
import kotlinx.coroutines.launch

data class DrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
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
    val transactionsFactory = TransactionsViewModelFactory(app.transactionRepository, app.accountRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val budgetsFactory = BudgetsViewModelFactory(app)
    val categoryFactory = com.vesper.ledger.ui.category.CategoryViewModelFactory(app, app.transactionRepository)

    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()
    val userName by settingsViewModel.userName.collectAsState()
    val userEmail by settingsViewModel.userEmail.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var activeSelectedCategory by remember { mutableStateOf<CategoryOption?>(null) }
    var editingCategoryState by remember { mutableStateOf<Category?>(null) }
    var editingAccountState by remember { mutableStateOf<Account?>(null) }

    val drawerItems = listOf(
        DrawerItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard),
        DrawerItem(Screen.Transactions.route, "Transactions", Icons.Outlined.ListAlt),
        DrawerItem(Screen.Accounts.route, "Manage Accounts", Icons.Outlined.AccountBalanceWallet),
        DrawerItem(Screen.Budgets.route, "Budgets", Icons.Outlined.PieChart),
        DrawerItem(Screen.Savings.route, "Savings", Icons.Outlined.Savings),
        DrawerItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings)
    )

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        BottomNavItem(Screen.Transactions.route, "Transactions", Icons.Outlined.ListAlt, Icons.Filled.ListAlt),
        BottomNavItem(Screen.Budgets.route, "Budgets", Icons.Outlined.PieChart, Icons.Filled.PieChart),
        BottomNavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Transactions.route,
        Screen.Budgets.route,
        Screen.Settings.route
    )

    // 100% Uniform Transition Specs Across ALL Screens & Routes
    val uniformEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(280))
    }
    val uniformExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { -it / 4 } + fadeOut(animationSpec = tween(280))
    }
    val uniformPopEnter: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { -it / 4 } + fadeIn(animationSpec = tween(280))
    }
    val uniformPopExit: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(280))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevelRoute,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = if (userName.isNotBlank()) userName else "Vesper Ledger",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (userEmail.isNotBlank()) {
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        drawerItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    unselectedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevelRoute) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .navigationBarsPadding(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomNavItems.forEach { item ->
                                val selected = currentRoute == item.route
                                TabItem(
                                    icon = if (selected) item.selectedIcon else item.icon,
                                    label = item.label,
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = uniformEnter,
                exitTransition = uniformExit,
                popEnterTransition = uniformPopEnter,
                popExitTransition = uniformPopExit
            ) {
                composable(
                    route = Screen.Dashboard.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = dashboardFactory)
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        currencySymbol = currencySymbol,
                        userName = userName,
                        onMenuClick = { scope.launch { drawerState.open() } },
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
                        onAddTransactionClick = {
                            navController.navigate(Screen.AddTransaction.route)
                        }
                    )
                }

                composable(
                    route = Screen.Transactions.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                    TransactionsScreen(
                        viewModel = transactionsViewModel,
                        currencySymbol = currencySymbol,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { navController.popBackStack() },
                        onAddTransactionClick = {
                            navController.navigate(Screen.AddTransaction.route)
                        }
                    )
                }

                composable(
                    route = Screen.AddTransaction.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                    val dbAccounts by transactionsViewModel.dbAccounts.collectAsState()

                    AddTransactionScreen(
                        currencySymbol = currencySymbol,
                        accountsList = dbAccounts,
                        onBackClick = { navController.popBackStack() },
                        onOpenCategorySelection = { type, currentCatName ->
                            navController.navigate("${Screen.CategorySelection.route}?type=${type.name}")
                        },
                        selectedCategory = activeSelectedCategory,
                        onAddAccountClick = {
                            editingAccountState = null
                            navController.navigate(Screen.AddAccount.route)
                        },
                        onSaveTransaction = { title, amount, type, categoryId, dateEpochMillis, accountName, paymentMethod, note ->
                            transactionsViewModel.addTransaction(
                                title = title,
                                amount = amount,
                                type = type,
                                categoryId = categoryId,
                                accountName = accountName,
                                note = note
                            )
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "${Screen.CategorySelection.route}?type={type}",
                    arguments = listOf(androidx.navigation.navArgument("type") { defaultValue = TransactionType.EXPENSE.name }),
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) { backStackEntry ->
                    val typeStr = backStackEntry.arguments?.getString("type") ?: TransactionType.EXPENSE.name
                    val categoryType = try { TransactionType.valueOf(typeStr) } catch (e: Exception) { TransactionType.EXPENSE }

                    CategorySelectionScreen(
                        initialType = categoryType,
                        selectedCategoryName = activeSelectedCategory?.name ?: "Groceries",
                        onBackClick = { navController.popBackStack() },
                        onCategorySelected = { cat ->
                            activeSelectedCategory = cat
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Screen.Accounts.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                    val dbAccounts by transactionsViewModel.dbAccounts.collectAsState()

                    com.vesper.ledger.ui.accounts.AccountsScreen(
                        accounts = dbAccounts,
                        currencySymbol = currencySymbol,
                        onBackClick = { navController.popBackStack() },
                        onAddAccountClick = {
                            editingAccountState = null
                            navController.navigate(Screen.AddAccount.route)
                        },
                        onEditAccountClick = { acc ->
                            editingAccountState = acc
                            navController.navigate(Screen.AddAccount.route)
                        },
                        onUpdateAccount = { acc ->
                            transactionsViewModel.updateAccount(acc)
                        },
                        onDeleteAccount = { acc ->
                            transactionsViewModel.deleteAccount(acc)
                        }
                    )
                }

                composable(
                    route = Screen.AddAccount.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                    com.vesper.ledger.ui.accounts.AddEditAccountScreen(
                        editingAccount = editingAccountState,
                        onBackClick = { navController.popBackStack() },
                        onSaveAccount = { name, type, bal, colorHex, includeInTotal ->
                            val currentEditing = editingAccountState
                            if (currentEditing != null) {
                                transactionsViewModel.updateAccount(
                                    currentEditing.copy(
                                        name = name,
                                        type = type,
                                        initialBalance = bal,
                                        colorHex = colorHex,
                                        includeInTotal = includeInTotal
                                    )
                                )
                            } else {
                                transactionsViewModel.addNewAccount(
                                    name = name,
                                    type = type,
                                    initialBalance = bal,
                                    colorHex = colorHex,
                                    includeInTotal = includeInTotal
                                )
                            }
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Screen.Categories.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val categoryViewModel: com.vesper.ledger.ui.category.CategoryViewModel = viewModel(factory = categoryFactory)
                    com.vesper.ledger.ui.category.CategoriesScreen(
                        viewModel = categoryViewModel,
                        onBackClick = { navController.popBackStack() },
                        onAddCategoryClick = {
                            editingCategoryState = null
                            navController.navigate(Screen.AddCategory.route)
                        },
                        onEditCategoryClick = { cat ->
                            editingCategoryState = cat
                            navController.navigate(Screen.AddCategory.route)
                        }
                    )
                }

                composable(
                    route = Screen.AddCategory.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val categoryViewModel: com.vesper.ledger.ui.category.CategoryViewModel = viewModel(factory = categoryFactory)
                    com.vesper.ledger.ui.categories.AddEditCategoryScreen(
                        editingCategory = editingCategoryState,
                        onBackClick = { navController.popBackStack() },
                        onSaveCategory = { name, iconName, type, colorHex ->
                            val currentEditing = editingCategoryState
                            if (currentEditing != null) {
                                categoryViewModel.updateCategory(currentEditing.copy(name = name, iconName = iconName, type = type, colorHex = colorHex))
                            } else {
                                categoryViewModel.addCategory(name = name, iconName = iconName, type = type, colorHex = colorHex)
                            }
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Screen.Budgets.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val budgetsViewModel: BudgetsViewModel = viewModel(factory = budgetsFactory)
                    BudgetScreen(
                        viewModel = budgetsViewModel,
                        currencySymbol = currencySymbol,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Savings.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    val savingsViewModel: SavingsViewModel = viewModel(factory = savingsFactory)
                    SavingsScreen(
                        viewModel = savingsViewModel,
                        currencySymbol = currencySymbol,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Settings.route,
                    enterTransition = uniformEnter,
                    exitTransition = uniformExit,
                    popEnterTransition = uniformPopEnter,
                    popExitTransition = uniformPopExit
                ) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        updateViewModel = updateViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { navController.popBackStack() },
                        onCategoriesClick = onCategoryManagementClick,
                        onAccountsClick = { navController.navigate(Screen.Accounts.route) },
                        onSignOutClick = onSignOutClick
                    )
                }
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
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}
