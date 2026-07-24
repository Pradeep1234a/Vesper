package com.vesper.ledger.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    accounts: List<Account>,
    transactions: List<Transaction>,
    currencySymbol: String = "$",
    onBackClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onEditAccountClick: (Account) -> Unit,
    onToggleHideAccount: (Account) -> Unit
) {
    // Calculate total balance from non-hidden accounts
    val totalActiveBalance = remember(accounts, transactions) {
        accounts.filter { !it.isHidden }.sumOf { account ->
            val acctIncome = transactions.filter { it.accountId == account.id && it.type == TransactionType.INCOME }.sumOf { it.amount }
            val acctExpense = transactions.filter { it.accountId == account.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val transfersOut = transactions.filter { it.accountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            val transfersIn = transactions.filter { it.targetAccountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            account.initialBalance + acctIncome - acctExpense - transfersOut + transfersIn
        }
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Accounts",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Account"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // TOTAL ACCOUNTS BALANCE BANNER CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column {
                        Text(
                            text = "TOTAL ACCOUNTS BALANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "$currencySymbol${String.format("%,.2f", totalActiveBalance)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${accounts.count { !it.isHidden }} active accounts • Sum of all unhidden account balances",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = PlusJakartaSansFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                if (accounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No accounts created yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add your first Cash, Bank or Credit Card account.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PlusJakartaSansFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(accounts, key = { it.id }) { account ->
                            val currentAccountBalance = remember(account, transactions) {
                                val acctIncome = transactions.filter { it.accountId == account.id && it.type == TransactionType.INCOME }.sumOf { it.amount }
                                val acctExpense = transactions.filter { it.accountId == account.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                                val transfersOut = transactions.filter { it.accountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
                                val transfersIn = transactions.filter { it.targetAccountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
                                account.initialBalance + acctIncome - acctExpense - transfersOut + transfersIn
                            }

                            ShCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditAccountClick(account) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (account.isHidden) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            )
                                            .border(
                                                1.dp,
                                                if (account.isHidden) MaterialTheme.colorScheme.outlineVariant
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                                RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getAccountIcon(account.iconName),
                                            contentDescription = null,
                                            tint = if (account.isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = account.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = if (account.isHidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = account.type.replace("_", " "),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }

                                            if (account.isHidden) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "HIDDEN",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontFamily = SpaceGroteskFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = account.notes?.takeIf { it.isNotBlank() } ?: "Initial balance: $currencySymbol${String.format("%.2f", account.initialBalance)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PlusJakartaSansFamily,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$currencySymbol${String.format("%,.2f", currentAccountBalance)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = if (account.isHidden) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onToggleHideAccount(account) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (account.isHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                                    contentDescription = "Toggle Hide",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = "Edit Account",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}
