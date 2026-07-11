package com.vesper.ledger.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.RadioButton

enum class FilterSheetView {
    FILTERS, CATEGORIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit,
    onAddTransactionClick: (type: String?, id: Long?) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val isMultiSelectCategory by viewModel.isMultiSelectCategory.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val adaptiveCategories by viewModel.adaptiveCategories.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val dateFilterOption by viewModel.dateFilterOption.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val amountFilterOption by viewModel.amountFilterOption.collectAsState()
    val minAmount by viewModel.minAmount.collectAsState()
    val maxAmount by viewModel.maxAmount.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val context = LocalContext.current

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.dateEpochMillis))
        }
    }

    val listState = rememberLazyListState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTransactionClick(null, null) },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("New") },
                expanded = !listState.isScrollInProgress,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field & Filter Menu Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                val activeFiltersCount = (if (selectedCategories.isNotEmpty()) 1 else 0) +
                        (if (selectedType != null) 1 else 0) +
                        (if (dateFilterOption != DateFilterOption.ALL) 1 else 0) +
                        (if (amountFilterOption != AmountFilterOption.ALL) 1 else 0)
                
                val hasActiveFilters = activeFiltersCount > 0

                Card(
                    modifier = Modifier
                        .height(56.dp)
                        .width(56.dp)
                        .clickable { showFilterSheet = true },
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasActiveFilters) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filters",
                            tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
                    contentPadding = PaddingValues(bottom = 80.dp) // extra padding to clear the FAB!
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
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { showMenu = true },
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
                                                    text = categoryLabel,
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
                                                    text = dateFormat.format(Date(tx.dateEpochMillis)),
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
                                                        imageVector = Icons.Outlined.Edit,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Edit", style = MaterialTheme.typography.bodyMedium)
                                                }
                                            },
                                            onClick = {
                                                showMenu = false
                                                onAddTransactionClick(tx.type.name, tx.id)
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

    if (showFilterSheet) {
        var sheetView by remember { mutableStateOf(FilterSheetView.FILTERS) }
        var showRangePicker by remember { mutableStateOf(false) }
        var showSingleDatePicker by remember { mutableStateOf(false) }
        var showMonthPicker by remember { mutableStateOf(false) }

        // Date pickers dialog hooks
        if (showRangePicker) {
            DateRangePickerDialog(
                onDismissRequest = { showRangePicker = false },
                onDateRangeSelected = { start, end ->
                    viewModel.startDate.value = start
                    viewModel.endDate.value = end
                    viewModel.dateFilterOption.value = DateFilterOption.CUSTOM_RANGE
                    viewModel.selectedDate.value = null
                    viewModel.selectedMonth.value = null
                    showRangePicker = false
                }
            )
        }

        if (showSingleDatePicker) {
            SpecificDatePickerDialog(
                onDismissRequest = { showSingleDatePicker = false },
                onDateSelected = { date ->
                    viewModel.selectedDate.value = date
                    viewModel.dateFilterOption.value = DateFilterOption.SPECIFIC_DATE
                    viewModel.startDate.value = null
                    viewModel.endDate.value = null
                    viewModel.selectedMonth.value = null
                    showSingleDatePicker = false
                }
            )
        }

        if (showMonthPicker) {
            MonthYearPickerDialog(
                onDismissRequest = { showMonthPicker = false },
                onMonthYearSelected = { m, y ->
                    viewModel.selectedMonth.value = m
                    viewModel.selectedYear.value = y
                    viewModel.dateFilterOption.value = DateFilterOption.SPECIFIC_MONTH
                    viewModel.selectedDate.value = null
                    viewModel.startDate.value = null
                    viewModel.endDate.value = null
                    showMonthPicker = false
                }
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            if (sheetView == FilterSheetView.FILTERS) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Text("Reset All", color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 1. Scrollable filter options to prevent overflow
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // A. Quick Filters
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Quick Filters",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            // Compact selectable chips arranged in a balanced grid / FlowRow
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val quickPeriods = listOf(
                                    "Today" to DateFilterOption.TODAY,
                                    "Yesterday" to DateFilterOption.YESTERDAY,
                                    "This Week" to DateFilterOption.THIS_WEEK,
                                    "This Month" to DateFilterOption.THIS_MONTH,
                                    "Last Month" to DateFilterOption.LAST_MONTH,
                                    "Last 3 Months" to DateFilterOption.LAST_3_MONTHS
                                )
                                // Chunk into rows of 3
                                quickPeriods.chunked(3).forEach { rowPeriods ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowPeriods.forEach { (label, option) ->
                                            val isSelected = dateFilterOption == option
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    viewModel.dateFilterOption.value = if (isSelected) DateFilterOption.ALL else option
                                                },
                                                label = { Text(label, fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                // Surface adaptive top 3 categories
                                if (adaptiveCategories.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Top Categories",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        adaptiveCategories.forEach { cat ->
                                            val isSelected = selectedCategories.contains(cat.id)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    val current = selectedCategories.toMutableSet()
                                                    if (isSelected) {
                                                        current.remove(cat.id)
                                                    } else {
                                                        if (!isMultiSelectCategory) {
                                                            current.clear()
                                                        }
                                                        current.add(cat.id)
                                                    }
                                                    viewModel.selectedCategories.value = current
                                                },
                                                label = { Text(cat.name, fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        // Fill empty cells if less than 3
                                        repeat(3 - adaptiveCategories.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // B. Transaction Type
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Transaction Type",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val types = listOf(
                                    "All Transactions" to null,
                                    "Income" to TransactionType.INCOME,
                                    "Expense" to TransactionType.EXPENSE
                                )
                                types.forEach { (label, typeVal) ->
                                    val isSelected = selectedType == typeVal
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectedType.value = typeVal },
                                        label = { Text(label, fontSize = 12.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // C. Date Filters
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Date Filters",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            var dateDropdownExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { dateDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val activeLabel = when (dateFilterOption) {
                                        DateFilterOption.ALL -> "Select Date Option..."
                                        DateFilterOption.TODAY -> "Today"
                                        DateFilterOption.YESTERDAY -> "Yesterday"
                                        DateFilterOption.THIS_WEEK -> "This Week"
                                        DateFilterOption.THIS_MONTH -> "This Month"
                                        DateFilterOption.LAST_MONTH -> "Last Month"
                                        DateFilterOption.LAST_3_MONTHS -> "Last 3 Months"
                                        DateFilterOption.LAST_6_MONTHS -> "Last 6 Months"
                                        DateFilterOption.THIS_YEAR -> "This Year"
                                        DateFilterOption.CUSTOM_RANGE -> "Custom Date Range"
                                        DateFilterOption.SPECIFIC_DATE -> "Specific Date"
                                        DateFilterOption.SPECIFIC_MONTH -> "Specific Month"
                                    }
                                    Text(activeLabel)
                                }
                                DropdownMenu(
                                    expanded = dateDropdownExpanded,
                                    onDismissRequest = { dateDropdownExpanded = false }
                                ) {
                                    DateFilterOption.values().forEach { option ->
                                        val label = when (option) {
                                            DateFilterOption.ALL -> "No Date Filter"
                                            DateFilterOption.TODAY -> "Today"
                                            DateFilterOption.YESTERDAY -> "Yesterday"
                                            DateFilterOption.THIS_WEEK -> "This Week"
                                            DateFilterOption.THIS_MONTH -> "This Month"
                                            DateFilterOption.LAST_MONTH -> "Last Month"
                                            DateFilterOption.LAST_3_MONTHS -> "Last 3 Months"
                                            DateFilterOption.LAST_6_MONTHS -> "Last 6 Months"
                                            DateFilterOption.THIS_YEAR -> "This Year"
                                            DateFilterOption.CUSTOM_RANGE -> "Custom Date Range..."
                                            DateFilterOption.SPECIFIC_DATE -> "Specific Date..."
                                            DateFilterOption.SPECIFIC_MONTH -> "Specific Month..."
                                        }
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                viewModel.dateFilterOption.value = option
                                                dateDropdownExpanded = false
                                                if (option == DateFilterOption.CUSTOM_RANGE) {
                                                    showRangePicker = true
                                                } else if (option == DateFilterOption.SPECIFIC_DATE) {
                                                    showSingleDatePicker = true
                                                } else if (option == DateFilterOption.SPECIFIC_MONTH) {
                                                    showMonthPicker = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Show active summary
                            when (dateFilterOption) {
                                DateFilterOption.CUSTOM_RANGE -> {
                                    if (startDate != null && endDate != null) {
                                        val rangeStr = "${sdfShort.format(Date(startDate!!))} - ${sdfShort.format(Date(endDate!!))}"
                                        Text(
                                            text = "Selected: $rangeStr",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                DateFilterOption.SPECIFIC_DATE -> {
                                    if (selectedDate != null) {
                                        Text(
                                            text = "Selected: ${sdfShort.format(Date(selectedDate!!))}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                DateFilterOption.SPECIFIC_MONTH -> {
                                    if (selectedMonth != null) {
                                        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                                        val mName = months[selectedMonth!!]
                                        Text(
                                            text = "Selected: $mName ${selectedYear ?: ""}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                                else -> {}
                            }
                        }

                        // D. Categories Selector trigger
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            OutlinedButton(
                                onClick = { sheetView = FilterSheetView.CATEGORIES },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val label = if (selectedCategories.isEmpty()) "All Categories" else "${selectedCategories.size} selected"
                                Text("$label (Tap to select)")
                            }
                        }

                        // E. Amount Filters
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Amount",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            // Quick Amount Filters Row
                            Text(
                                text = "Quick Amount Filters",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val quickAmts = listOf(
                                    "Under $50" to AmountFilterOption.UNDER_50,
                                    "Under $100" to AmountFilterOption.UNDER_100,
                                    "Over $500" to AmountFilterOption.OVER_500,
                                    "Over $1000" to AmountFilterOption.OVER_1000
                                )
                                quickAmts.forEach { (label, option) ->
                                    val isSelected = amountFilterOption == option
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.amountFilterOption.value = if (isSelected) AmountFilterOption.ALL else option
                                        },
                                        label = { Text(label, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Custom Amount Filters Input + Slider
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Custom Amount Range",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = minAmount?.toString() ?: "",
                                    onValueChange = {
                                        viewModel.minAmount.value = it.toDoubleOrNull()
                                        viewModel.amountFilterOption.value = AmountFilterOption.CUSTOM
                                    },
                                    placeholder = { Text("Min", style = MaterialTheme.typography.bodyMedium) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                OutlinedTextField(
                                    value = maxAmount?.toString() ?: "",
                                    onValueChange = {
                                        viewModel.maxAmount.value = it.toDoubleOrNull()
                                        viewModel.amountFilterOption.value = AmountFilterOption.CUSTOM
                                    },
                                    placeholder = { Text("Max", style = MaterialTheme.typography.bodyMedium) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }

                            // Slider synchronization
                            val maxLimit = 2000f
                            var sliderPosition by remember(minAmount, maxAmount) {
                                mutableStateOf((minAmount?.toFloat() ?: 0f)..(maxAmount?.toFloat() ?: maxLimit))
                            }
                            RangeSlider(
                                value = sliderPosition,
                                onValueChange = { range ->
                                    sliderPosition = range
                                    viewModel.minAmount.value = range.start.toDouble()
                                    viewModel.maxAmount.value = range.end.toDouble()
                                    viewModel.amountFilterOption.value = AmountFilterOption.CUSTOM
                                },
                                valueRange = 0f..maxLimit,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // F. Sorting
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Sort By",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            val sorts = listOf(
                                SortOption.DATE_DESC to "Newest First",
                                SortOption.DATE_ASC to "Oldest First",
                                SortOption.AMOUNT_DESC to "Highest Amount",
                                SortOption.AMOUNT_ASC to "Lowest Amount",
                                SortOption.EXPENSE_DESC to "Largest Expense",
                                SortOption.INCOME_DESC to "Largest Income"
                            )
                            sorts.chunked(2).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    chunk.forEach { (option, label) ->
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.sortBy.value = option },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = sortBy == option,
                                                onClick = { viewModel.sortBy.value = option }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(label, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                    if (chunk.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // F. Apply / Reset Buttons Row
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearAllFilters()
                                showFilterSheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Filters")
                        }
                        Button(
                            onClick = { showFilterSheet = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply Filters")
                        }
                    }
                }
            } else {
                // CATEGORIES Selection View (Swapped dynamically, no nested sheets!)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Row with back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { sheetView = FilterSheetView.FILTERS }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select Categories",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Multi-select toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multiple Selection Mode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Switch(
                            checked = isMultiSelectCategory,
                            onCheckedChange = { viewModel.isMultiSelectCategory.value = it }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Categories List
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categories.forEach { cat ->
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex.ifBlank { "#71717A" }))
                            val isSelected = selectedCategories.contains(cat.id)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val current = selectedCategories.toMutableSet()
                                        if (isSelected) {
                                            current.remove(cat.id)
                                        } else {
                                            if (!isMultiSelectCategory) {
                                                current.clear()
                                            }
                                            current.add(cat.id)
                                        }
                                        viewModel.selectedCategories.value = current

                                        if (!isMultiSelectCategory) {
                                            sheetView = FilterSheetView.FILTERS
                                        }
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category Icon with colored background indicator tint
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            color = catColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(cat.iconName),
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Color dot indicator + Name
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color = catColor, shape = RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Back to main filters button
                    Button(
                        onClick = { sheetView = FilterSheetView.FILTERS },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Filters")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    onDismissRequest: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    val state = rememberDateRangePickerState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onDateRangeSelected(start, end)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            Box(modifier = Modifier.height(400.dp)) {
                DateRangePicker(
                    state = state,
                    title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
                    headline = { Text("Choose dates") },
                    showModeToggle = false
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecificDatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val date = state.selectedDateMillis
                    if (date != null) {
                        onDateSelected(date)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(
    onDismissRequest: () -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    
    val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val years = (2020..2030).toList()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onMonthYearSelected(selectedMonth, selectedYear) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        title = { Text("Select Month and Year") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Year Selector
                Text("Year Selector", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                var yearExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { yearExpanded = true }) {
                        Text(selectedYear.toString())
                    }
                    DropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                        years.forEach { y ->
                            DropdownMenuItem(text = { Text(y.toString()) }, onClick = { selectedYear = y; yearExpanded = false })
                        }
                    }
                }

                // Month Grid
                Text("Month Grid", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chunkedMonths = months.chunked(3)
                    chunkedMonths.forEachIndexed { rowIndex, rowList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowList.forEachIndexed { colIndex, mName ->
                                val actualMonthIndex = rowIndex * 3 + colIndex
                                val isSelected = selectedMonth == actualMonthIndex
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMonth = actualMonthIndex },
                                    label = { Text(mName) }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
