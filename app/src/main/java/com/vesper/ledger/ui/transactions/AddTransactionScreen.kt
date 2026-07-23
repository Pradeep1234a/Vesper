package com.vesper.ledger.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.QrCodeScanner
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
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AccountOption(
    val name: String,
    val type: String, // CASH, BANK, CREDIT_CARD
    val balance: Double,
    val iconName: String = "account_balance_wallet"
)

val BANK_PAYMENT_METHODS = listOf("UPI / GPay / PhonePe", "Debit Card", "Net Banking", "Bank Transfer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currencySymbol: String = "$",
    accountsList: List<Account> = emptyList(),
    onBackClick: () -> Unit,
    onOpenCategorySelection: (TransactionType, String) -> Unit,
    selectedCategory: CategoryOption?,
    onSaveTransaction: (
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        dateEpochMillis: Long,
        accountName: String,
        paymentMethod: String,
        note: String
    ) -> Unit
) {
    val context = LocalContext.current
    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    // Dynamically map database accounts loaded from AccountRepository
    val availableAccounts = remember(accountsList) {
        if (accountsList.isNotEmpty()) {
            accountsList.map { acc ->
                AccountOption(
                    name = acc.name,
                    type = acc.type,
                    balance = acc.initialBalance,
                    iconName = acc.iconName
                )
            }
        } else {
            listOf(
                AccountOption("Cash Wallet", "CASH", 3420.00, "payments"),
                AccountOption("HDFC Bank Account", "BANK", 45280.00, "account_balance"),
                AccountOption("ICICI Bank Account", "BANK", 28150.00, "account_balance"),
                AccountOption("SBI Bank Account", "BANK", 18900.00, "account_balance"),
                AccountOption("HDFC Regalia Credit Card", "CREDIT_CARD", 65000.00, "credit_card")
            )
        }
    }

    // Form States
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var title by remember { mutableStateOf("") }
    var amountExpr by remember { mutableStateOf("0") }
    var isCalculatorExpanded by remember { mutableStateOf(false) }

    // Account & Payment Method Dropdown States
    var selectedAccount by remember(availableAccounts) { mutableStateOf(availableAccounts.first()) }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var expandedAccountMenu by remember { mutableStateOf(false) }
    var expandedPaymentMenu by remember { mutableStateOf(false) }

    var note by remember { mutableStateOf("") }

    // Date & Time States
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var dateString by remember { mutableStateOf(dateFormat.format(calendar.time)) }
    var timeString by remember { mutableStateOf(timeFormat.format(calendar.time)) }

    // Auto-update payment method choices when account changes
    LaunchedEffect(selectedAccount) {
        when (selectedAccount.type) {
            "CASH" -> selectedPaymentMethod = "Cash"
            "CREDIT_CARD", "CREDIT" -> selectedPaymentMethod = "Credit Card"
            "BANK" -> if (selectedPaymentMethod !in BANK_PAYMENT_METHODS) {
                selectedPaymentMethod = BANK_PAYMENT_METHODS[0]
            }
        }
    }

    // Evaluates built-in calculator mathematical expressions
    fun evaluateAmount(): Double {
        return try {
            val expr = amountExpr.replace("×", "*").replace("÷", "/")
            if (expr.contains("+") || expr.contains("-") || expr.contains("*") || expr.contains("/")) {
                val tokens = expr.split(Regex("(?<=[+\\-*/])|(?=[+\\-*/])"))
                var res = tokens.getOrNull(0)?.trim()?.toDoubleOrNull() ?: 0.0
                var i = 1
                while (i < tokens.size - 1) {
                    val op = tokens[i].trim()
                    val nextVal = tokens[i + 1].trim().toDoubleOrNull() ?: 0.0
                    when (op) {
                        "+" -> res += nextVal
                        "-" -> res -= nextVal
                        "*" -> res *= nextVal
                        "/" -> if (nextVal != 0.0) res /= nextVal
                    }
                    i += 2
                }
                res
            } else {
                expr.toDoubleOrNull() ?: 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    fun handleKeypadInput(key: String) {
        when (key) {
            "C" -> amountExpr = "0"
            "⌫" -> {
                amountExpr = if (amountExpr.length > 1) amountExpr.dropLast(1) else "0"
            }
            "=" -> {
                val evaluated = evaluateAmount()
                amountExpr = if (evaluated % 1.0 == 0.0) evaluated.toLong().toString() else String.format(Locale.US, "%.2f", evaluated)
            }
            "+", "-", "×", "÷" -> {
                if (amountExpr.isNotEmpty() && !amountExpr.endsWith(" ") && !amountExpr.endsWith("+") && !amountExpr.endsWith("-") && !amountExpr.endsWith("×") && !amountExpr.endsWith("÷")) {
                    amountExpr += key
                }
            }
            else -> {
                amountExpr = if (amountExpr == "0") key else amountExpr + key
            }
        }
    }

    // Launch Android Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            dateString = dateFormat.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Launch Android Time Picker
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            timeString = timeFormat.format(calendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    // Dynamically match active category based on active tab type
    val defaultCat = if (selectedType == TransactionType.INCOME) DEFAULT_INCOME_CATEGORIES.first() else DEFAULT_EXPENSE_CATEGORIES.first()
    val category = selectedCategory?.takeIf { it.type == selectedType } ?: defaultCat

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Transactions",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ShButton(
                        text = "Save Transaction",
                        onClick = {
                            val finalAmount = evaluateAmount()
                            val finalTitle = title.ifBlank { if (selectedType == TransactionType.TRANSFER) "Account Transfer" else category.name }
                            onSaveTransaction(
                                finalTitle,
                                finalAmount,
                                selectedType,
                                if (selectedType == TransactionType.TRANSFER) 0L else category.id,
                                calendar.timeInMillis,
                                selectedAccount.name,
                                selectedPaymentMethod,
                                note
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
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
            // Segmented Transaction Type Tab Switcher (Expense / Income / Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val label = when (type) {
                        TransactionType.EXPENSE -> "Expense"
                        TransactionType.INCOME -> "Income"
                        TransactionType.TRANSFER -> "Transfer"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount Display & Built-in Calculator Card (Shadcn UI Minimalist)
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        IconButton(
                            onClick = { isCalculatorExpanded = !isCalculatorExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "Calculator",
                                tint = if (isCalculatorExpanded) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = amountExpr,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 32.sp
                            ),
                            maxLines = 1
                        )
                    }

                    // Expandable Built-in Interactive Calculator Grid
                    AnimatedVisibility(visible = isCalculatorExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            val keypadRows = listOf(
                                listOf("7", "8", "9", "÷"),
                                listOf("4", "5", "6", "×"),
                                listOf("1", "2", "3", "-"),
                                listOf("C", "0", "⌫", "+"),
                                listOf(".")
                            )

                            keypadRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { key ->
                                        val isOperator = key in listOf("+", "-", "×", "÷", "=", "C", "⌫")
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isOperator) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                .clickable { handleKeypadInput(key) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
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

            // Transaction Title Input Field (Shadcn UI style)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Transaction Title") },
                placeholder = { Text(if (selectedType == TransactionType.INCOME) "e.g., Monthly Salary" else if (selectedType == TransactionType.TRANSFER) "e.g., Savings Transfer" else "e.g., Grocery Shopping at DMart") },
                leadingIcon = {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Category Selection Button (HIDDEN FOR TRANSFERS)
            if (selectedType != TransactionType.TRANSFER) {
                ShCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCategorySelection(selectedType, category.name) },
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.onBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(category.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Category (${if (selectedType == TransactionType.INCOME) "Income" else "Expense"})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Text(
                            text = "Select",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Separate Menu Type Selectors for Account & Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Account Selection Dropdown Menu Card
                Box(modifier = Modifier.weight(1f)) {
                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedAccountMenu = true },
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Account",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedAccount.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Bal: $currencySymbol${df.format(selectedAccount.balance)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select Account", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    DropdownMenu(
                        expanded = expandedAccountMenu,
                        onDismissRequest = { expandedAccountMenu = false }
                    ) {
                        availableAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.name, fontWeight = FontWeight.Bold)
                                        Text("Bal: $currencySymbol${df.format(acc.balance)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedAccount = acc
                                    expandedAccountMenu = false
                                }
                            )
                        }
                    }
                }

                // 2. Payment Method Selection Dropdown Menu Card
                Box(modifier = Modifier.weight(1f)) {
                    val isBank = selectedAccount.type == "BANK"
                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isBank) { expandedPaymentMenu = true },
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Payment Method",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedPaymentMethod,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isBank) "Select Method" else "Auto-Selected",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            if (isBank) {
                                Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select Method", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    if (isBank) {
                        DropdownMenu(
                            expanded = expandedPaymentMenu,
                            onDismissRequest = { expandedPaymentMenu = false }
                        ) {
                            BANK_PAYMENT_METHODS.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedPaymentMethod = method
                                        expandedPaymentMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Date & Time Pickers Card
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "DATE & TIME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Picker Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Date", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(dateString, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface))
                                }
                            }
                        }

                        // Time Picker Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Time", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(timeString, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface))
                                }
                            }
                        }
                    }
                }
            }

            // Transaction Note Field
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / Tag (Optional)") },
                placeholder = { Text("Add transaction description, tags or merchant location...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
