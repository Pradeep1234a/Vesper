package com.vesper.ledger.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.getIconByName
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.dateEpochMillis))
        }
    }

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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filtering Row: Types
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == null,
                        onClick = { viewModel.selectedType.value = null },
                        label = { Text("All Types") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { viewModel.selectedType.value = TransactionType.INCOME },
                        label = { Text("Income") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.selectedType.value = TransactionType.EXPENSE },
                        label = { Text("Expenses") }
                    )
                }
            }

            // Filtering Row: Categories
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectedCategory.value = null },
                            label = { Text("All Categories") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.id,
                            onClick = { viewModel.selectedCategory.value = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            // Sort Toggle Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    viewModel.sortBy.value = when (sortBy) {
                        SortOption.DATE_DESC -> SortOption.DATE_ASC
                        SortOption.DATE_ASC -> SortOption.AMOUNT_DESC
                        SortOption.AMOUNT_DESC -> SortOption.AMOUNT_ASC
                        SortOption.AMOUNT_ASC -> SortOption.DATE_DESC
                    }
                }) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Sort")
                }
                Text(
                    text = when (sortBy) {
                        SortOption.DATE_DESC -> "Newest First"
                        SortOption.DATE_ASC -> "Oldest First"
                        SortOption.AMOUNT_DESC -> "Highest Amount"
                        SortOption.AMOUNT_ASC -> "Lowest Amount"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp)
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
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
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
                            val category = categories.find { it.id == tx.categoryId }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { transactionToDelete = tx }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Color(android.graphics.Color.parseColor(category?.colorHex ?: "#71717A")).copy(alpha = 0.15f),
                                                shape = MaterialTheme.shapes.small
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(category?.iconName ?: "category"),
                                            contentDescription = category?.name,
                                            tint = Color(android.graphics.Color.parseColor(category?.colorHex ?: "#71717A")),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = category?.name ?: "Uncategorized",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val prefix = if (tx.type == TransactionType.INCOME) "+" else "-"
                                    val color = if (tx.type == TransactionType.INCOME) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface
                                    Text(
                                        text = "$prefix$currencySymbol${df.format(tx.amount)}",
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            fontSize = 16.sp,
                                            color = color
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { transactionToDelete = tx }
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
