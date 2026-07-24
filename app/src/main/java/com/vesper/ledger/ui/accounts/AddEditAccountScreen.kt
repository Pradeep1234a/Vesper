package com.vesper.ledger.ui.accounts

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

val ACCOUNT_TYPES = listOf("CASH", "BANK", "CREDIT_CARD", "SAVINGS", "INVESTMENT", "OTHER")

val ACCOUNT_ICONS = mapOf(
    "account_balance_wallet" to Icons.Outlined.AccountBalanceWallet,
    "account_balance" to Icons.Outlined.AccountBalance,
    "credit_card" to Icons.Outlined.CreditCard,
    "savings" to Icons.Outlined.Savings,
    "trending_up" to Icons.Outlined.TrendingUp,
    "payments" to Icons.Outlined.Payments,
    "work" to Icons.Outlined.WorkOutline,
    "store" to Icons.Outlined.Storefront
)

fun getAccountIcon(name: String): ImageVector {
    return ACCOUNT_ICONS[name] ?: Icons.Outlined.AccountBalanceWallet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    accountToEdit: Account? = null,
    currencySymbol: String = "$",
    onBackClick: () -> Unit,
    onSaveAccount: (name: String, type: String, initialBalance: Double, iconName: String, notes: String?, isHidden: Boolean, accountIdToUpdate: Long?) -> Unit,
    onDeleteAccount: ((Account) -> Unit)? = null
) {
    val context = LocalContext.current
    val isEditMode = accountToEdit != null

    var nameText by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var selectedType by remember { mutableStateOf(accountToEdit?.type ?: "CASH") }
    var initialBalanceText by remember { mutableStateOf(accountToEdit?.initialBalance?.toString() ?: "0.00") }
    var selectedIcon by remember { mutableStateOf(accountToEdit?.iconName ?: "account_balance_wallet") }
    var notesText by remember { mutableStateOf(accountToEdit?.notes ?: "") }
    var isHidden by remember { mutableStateOf(accountToEdit?.isHidden ?: false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && accountToEdit != null && onDeleteAccount != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Account",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${accountToEdit.name}'? Transactions associated with this account will remain logged.",
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(accountToEdit)
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = SpaceGroteskFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChildHeader(
                title = if (isEditMode) "Edit Account" else "Add Account",
                onBackClick = onBackClick
            )
        }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // LIVE ACCOUNT PREVIEW CARD
                Text(
                    text = "LIVE PREVIEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )

                val parsedBalance = initialBalanceText.toDoubleOrNull() ?: 0.0
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getAccountIcon(selectedIcon),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nameText.ifBlank { "Account Name" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (nameText.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = selectedType.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                if (isHidden) {
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
                        }

                        Text(
                            text = "$currencySymbol${String.format("%.2f", parsedBalance)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }

                // ACCOUNT NAME INPUT
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Account Name", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. Cash Wallet, Main Bank, Credit Card") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // ACCOUNT TYPE SELECTOR CHIPS
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ACCOUNT TYPE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ACCOUNT_TYPES.forEach { type ->
                            val isSelected = type == selectedType
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = type },
                                label = {
                                    Text(
                                        text = type.replace("_", " "),
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // INITIAL BALANCE FIELD
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Initial Balance ($currencySymbol)", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // ACCOUNT ICON SELECTOR
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ACCOUNT ICON",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ACCOUNT_ICONS.forEach { (iconKey, vector) ->
                            val isSelected = iconKey == selectedIcon
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIcon = iconKey },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = iconKey,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // HIDE / TURN OFF ACCOUNT SWITCH
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hide Account",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Excludes balance from Dashboard total balance sum.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Switch(
                            checked = isHidden,
                            onCheckedChange = { isHidden = it }
                        )
                    }
                }

                // NOTES FIELD
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Account Notes (optional)", fontFamily = SpaceGroteskFamily) },
                    placeholder = { Text("e.g. Account number, Bank branch, Notes...") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // SAVE BUTTON
                ShButton(
                    text = if (isEditMode) "Save Changes" else "Create Account",
                    onClick = {
                        val trimmedName = nameText.trim()
                        val balance = initialBalanceText.toDoubleOrNull() ?: 0.0

                        if (trimmedName.isNotBlank()) {
                            onSaveAccount(
                                trimmedName,
                                selectedType,
                                balance,
                                selectedIcon,
                                notesText.ifBlank { null },
                                isHidden,
                                accountToEdit?.id
                            )
                            Toast.makeText(
                                context,
                                if (isEditMode) "Account updated!" else "Account created!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackClick()
                        } else {
                            Toast.makeText(context, "Please enter an account name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )

                // DELETE BUTTON (If Edit Mode)
                if (isEditMode && accountToEdit != null && onDeleteAccount != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Account",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Account",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
