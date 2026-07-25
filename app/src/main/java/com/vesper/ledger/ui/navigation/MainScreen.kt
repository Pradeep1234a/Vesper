package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.app.Activity
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
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
import com.vesper.ledger.ui.components.VesperUnifiedTopBar
import com.vesper.ledger.ui.dashboard.DashboardScreen
import com.vesper.ledger.ui.dashboard.DashboardViewModel
import com.vesper.ledger.ui.dashboard.DashboardViewModelFactory
import com.vesper.ledger.ui.settings.SettingsScreen
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.settings.CurrencySelectorScreen
import com.vesper.ledger.ui.settings.CurrencyFlowMode
import com.vesper.ledger.ui.transactions.TransactionsScreen
import com.vesper.ledger.ui.transactions.TransactionsViewModel
import com.vesper.ledger.ui.transactions.TransactionsViewModelFactory
import com.vesper.ledger.ui.savings.SavingsScreen
import com.vesper.ledger.ui.savings.SavingsViewModel
import com.vesper.ledger.ui.savings.SavingsViewModelFactory
import com.vesper.ledger.ui.budget.BudgetScreen
import com.vesper.ledger.ui.budget.BudgetsViewModel
import com.vesper.ledger.ui.budget.BudgetsViewModelFactory
import com.vesper.ledger.ui.split.SplitGroupsScreen
import com.vesper.ledger.ui.split.CreateSplitGroupScreen
import com.vesper.ledger.ui.profile.ProfileManagementScreen
import com.vesper.ledger.ui.budget.AddEditBudgetScreen
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.ui.category.CategoriesScreen
import com.vesper.ledger.ui.category.AddEditCategoryScreen
import com.vesper.ledger.ui.category.CategoryViewModel
import com.vesper.ledger.ui.category.CategoryViewModelFactory
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.ui.accounts.AccountsScreen
import com.vesper.ledger.ui.accounts.AddEditAccountScreen
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.ui.analytics.AnalyticsScreen
import com.vesper.ledger.ui.transactions.AddTransactionScreen
import com.vesper.ledger.data.model.TransactionType
import androidx.compose.material.icons.outlined.BarChart
import kotlinx.coroutines.Dispatchers
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
    onSavingsClick: () -> Unit = {},
    onCategoryManagementClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onSignOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val app = context.applicationContext as VesperApplication

    val dashboardFactory = DashboardViewModelFactory(app.transactionRepository, app.savingsRepository, app.accountRepository, app.budgetRepository)
    val transactionsFactory = TransactionsViewModelFactory(app.transactionRepository, app.accountRepository)
    val savingsFactory = SavingsViewModelFactory(app.savingsRepository)
    val budgetsFactory = BudgetsViewModelFactory(app)

    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()
    val userName by settingsViewModel.userName.collectAsState()
    val userEmail by settingsViewModel.userEmail.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val view = LocalView.current
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    if (!view.isInEditMode) {
        SideEffect {
            var ctx = view.context
            while (ctx is android.content.ContextWrapper) {
                if (ctx is Activity) {
                    ctx.window.statusBarColor = surfaceColor
                    ctx.window.navigationBarColor = surfaceColor
                    break
                }
                ctx = ctx.baseContext
            }
        }
    }

    val drawerItems = listOf(
        DrawerItem(Screen.Dashboard.route, "Dashboard", Icons.Outlined.Dashboard),
        DrawerItem(Screen.Transactions.route, "Transactions", Icons.Outlined.ListAlt),
        DrawerItem(Screen.Analytics.route, "Analytics", Icons.Outlined.BarChart),
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

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 0.dp,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    // Drawer Header
                    Text(
                        text = "VESPER LEDGER",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 1.8.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // User Info Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = SpaceGroteskFamily,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column {
                            Text(
                                text = userName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userEmail.ifBlank { "Personal Space" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Navigation Items
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        drawerItems.forEach { item ->
                            val selected = currentRoute == item.route
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent
                                    )
                                    .clickable {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(item.route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = item.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Logout Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                scope.launch {
                                    drawerState.close()
                                    onSignOutClick()
                                }
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Logout",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    ) {
        val showTopBar = currentRoute != null &&
                currentRoute != Screen.AuthWelcome.route &&
                currentRoute != Screen.AuthSignIn.route &&
                currentRoute != Screen.AuthCreateAccount.route &&
                currentRoute != Screen.AuthForgotPassword.route

        val isRootScreen = currentRoute in listOf(
            Screen.Dashboard.route,
            Screen.Transactions.route,
            Screen.Budgets.route,
            Screen.Analytics.route,
            Screen.Settings.route
        )

        val screenTitle = when (currentRoute) {
            Screen.Dashboard.route -> "Vesper Ledger"
            Screen.Transactions.route -> "Transactions"
            Screen.Budgets.route -> "Budgets"
            Screen.Analytics.route -> "Analytics"
            Screen.Settings.route -> "Settings"
            Screen.Savings.route -> "Savings Goals"
            Screen.AddTransaction.route -> "New Transaction"
            Screen.CurrencySelector.route -> "Select Currency"
            Screen.AddCategory.route -> "Categories"
            Screen.Accounts.route -> "Accounts"
            Screen.AddAccount.route -> "New Account"
            Screen.AddBudget.route -> "New Budget"
            Screen.SplitGroups.route -> "Split Groups"
            Screen.CreateSplitGroup.route -> "Create Split Group"
            Screen.ProfileManagement.route -> "Profile Management"
            else -> "Vesper Ledger"
        }

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (showTopBar) {
                    VesperUnifiedTopBar(
                        title = screenTitle,
                        isRoot = isRootScreen,
                        onNavigationClick = {
                            if (isRootScreen) {
                                scope.launch { drawerState.open() }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        actions = {
                            if (currentRoute == Screen.Dashboard.route) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                        .clickable { navController.navigate(Screen.ProfileManagement.route) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.take(1).uppercase(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Divider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(56.dp),
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
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (isRootScreen) 57.dp else 0.dp),
                    enterTransition = { fadeIn(animationSpec = tween(220)) },
                    exitTransition = { fadeOut(animationSpec = tween(180)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(220)) },
                    popExitTransition = { fadeOut(animationSpec = tween(180)) }
                ) {
                composable(Screen.Dashboard.route) {
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
                        onAddCategoryClick = { navController.navigate(Screen.AddCategory.route) },
                        onAccountsClick = { navController.navigate(Screen.Accounts.route) },
                        onAddTransactionClick = { navController.navigate(Screen.AddTransaction.route) },
                        onBudgetsClick = {
                            navController.navigate(Screen.Budgets.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSplitGroupsClick = { navController.navigate(Screen.SplitGroups.route) }
                    )
                }

                composable(Screen.Transactions.route) {
                    val transactionsViewModel: TransactionsViewModel = viewModel(factory = transactionsFactory)
                    TransactionsScreen(
                        viewModel = transactionsViewModel,
                        currencySymbol = currencySymbol,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { navController.popBackStack() },
                        onAddTransactionClick = { navController.navigate(Screen.AddTransaction.route) }
                    )
                }

                composable(Screen.Budgets.route) {
                    val budgetsViewModel: BudgetsViewModel = viewModel(factory = budgetsFactory)
                    BudgetScreen(
                        viewModel = budgetsViewModel,
                        currencySymbol = currencySymbol,
                        onBackClick = { navController.popBackStack() },
                        onAddBudgetClick = { navController.navigate(Screen.AddBudget.route) },
                        onEditBudgetClick = { budget ->
                            navController.navigate(Screen.AddBudget.route)
                        }
                    )
                }

                composable(Screen.Analytics.route) {
                    val transactions by app.transactionRepository.allTransactions.collectAsState(initial = emptyList())
                    val categories by app.transactionRepository.allCategories.collectAsState(initial = emptyList())
                    val accounts by app.accountRepository.allAccounts.collectAsState(initial = emptyList())

                    AnalyticsScreen(
                        transactions = transactions,
                        categories = categories,
                        accounts = accounts,
                        currencySymbol = currencySymbol,
                        onMenuClick = { scope.launch { drawerState.open() } },
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
                        updateViewModel = updateViewModel,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onBackClick = { navController.popBackStack() },
                        onCategoriesClick = onCategoryManagementClick,
                        onCurrencyClick = { navController.navigate(Screen.CurrencySelector.route) },
                        onSignOutClick = onSignOutClick
                    )
                }

                composable(Screen.CurrencySelector.route) {
                    CurrencySelectorScreen(
                        viewModel = settingsViewModel,
                        flowMode = CurrencyFlowMode.SETTINGS,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AddTransaction.route) {
                    val transactions by app.transactionRepository.allTransactions.collectAsState(initial = emptyList())
                    val categories by app.transactionRepository.allCategories.collectAsState(initial = emptyList())
                    val accounts by app.accountRepository.allAccounts.collectAsState(initial = emptyList())
                    val paymentMethods by app.accountRepository.allPaymentMethods.collectAsState(initial = emptyList())

                    AddTransactionScreen(
                        currencySymbol = currencySymbol,
                        categories = categories,
                        accounts = accounts,
                        paymentMethods = paymentMethods,
                        onBackClick = { navController.popBackStack() },
                        onAddCategoryClick = { navController.navigate(Screen.AddCategory.route) },
                        onAddAccountClick = { navController.navigate(Screen.Accounts.route) },
                        onSaveTransaction = { title, amount, type, categoryId, accountId, accountName, paymentMethod, dateEpochMillis, note ->
                            scope.launch(Dispatchers.IO) {
                                app.transactionRepository.insertTransaction(
                                    com.vesper.ledger.data.model.Transaction(
                                        title = title.ifBlank { if (type == TransactionType.EXPENSE) "Expense" else "Income" },
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

                composable(Screen.AddCategory.route) {
                    val categoryFactory = CategoryViewModelFactory(app, app.transactionRepository)
                    val categoryViewModel: CategoryViewModel = viewModel(factory = categoryFactory)
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

                composable(Screen.Accounts.route) {
                    val transactions by app.transactionRepository.allTransactions.collectAsState(initial = emptyList())
                    val accounts by app.accountRepository.allAccounts.collectAsState(initial = emptyList())
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

                composable(Screen.AddBudget.route) {
                    val categories by app.transactionRepository.allCategories.collectAsState(initial = emptyList())
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

                composable(Screen.SplitGroups.route) {
                    SplitGroupsScreen(
                        currencySymbol = currencySymbol,
                        onCreateGroupClick = { navController.navigate(Screen.CreateSplitGroup.route) },
                        onAddExpenseClick = { navController.navigate(Screen.AddTransaction.route) }
                    )
                }

                composable(Screen.CreateSplitGroup.route) {
                    CreateSplitGroupScreen(
                        onBackClick = { navController.popBackStack() },
                        onGroupCreated = { navController.popBackStack() }
                    )
                }

                composable(Screen.ProfileManagement.route) {
                    ProfileManagementScreen(
                        settingsViewModel = settingsViewModel,
                        onBackClick = { navController.popBackStack() },
                        onSignOutClick = onSignOutClick,
                        onCurrencyClick = { navController.navigate(Screen.CurrencySelector.route) }
                    )
                }
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
