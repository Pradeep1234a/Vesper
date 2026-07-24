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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onAddTransactionClick: () -> Unit = {}
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
        topBar = {
            RootHeader(
                title = "Transactions",
                onMenuClick = onMenuClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

                // Search Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        shape = MaterialTheme.shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
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
                                val accentColor = if (isIncome) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                val accentBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                                                            imageVector = Icons.Outlined.DeleteOutline,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            "Delete",
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                color = MaterialTheme.colorScheme.onSurface
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
