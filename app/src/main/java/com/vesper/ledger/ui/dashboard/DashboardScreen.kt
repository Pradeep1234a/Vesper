package com.vesper.ledger.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    currencySymbol: String,
    onAddTransactionClick: () -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSavingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val df = DecimalFormat("#,##0.00")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Vesper",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Available Balance Bento Card
            item {
                ShCard {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${df.format(uiState.availableBalance)}",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            // Bento Grid Row: Income & Expenses
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Income Bento
                    ShCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Income",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${df.format(uiState.totalIncome)}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color(0xFF16A34A) // Semantic Green
                            )
                        )
                    }

                    // Expense Bento
                    ShCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expenses",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${df.format(uiState.totalExpense)}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                color = Color(0xFFDC2626) // Semantic Red
                            )
                        )
                    }
                }
            }

            // Savings Bento Card
            item {
                ShCard(
                    modifier = Modifier.clickable { onSavingsClick() }
                ) {
                    Text(
                        text = "Total Saved",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${df.format(uiState.totalSaved)}",
                        style = MaterialTheme.typography.displayMedium.copy(
                            color = Color(0xFF2563EB) // Semantic Blue
                        )
                    )
                }
            }

            // Recent Transactions Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { onSeeAllTransactionsClick() }
                    )
                }
            }

            // Recent Transactions List
            if (uiState.recentTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent transactions.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            } else {
                items(uiState.recentTransactions) { tx ->
                    TransactionRow(
                        transaction = tx,
                        currencySymbol = currencySymbol,
                        df = df
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    currencySymbol: String,
    df: DecimalFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = transaction.note.ifBlank { "No notes" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        
        val prefix = if (transaction.type == TransactionType.INCOME) "+" else "-"
        val color = if (transaction.type == TransactionType.INCOME) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurface

        Text(
            text = "$prefix$currencySymbol${df.format(transaction.amount)}",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 18.sp,
                color = color
            )
        )
    }
}
