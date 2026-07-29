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

    val isFabVisible by com.vesper.ledger.ui.components.rememberFabVisibility(listState)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SpeedDialFab(
                onActionSelected = { type ->
                    viewModel.selectedType.value = type
                    onAddTransactionClick()
                },
                visible = isFabVisible,
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

                // Modern M3 Section-Wise Draggable BottomSheet Filter System
                if (showFilterBottomSheet) {
                    M3TransactionFilterSheet(
                        viewModel = viewModel,
                        categories = categories,
                        accounts = accounts,
                        currencySymbol = currencySymbol,
                        filteredCount = transactions.size,
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
// ACTIVE FILTER CHIP HELPER FOR MAIN SCREEN BAR
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}
