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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
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
    currencySymbol: String = "₹",
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

    LaunchedEffect(selectedAccount) {
        when (selectedAccount.type) {
            "CASH" -> selectedPaymentMethod = "Cash"
            "CREDIT_CARD", "CREDIT" -> selectedPaymentMethod = "Credit Card"
            "BANK" -> if (selectedPaymentMethod !in BANK_PAYMENT_METHODS) {
                selectedPaymentMethod = BANK_PAYMENT_METHODS[0]
            }
        }
    }

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
            // Save Section: Seamlessly integrated into page background surface & safe area
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
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
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Transaction Type Segmented Switcher (Expense / Income / Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
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

            // 2. Amount Hero Card (Visual Focal Point + Inline Calculator Action)
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "AMOUNT",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currencySymbol,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 32.sp
                                )
                            )
                            Text(
                                text = amountExpr,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 32.sp
                                ),
                                maxLines = 1
                            )
                        }

                        // Calculator Action Button: Positioned directly inline with the amount field
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isCalculatorExpanded) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable { isCalculatorExpanded = !isCalculatorExpanded },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = "Calculator",
                                tint = if (isCalculatorExpanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Expandable Keypad Grid
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
                                listOf("=")
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
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (key == "=") MaterialTheme.colorScheme.onSurface
                                                    else if (isOperator) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                )
                                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                                .clickable { handleKeypadInput(key) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = key,
                                                style = TextStyle(
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = if (key == "=") MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
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

            // 3. Transaction Title Card (Matches Card Background & Styling Exactly)
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TRANSACTION TITLE",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (title.isEmpty()) {
                                    Text(
                                        text = if (selectedType == TransactionType.INCOME) "e.g., Monthly Salary" else if (selectedType == TransactionType.TRANSFER) "e.g., Savings Transfer" else "Grocery Shopping",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            // 4. Category Card (Clear Hierarchy & Spacing)
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
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(category.iconName),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "CATEGORY (${if (selectedType == TransactionType.INCOME) "INCOME" else "EXPENSE"})",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = category.name,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Select",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 5. Account & Payment Method Cards (Breathing Room & Explicit Layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Account Card
                Box(modifier = Modifier.weight(1f)) {
                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedAccountMenu = true },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "ACCOUNT",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = selectedAccount.name,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Bal: $currencySymbol${df.format(selectedAccount.balance)}",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDown,
                                contentDescription = "Select Account",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
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

                // Payment Method Card
                Box(modifier = Modifier.weight(1f)) {
                    val isBank = selectedAccount.type == "BANK"
                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isBank) { expandedPaymentMenu = true },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CreditCard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "PAYMENT METHOD",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = selectedPaymentMethod,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (isBank) "Select Method" else "Auto-selected",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1
                                )
                            }

                            if (isBank) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDropDown,
                                    contentDescription = "Select Method",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
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

            // 6. Date & Time Selection Card
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "DATE & TIME",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Button Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text("Date", style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(dateString, style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface))
                                }
                            }
                        }

                        // Time Button Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text("Time", style = TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                    Text(timeString, style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface))
                                }
                            }
                        }
                    }
                }
            }

            // 7. Add Note Card
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ADD NOTE (OPTIONAL)",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        BasicTextField(
                            value = note,
                            onValueChange = { note = it },
                            textStyle = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (note.isEmpty()) {
                                    Text(
                                        text = "Add a note...",
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }
        }
    }
}
