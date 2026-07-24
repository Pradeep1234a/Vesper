package com.vesper.ledger.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.PaymentMethod
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.SimpleDateFormat
import java.util.*

enum class DateTimeMode {
    AUTO_CURRENT,
    MANUAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currencySymbol: String = "$",
    categories: List<Category>,
    accounts: List<Account>,
    paymentMethods: List<PaymentMethod> = emptyList(),
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onSaveTransaction: (
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        accountName: String,
        paymentMethod: String,
        dateEpochMillis: Long,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }

    // Category selection
    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }
    }
    var selectedCategoryId by remember(filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: categories.firstOrNull()?.id ?: 1L)
    }

    // Account selection
    val activeAccounts = remember(accounts) { accounts.filter { !it.isHidden } }
    var selectedAccount by remember(activeAccounts) {
        mutableStateOf(activeAccounts.firstOrNull())
    }

    // Payment method selection
    var selectedPaymentMethod by remember(paymentMethods) {
        mutableStateOf(paymentMethods.firstOrNull()?.name ?: "Cash")
    }

    // Date & Time picker state (shadcn monocolor)
    var dateTimeMode by remember { mutableStateOf(DateTimeMode.AUTO_CURRENT) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Native Date Picker launcher
    val showDatePicker = {
        val cal = selectedCalendar
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance()
                newCal.timeInMillis = selectedCalendar.timeInMillis
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedCalendar = newCal
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Native Time Picker launcher
    val showTimePicker = {
        val cal = selectedCalendar
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance()
                newCal.timeInMillis = selectedCalendar.timeInMillis
                newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCal.set(Calendar.MINUTE, minute)
                selectedCalendar = newCal
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    var noteText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChildHeader(
                title = "Add Transaction",
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
            // NO ACCOUNTS WARNING BANNER
            if (activeAccounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Text(
                                text = "No Account Found",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Text(
                                text = "You must create at least one financial account (e.g. Cash, Bank, Credit Card) before logging transactions.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = PlusJakartaSansFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            ShButton(
                                text = "Add Account Now",
                                onClick = onAddAccountClick,
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.background
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // TRANSACTION TYPE SELECTOR (Expense vs Income vs Transfer)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (type == TransactionType.EXPENSE) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { type = TransactionType.EXPENSE },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Expense",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TransactionType.EXPENSE) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (type == TransactionType.INCOME) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { type = TransactionType.INCOME },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TransactionType.INCOME) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // AMOUNT INPUT FIELD
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount ($currencySymbol)", fontFamily = SpaceGroteskFamily) },
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

                    // TITLE INPUT FIELD
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Title / Merchant", fontFamily = SpaceGroteskFamily) },
                        placeholder = { Text("e.g. Coffee Shop, Salary, Groceries") },
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

                    // CATEGORY SELECTION SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATEGORY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )

                            Text(
                                text = "+ Add Category",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.clickable { onAddCategoryClick() }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredCategories.forEach { cat ->
                                val isSelected = cat.id == selectedCategoryId
                                val catColor = safeParseColor(cat.colorHex)

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getIconByName(cat.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.background else catColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = cat.name,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }

                    // ACCOUNT SELECTION SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACCOUNT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            )

                            Text(
                                text = "+ Add Account",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                modifier = Modifier.clickable { onAddAccountClick() }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeAccounts.forEach { acct ->
                                val isSelected = acct.id == selectedAccount?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAccount = acct },
                                    label = {
                                        Text(
                                            text = "${acct.name} (${acct.type})",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                        selectedLabelColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        }
                    }

                    // PAYMENT METHOD SELECTION
                    if (paymentMethods.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "PAYMENT METHOD",
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
                                paymentMethods.forEach { method ->
                                    val isSelected = method.name == selectedPaymentMethod
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedPaymentMethod = method.name },
                                        label = {
                                            Text(
                                                text = method.name,
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.onBackground,
                                            selectedLabelColor = MaterialTheme.colorScheme.background
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // SHADCN MONOCOLOR DATE & TIME PICKER SECTION
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "DATE & TIME",
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
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dateTimeMode == DateTimeMode.AUTO_CURRENT) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable {
                                        dateTimeMode = DateTimeMode.AUTO_CURRENT
                                        selectedCalendar = Calendar.getInstance()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Auto / Current",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateTimeMode == DateTimeMode.AUTO_CURRENT) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dateTimeMode == DateTimeMode.MANUAL) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { dateTimeMode = DateTimeMode.MANUAL },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Manual Picker",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateTimeMode == DateTimeMode.MANUAL) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        // Date & Time Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Date selector tile
                            ShCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (dateTimeMode == DateTimeMode.MANUAL) {
                                            showDatePicker()
                                        }
                                    },
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Date",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = dateFormatter.format(selectedCalendar.time),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            // Time selector tile
                            ShCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (dateTimeMode == DateTimeMode.MANUAL) {
                                            showTimePicker()
                                        }
                                    },
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Time",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        )
                                        Text(
                                            text = timeFormatter.format(selectedCalendar.time),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // NOTE FIELD
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Note (optional)", fontFamily = SpaceGroteskFamily) },
                        placeholder = { Text("Add transaction details...") },
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
                        text = "Save Transaction",
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            val acct = selectedAccount

                            if (amt <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            } else if (acct == null) {
                                Toast.makeText(context, "Please select or create an account", Toast.LENGTH_SHORT).show()
                            } else {
                                val timestamp = if (dateTimeMode == DateTimeMode.AUTO_CURRENT) {
                                    System.currentTimeMillis()
                                } else {
                                    selectedCalendar.timeInMillis
                                }

                                onSaveTransaction(
                                    titleText.trim(),
                                    amt,
                                    type,
                                    selectedCategoryId,
                                    acct.id,
                                    acct.name,
                                    selectedPaymentMethod,
                                    timestamp,
                                    noteText.trim()
                                )

                                Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
