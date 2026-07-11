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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit,
    onAddTransactionClick: (type: String?, id: Long?) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val selectedDate by viewModel.selectedDate.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val minAmount by viewModel.minAmount.collectAsState()
    val maxAmount by viewModel.maxAmount.collectAsState()
    val singleAmount by viewModel.singleAmount.collectAsState()

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

                val activeFiltersCount = (if (selectedCategory != null) 1 else 0) +
                        (if (selectedType != null) 1 else 0) +
                        (if (selectedDate != null) 1 else 0) +
                        (if (startDate != null && endDate != null) 1 else 0) +
                        (if (selectedMonth != null) 1 else 0) +
                        (if (minAmount != null || maxAmount != null) 1 else 0) +
                        (if (singleAmount != null) 1 else 0)
                
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
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
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
                        Text("Clear All", color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Divider
                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Sort By Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    var expandedSortDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedSortDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val currentSortLabel = when (sortBy) {
                                SortOption.DATE_DESC -> "Newest First"
                                SortOption.DATE_ASC -> "Oldest First"
                                SortOption.AMOUNT_DESC -> "Highest Amount"
                                SortOption.AMOUNT_ASC -> "Lowest Amount"
                            }
                            Text(currentSortLabel)
                        }
                        DropdownMenu(
                            expanded = expandedSortDropdown,
                            onDismissRequest = { expandedSortDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Newest First") },
                                onClick = {
                                    viewModel.sortBy.value = SortOption.DATE_DESC
                                    expandedSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Oldest First") },
                                onClick = {
                                    viewModel.sortBy.value = SortOption.DATE_ASC
                                    expandedSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Highest Amount") },
                                onClick = {
                                    viewModel.sortBy.value = SortOption.AMOUNT_DESC
                                    expandedSortDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Lowest Amount") },
                                onClick = {
                                    viewModel.sortBy.value = SortOption.AMOUNT_ASC
                                    expandedSortDropdown = false
                                }
                            )
                        }
                    }
                }

                // 1. Transaction Type Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Transaction Type",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedType == null,
                            onClick = { viewModel.selectedType.value = null },
                            label = { Text("All") }
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { viewModel.selectedType.value = TransactionType.INCOME },
                            label = { Text("Income") }
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { viewModel.selectedType.value = TransactionType.EXPENSE },
                            label = { Text("Expenses") }
                        )
                    }
                }

                // 2. Category Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    var expandedCatDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedCatDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val activeCatName = categories.find { it.id == selectedCategory }?.name ?: "All Categories"
                            Text(activeCatName)
                        }
                        DropdownMenu(
                            expanded = expandedCatDropdown,
                            onDismissRequest = { expandedCatDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Categories") },
                                onClick = {
                                    viewModel.selectedCategory.value = null
                                    expandedCatDropdown = false
                                }
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        viewModel.selectedCategory.value = cat.id
                                        expandedCatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Date Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Date Filters",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    val calendar = Calendar.getInstance()
                    val datePickerDialogSingle = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                            }
                            viewModel.selectedDate.value = cal.timeInMillis
                            viewModel.startDate.value = null
                            viewModel.endDate.value = null
                            viewModel.selectedMonth.value = null
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    val datePickerDialogStart = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                            }
                            viewModel.startDate.value = cal.timeInMillis
                            viewModel.selectedDate.value = null
                            viewModel.selectedMonth.value = null
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    val datePickerDialogEnd = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance().apply {
                                set(year, month, day, 23, 59, 59)
                            }
                            viewModel.endDate.value = cal.timeInMillis
                            viewModel.selectedDate.value = null
                            viewModel.selectedMonth.value = null
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { datePickerDialogSingle.show() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(selectedDate?.let { sdfShort.format(java.util.Date(it)) } ?: "Single Date")
                        }
                        
                        OutlinedButton(
                            onClick = { datePickerDialogStart.show() },
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text(startDate?.let { sdfShort.format(java.util.Date(it)) } ?: "Start Date")
                        }

                        OutlinedButton(
                            onClick = { datePickerDialogEnd.show() },
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Text(endDate?.let { sdfShort.format(java.util.Date(it)) } ?: "End Date")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        var expandedMonthDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedMonthDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val activeMonthName = selectedMonth?.let { months[it] } ?: "Filter by Month"
                                Text(activeMonthName)
                            }
                            DropdownMenu(
                                expanded = expandedMonthDropdown,
                                onDismissRequest = { expandedMonthDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("No Month Filter") },
                                    onClick = {
                                        viewModel.selectedMonth.value = null
                                        expandedMonthDropdown = false
                                    }
                                )
                                months.forEachIndexed { index, mName ->
                                    DropdownMenuItem(
                                        text = { Text(mName) },
                                        onClick = {
                                            viewModel.selectedMonth.value = index
                                            viewModel.selectedDate.value = null
                                            viewModel.startDate.value = null
                                            viewModel.endDate.value = null
                                            expandedMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Amount Filters",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    
                    OutlinedTextField(
                        value = singleAmount?.toString() ?: "",
                        onValueChange = {
                            viewModel.singleAmount.value = it.toDoubleOrNull()
                            if (it.isNotBlank()) {
                                viewModel.minAmount.value = null
                                viewModel.maxAmount.value = null
                            }
                        },
                        placeholder = { Text("Exact Amount (e.g. 50.0)", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
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
                                if (it.isNotBlank()) {
                                    viewModel.singleAmount.value = null
                                }
                            },
                            placeholder = { Text("Min Amount", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
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
                                if (it.isNotBlank()) {
                                    viewModel.singleAmount.value = null
                                }
                            },
                            placeholder = { Text("Max Amount", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}
