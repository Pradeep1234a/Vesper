package com.vesper.ledger.ui.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    val selectedDatePreset by viewModel.selectedDatePreset.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val minAmountFilter by viewModel.minAmountFilter.collectAsState()
    val maxAmountFilter by viewModel.maxAmountFilter.collectAsState()

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.dateEpochMillis))
        }
    }

    val listState = rememberLazyListState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Delete Transaction",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction?",
                    fontFamily = SpaceGroteskFamily,
                    color = Color(0xFFA1A1AA)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Delete", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily, color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(6.dp)
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SpeedDialFab(
                onActionSelected = { type ->
                    viewModel.selectedType.value = type
                    onAddTransactionClick()
                },
                hasBottomBar = true
            )
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
                        placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceGroteskFamily)) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Filter Icon Button with Count Badge
                    Surface(
                        onClick = { showFilterBottomSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeFilterCount > 0) Color(0xFF38BDF8).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (activeFilterCount > 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = "Filter & Sort",
                                tint = if (activeFilterCount > 0) Color(0xFF38BDF8) else MaterialTheme.colorScheme.onSurface
                            )
                            if (activeFilterCount > 0) {
                                Badge(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp),
                                    containerColor = Color(0xFF38BDF8),
                                    contentColor = Color.Black
                                ) {
                                    Text(text = activeFilterCount.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Active Filter Chips Bar (Scrollable horizontal row)
                val hasActiveFilters = activeFilterCount > 0 || selectedDatePreset != DatePreset.ALL
                if (hasActiveFilters) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Date Preset Chip
                        if (selectedDatePreset != DatePreset.ALL) {
                            item {
                                ActiveFilterChip(
                                    label = "📅 ${selectedDatePreset.label}",
                                    onRemove = { viewModel.setDatePreset(DatePreset.ALL) }
                                )
                            }
                        }

                        // Transaction Type Chip
                        if (selectedType != null) {
                            item {
                                ActiveFilterChip(
                                    label = selectedType!!.name,
                                    onRemove = { viewModel.selectedType.value = null }
                                )
                            }
                        }

                        // Category Chips
                        items(selectedCategories.toList()) { catId ->
                            val cat = categories.find { it.id == catId }
                            if (cat != null) {
                                ActiveFilterChip(
                                    label = cat.name,
                                    onRemove = {
                                        viewModel.selectedCategories.value = selectedCategories - catId
                                    }
                                )
                            }
                        }

                        // Payment Method Chip
                        if (selectedPaymentMethod != null) {
                            item {
                                ActiveFilterChip(
                                    label = selectedPaymentMethod!!,
                                    onRemove = { viewModel.selectedPaymentMethod.value = null }
                                )
                            }
                        }

                        // Account Chip
                        if (selectedAccount != null) {
                            val acct = accounts.find { it.id == selectedAccount }
                            if (acct != null) {
                                item {
                                    ActiveFilterChip(
                                        label = acct.name,
                                        onRemove = { viewModel.selectedAccount.value = null }
                                    )
                                }
                            }
                        }

                        // Amount Range Chip
                        if (minAmountFilter != null || maxAmountFilter != null) {
                            val minStr = minAmountFilter?.let { "$currencySymbol${DecimalFormat("#,##0").format(it)}" } ?: "0"
                            val maxStr = maxAmountFilter?.let { "$currencySymbol${DecimalFormat("#,##0").format(it)}" } ?: "Max"
                            item {
                                ActiveFilterChip(
                                    label = "$minStr - $maxStr",
                                    onRemove = {
                                        viewModel.minAmountFilter.value = null
                                        viewModel.maxAmountFilter.value = null
                                    }
                                )
                            }
                        }

                        // Clear All Button Chip
                        item {
                            Text(
                                text = "Clear All",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.clearAllFilters() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Modern BottomSheet Filter Dialog
                if (showFilterBottomSheet) {
                    TransactionFilterDialog(
                        viewModel = viewModel,
                        currencySymbol = currencySymbol,
                        onDismissRequest = { showFilterBottomSheet = false }
                    )
                }

                // Grouped Transaction List
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
                                fontFamily = SpaceGroteskFamily,
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
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                                )
                            }

                            items(txList) { tx ->
                                val category = categories.find { it.id == tx.categoryId }
                                val isIncome = tx.type == TransactionType.INCOME
                                val accentColor = if (isIncome) Color(0xFF10B981) else Color(0xFFEF4444)
                                val accentBg = if (isIncome) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFEF4444).copy(alpha = 0.12f)
                                val iconName = category?.iconName ?: "category"
                                val categoryLabel = category?.name ?: "Uncategorized"

                                ShCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onEditTransactionClick(tx) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category Icon Container
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = accentBg,
                                                    shape = RoundedCornerShape(6.dp)
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
                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Title & Category Left, Amount & Time Right
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                val displayTitle = if (tx.title.isBlank() || tx.title == "Untitled Transaction") categoryLabel else tx.title
                                                Text(
                                                    text = displayTitle,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "$categoryLabel • ${tx.paymentMethod}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    maxLines = 1
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${if (isIncome) "+" else "-"}$currencySymbol${df.format(tx.amount)}",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = accentColor
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = timeFormat.format(Date(tx.dateEpochMillis)),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
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
}

// ────────────────────────────────────────────────────────────────────────────
// REDESIGNED BOTTOMSHEET FILTER DIALOG WITH SECTION CARDS & HARMONIOUS CONTRAST
// ────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterDialog(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onDismissRequest: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()

    val selectedDatePreset by viewModel.selectedDatePreset.collectAsState()
    val startDateFilter by viewModel.startDateFilter.collectAsState()
    val endDateFilter by viewModel.endDateFilter.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val selectedPaymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val minAmountFilter by viewModel.minAmountFilter.collectAsState()
    val maxAmountFilter by viewModel.maxAmountFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showCustomDateRangePicker by remember { mutableStateOf(false) }
    var showAllCategories by remember { mutableStateOf(false) }

    val paymentMethodsList = listOf("Cash", "UPI", "Credit Card", "Debit Card", "Bank Transfer", "Wallet")

    // Date Range Picker Dialog for Custom Preset
    if (showCustomDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDateFilter ?: System.currentTimeMillis(),
            initialSelectedEndDateMillis = endDateFilter ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showCustomDateRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        viewModel.setDatePreset(DatePreset.CUSTOM, start, end)
                    }
                    showCustomDateRangePicker = false
                }) {
                    Text("Apply Range", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDateRangePicker = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily, color = Color(0xFFA1A1AA))
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color(0xFF18181B))
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF18181B),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color(0xFFA1A1AA),
                    subheadContentColor = Color.White,
                    yearContentColor = Color.White,
                    currentYearContentColor = Color(0xFF38BDF8),
                    selectedYearContentColor = Color.Black,
                    selectedYearContainerColor = Color.White,
                    dayContentColor = Color.White,
                    disabledDayContentColor = Color(0xFF52525B),
                    selectedDayContentColor = Color.Black,
                    selectedDayContainerColor = Color.White,
                    todayContentColor = Color(0xFF38BDF8),
                    todayDateBorderColor = Color(0xFF38BDF8)
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF121215), // Distinct Modal Surface
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILTERS",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFA1A1AA))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION CARD 1: DATE & TYPE
                FilterSectionCard(title = "DATE & TYPE") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePreset.values().forEach { preset ->
                                val isSelected = selectedDatePreset == preset
                                FilterChipPill(
                                    label = preset.label,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (preset == DatePreset.CUSTOM) {
                                            showCustomDateRangePicker = true
                                        } else {
                                            viewModel.setDatePreset(preset)
                                        }
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF242429))
                                .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val types = listOf<Pair<TransactionType?, String>>(
                                null to "All",
                                TransactionType.EXPENSE to "Expense",
                                TransactionType.INCOME to "Income",
                                TransactionType.TRANSFER to "Transfer"
                            )
                            types.forEach { (t, label) ->
                                val selected = selectedType == t
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (selected) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.Transparent)
                                        .border(1.dp, if (selected) Color(0xFF38BDF8) else Color.Transparent, RoundedCornerShape(4.dp))
                                        .clickable { viewModel.selectedType.value = t },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else Color(0xFFA1A1AA)
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION CARD 2: CATEGORIES
                FilterSectionCard(
                    title = "CATEGORIES",
                    action = if (categories.size > 6) {
                        {
                            Text(
                                text = if (showAllCategories) "Show Less" else "View All (${categories.size})",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.clickable { showAllCategories = !showAllCategories }
                            )
                        }
                    } else null
                ) {
                    val displayCategories = if (showAllCategories) categories else categories.take(6)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        displayCategories.forEach { cat ->
                            val isSelected = selectedCategories.contains(cat.id)
                            val catColor = safeParseColor(cat.colorHex)
                            FilterCategoryChip(
                                name = cat.name,
                                iconName = cat.iconName,
                                color = catColor,
                                isSelected = isSelected,
                                onClick = {
                                    val newSet = if (isSelected) selectedCategories - cat.id else selectedCategories + cat.id
                                    viewModel.selectedCategories.value = newSet
                                }
                            )
                        }
                    }
                }

                // SECTION CARD 3: PAYMENT METHODS & ACCOUNTS
                FilterSectionCard(title = "PAYMENT & ACCOUNTS") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "PAYMENT METHOD",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFFA1A1AA)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            paymentMethodsList.forEach { method ->
                                val isSelected = selectedPaymentMethod == method
                                FilterMethodChip(
                                    label = method,
                                    icon = getPaymentMethodIcon(method),
                                    isSelected = isSelected,
                                    onClick = {
                                        viewModel.selectedPaymentMethod.value = if (isSelected) null else method
                                    }
                                )
                            }
                        }

                        if (accounts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ACCOUNT",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFA1A1AA)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acct ->
                                    val isSelected = selectedAccount == acct.id
                                    FilterMethodChip(
                                        label = acct.name,
                                        icon = getIconByName(acct.iconName),
                                        isSelected = isSelected,
                                        onClick = {
                                            viewModel.selectedAccount.value = if (isSelected) null else acct.id
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION CARD 4: AMOUNT & SORT
                FilterSectionCard(title = "AMOUNT & SORT") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AMOUNT RANGE",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                                val minLabel = minAmountFilter?.let { "${currencySymbol}${DecimalFormat("#,##0").format(it)}" } ?: "${currencySymbol}0"
                                val maxLabel = maxAmountFilter?.let { "${currencySymbol}${DecimalFormat("#,##0").format(it)}" } ?: "${currencySymbol}50,000+"
                                Text(
                                    text = "$minLabel - $maxLabel",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            var sliderRange by remember(minAmountFilter, maxAmountFilter) {
                                val currentMin = minAmountFilter?.toFloat() ?: 0f
                                val currentMax = maxAmountFilter?.toFloat() ?: 50000f
                                mutableStateOf(currentMin..currentMax)
                            }

                            RangeSlider(
                                value = sliderRange,
                                onValueChange = { range ->
                                    sliderRange = range
                                    viewModel.minAmountFilter.value = if (range.start > 0f) range.start.toDouble() else null
                                    viewModel.maxAmountFilter.value = if (range.endInclusive < 50000f) range.endInclusive.toDouble() else null
                                },
                                valueRange = 0f..50000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF38BDF8),
                                    activeTrackColor = Color(0xFF38BDF8),
                                    inactiveTrackColor = Color(0xFF27272A)
                                )
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "SORT ORDER",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = Color(0xFFA1A1AA)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    SortOption.DATE_DESC to "Newest First",
                                    SortOption.DATE_ASC to "Oldest First",
                                    SortOption.AMOUNT_DESC to "Highest Amount",
                                    SortOption.AMOUNT_ASC to "Lowest Amount"
                                ).forEach { (sort, label) ->
                                    val isSelected = sortBy == sort
                                    FilterChipPill(
                                        label = label,
                                        isSelected = isSelected,
                                        onClick = { viewModel.sortBy.value = sort }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(
                    text = "Apply (${filteredTransactions.size})",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.clearAllFilters() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Text("Reset All", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ────────────────────────────────────────────────────────────────────────────
// DESIGN SYSTEM COMPONENTS FOR FILTER SHEET (RHYTHM, CONTRAST & HIERARCHY)
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun FilterSectionCard(
    title: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF18181B), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0xFFA1A1AA)
                )
                action?.invoke()
            }
            content()
        }
    }
}

@Composable
fun FilterChipPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF242429))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFFD4D4D8)
        )
    }
}

@Composable
fun FilterCategoryChip(
    name: String,
    iconName: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF242429))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = getIconByName(iconName),
                contentDescription = null,
                tint = if (isSelected) Color(0xFF38BDF8) else color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = name,
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFD4D4D8)
            )
        }
    }
}

@Composable
fun FilterMethodChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF242429))
            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF38BDF8) else Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFD4D4D8)
            )
        }
    }
}

@Composable
fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF27272A))
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFFA1A1AA),
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}
