package com.vesper.ledger.ui.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat

private val ACCOUNT_COLORS = listOf(
    "#2563EB", "#16A34A", "#DC2626", "#D97706", "#9333EA",
    "#059669", "#0D9488", "#EA580C", "#DB2777", "#4F46E5",
    "#0891B2", "#65A30D", "#CA8A04", "#71717A"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    editingAccount: Account? = null,
    onBackClick: () -> Unit,
    onSaveAccount: (name: String, type: String, balance: Double, colorHex: String, includeInTotal: Boolean) -> Unit
) {
    val df = DecimalFormat("#,##0.00")
    val isEditing = editingAccount != null

    var name by remember { mutableStateOf(editingAccount?.name ?: "") }
    var type by remember { mutableStateOf(editingAccount?.type ?: "BANK") }
    var balanceText by remember { mutableStateOf(editingAccount?.initialBalance?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var colorHex by remember { mutableStateOf(editingAccount?.colorHex ?: "#2563EB") }
    var includeInTotal by remember { mutableStateOf(editingAccount?.includeInTotal ?: true) }

    val iconName = when (type) {
        "CASH" -> "payments"
        "CREDIT_CARD", "CREDIT" -> "credit_card"
        else -> "account_balance"
    }

    val parsedBalance = balanceText.toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank()

    val accountColor = safeParseColor(colorHex)

    Scaffold(
        topBar = {
            ChildHeader(
                title = if (isEditing) "Edit Account" else "Add Account",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                ) {
                    ShButton(
                        text = if (isEditing) "Save Changes" else "Create Account",
                        onClick = {
                            if (isSaveEnabled) {
                                onSaveAccount(
                                    name.trim(),
                                    type,
                                    parsedBalance,
                                    colorHex,
                                    includeInTotal
                                )
                            }
                        },
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Account Preview Card
            Text(
                text = "PREVIEW",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            )

            ShCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, accountColor, RoundedCornerShape(16.dp)),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accountColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(iconName),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = name.ifBlank { "Account Name" },
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Starting Bal: ₹${df.format(parsedBalance)}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Surface(
                        color = accountColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = type,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accountColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 2. Account Type Selector
            Text(
                text = "ACCOUNT TYPE",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("BANK" to "Bank", "CASH" to "Cash", "CREDIT_CARD" to "Credit Card").forEach { (typeKey, label) ->
                    val selected = type == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                            .clickable { type = typeKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3. Account Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g. HDFC Salary, Cash Wallet") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // 4. Starting Balance Input
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("Starting Balance") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // 5. Account Color Swatches
            Text(
                text = "ACCOUNT COLOR",
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ACCOUNT_COLORS.take(7).forEach { hex ->
                    val color = safeParseColor(hex)
                    val isSelected = colorHex.equals(hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { colorHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 6. Include in Total Calculated Balance Toggle
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Include in Total Balance",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Include this account's balance in your home screen total balance card.",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Switch(
                        checked = includeInTotal,
                        onCheckedChange = { includeInTotal = it }
                    )
                }
            }
        }
    }
}
