package com.vesper.ledger.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddTransactionClick: () -> Unit = {},
    onEditTransactionClick: (Transaction) -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.dateEpochMillis))
        }
    }

    val listState = rememberLazyListState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Delete Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                // Search Input Field & Filter Action Button Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    val activeFilterCount by viewModel.activeFilterCount.collectAsState()
                    Surface(
                        onClick = { showFilterDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = if (activeFilterCount > 0) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Outlined.FilterList,
                                contentDescription = "Filter & Sort",
                                tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                            )
                            if (activeFilterCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text(text = activeFilterCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (showFilterDialog) {
                    FilterAndSortDialog(
                        viewModel = viewModel,
                        currencySymbol = currencySymbol,
                        onDismissRequest = { showFilterDialog = false }
                    )
                }

                // Grouped Transaction list
                if (groupedTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        groupedTransactions.forEach { (dateStr, txList) ->
                            item {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }

                            items(txList) { tx ->
                                var showMenu by remember { mutableStateOf(false) }
                                val category = categories.find { it.id == tx.categoryId }
                                val isIncome = tx.type == TransactionType.INCOME
                                val accentColor = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626)
                                val accentBg = if (isIncome) Color(0xFF16A34A).copy(alpha = 0.08f) else Color(0xFFDC2626).copy(alpha = 0.08f)
                                val iconName = category?.iconName ?: "category"
                                val categoryLabel = category?.name ?: "Uncategorized"

                                ShCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 1.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onEditTransactionClick(tx) },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Category Icon Container
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(
                                                        color = accentBg,
                                                        shape = RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getIconByName(iconName),
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            
                                            // Title/Category & Amount/Date aligned up-and-down
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Left Column: Title on top, Category Name on bottom
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val displayTitle = if (tx.title.isBlank() || tx.title == "Untitled Transaction") categoryLabel else tx.title
                                                    Text(
                                                        text = displayTitle,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        ),
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "$categoryLabel • ${tx.accountName}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        ),
                                                        maxLines = 1
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(8.dp))
                                                
                                                // Right Column: Amount on top, Date on bottom
                                                Column(horizontalAlignment = Alignment.End) {
                                                    val prefix = if (isIncome) "+" else "-"
                                                    Text(
                                                        text = "$prefix$currencySymbol${df.format(tx.amount)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 14.sp,
                                                            fontFamily = SpaceGroteskFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            color = accentColor
                                                        )
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "${sdfShort.format(Date(tx.dateEpochMillis))} • ${timeFormat.format(Date(tx.dateEpochMillis))}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        // CRUD Action Menu
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = getIconByName("edit"),
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Edit",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    onEditTransactionClick(tx)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.DeleteOutline,
                                                            contentDescription = null,
                                                            tint = Color(0xFFDC2626),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Delete",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = Color(0xFFDC2626)
                                                            )
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    showMenu = false
                                                    transactionToDelete = tx
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAndSortDialog(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onDismissRequest: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val accounts: List<Account> by viewModel.accounts.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val startDateFilter by viewModel.startDateFilter.collectAsState()
    val endDateFilter by viewModel.endDateFilter.collectAsState()
    val minAmountFilter by viewModel.minAmountFilter.collectAsState()
    val maxAmountFilter by viewModel.maxAmountFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showTypeMenu by remember { mutableStateOf(false) }
    var showCatMenu by remember { mutableStateOf(false) }
    var showAcctMenu by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var minAmtText by remember(minAmountFilter) { mutableStateOf(minAmountFilter?.toString() ?: "") }
    var maxAmtText by remember(maxAmountFilter) { mutableStateOf(maxAmountFilter?.toString() ?: "") }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val paymentMethodsList = listOf("All Payment Methods", "Cash", "Bank Transfer", "Credit Card", "Debit Card", "UPI", "Other")

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateFilter ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startDateFilter.value = datePickerState.selectedDateMillis
                    showStartDatePicker = false
                }) {
                    Text("Select", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDateFilter ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.endDateFilter.value = datePickerState.selectedDateMillis
                    showEndDatePicker = false
                }) {
                    Text("Select", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                TextButton(onClick = {
                    viewModel.clearAllFilters()
                    minAmtText = ""
                    maxAmtText = ""
                }) {
                    Text(
                        text = "Reset All",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SORT ORDER MENU
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SORT ORDER", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (sortBy) {
                                        SortOption.DATE_DESC -> "Date: Newest First"
                                        SortOption.DATE_ASC -> "Date: Oldest First"
                                        SortOption.AMOUNT_DESC -> "Amount: Highest First"
                                        SortOption.AMOUNT_ASC -> "Amount: Lowest First"
                                        SortOption.EXPENSE_DESC -> "Highest Expense First"
                                        SortOption.INCOME_DESC -> "Highest Income First"
                                    },
                                    fontFamily = SpaceGroteskFamily,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(text = { Text("Date: Newest First") }, onClick = { viewModel.sortBy.value = SortOption.DATE_DESC; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Date: Oldest First") }, onClick = { viewModel.sortBy.value = SortOption.DATE_ASC; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Amount: Highest First") }, onClick = { viewModel.sortBy.value = SortOption.AMOUNT_DESC; showSortMenu = false })
                            DropdownMenuItem(text = { Text("Amount: Lowest First") }, onClick = { viewModel.sortBy.value = SortOption.AMOUNT_ASC; showSortMenu = false })
                        }
                    }
                }

                // 2. TRANSACTION TYPE MENU
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("TRANSACTION TYPE", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showTypeMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedType?.name ?: "All Types",
                                    fontFamily = SpaceGroteskFamily,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(expanded = showTypeMenu, onDismissRequest = { showTypeMenu = false }) {
                            DropdownMenuItem(text = { Text("All Types") }, onClick = { viewModel.selectedType.value = null; showTypeMenu = false })
                            TransactionType.values().forEach { t ->
                                DropdownMenuItem(text = { Text(t.name) }, onClick = { viewModel.selectedType.value = t; showTypeMenu = false })
                            }
                        }
                    }
                }

                // 3. CATEGORY MENU
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("CATEGORY", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentCatName = categories.find { selectedCategories.contains(it.id) }?.name ?: "All Categories"
                        OutlinedButton(
                            onClick = { showCatMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentCatName, fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurface)
                                Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(expanded = showCatMenu, onDismissRequest = { showCatMenu = false }) {
                            DropdownMenuItem(text = { Text("All Categories") }, onClick = { viewModel.selectedCategories.value = emptySet(); showCatMenu = false })
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat.name) }, onClick = { viewModel.selectedCategories.value = setOf(cat.id); showCatMenu = false })
                            }
                        }
                    }
                }

                // 4. ACCOUNT MENU
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ACCOUNT", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentAcctName = accounts.find { it.id == selectedAccount }?.name ?: "All Accounts"
                        OutlinedButton(
                            onClick = { showAcctMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentAcctName, fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurface)
                                Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(expanded = showAcctMenu, onDismissRequest = { showAcctMenu = false }) {
                            DropdownMenuItem(text = { Text("All Accounts") }, onClick = { viewModel.selectedAccount.value = null; showAcctMenu = false })
                            accounts.forEach { acct ->
                                DropdownMenuItem(text = { Text(acct.name) }, onClick = { viewModel.selectedAccount.value = acct.id; showAcctMenu = false })
                            }
                        }
                    }
                }

                // 5. PAYMENT METHOD MENU
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("PAYMENT METHOD", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showPaymentMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = selectedPaymentMethod ?: "All Payment Methods", fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurface)
                                Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                        DropdownMenu(expanded = showPaymentMenu, onDismissRequest = { showPaymentMenu = false }) {
                            paymentMethodsList.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        viewModel.selectedPaymentMethod.value = if (p == "All Payment Methods") null else p
                                        showPaymentMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. DATE RANGE (MATERIAL 3 DATE PICKERS)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("DATE FILTER", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = startDateFilter?.let { dateFormat.format(Date(it)) } ?: "Start Date",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFamily
                            )
                        }

                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = endDateFilter?.let { dateFormat.format(Date(it)) } ?: "End Date",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFamily
                            )
                        }
                    }
                }

                // 7. AMOUNT RANGE FILTER
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AMOUNT RANGE ($currencySymbol)", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minAmtText,
                            onValueChange = {
                                minAmtText = it
                                viewModel.minAmountFilter.value = it.toDoubleOrNull()
                            },
                            placeholder = { Text("Min Amount", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = maxAmtText,
                            onValueChange = {
                                maxAmtText = it
                                viewModel.maxAmountFilter.value = it.toDoubleOrNull()
                            },
                            placeholder = { Text("Max Amount", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
            ) {
                Text("Apply Filters", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}
