package com.vesper.ledger.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val id: Long = 0,
    val name: String,
    val type: String, // CASH, BANK, CREDIT_CARD
    val balance: Double,
    val iconName: String = "account_balance_wallet",
    val colorHex: String = "#2563EB",
    val includeInTotal: Boolean = true,
    val isHidden: Boolean = false
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
    onAddNewAccount: (name: String, type: String, initialBalance: Double) -> Unit = { _, _, _ -> },
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

    // Filter out hidden accounts
    val availableAccounts = remember(accountsList) {
        val visible = accountsList.filter { !it.isHidden }
        if (visible.isNotEmpty()) {
            visible.map { acc ->
                AccountOption(
                    id = acc.id,
                    name = acc.name,
                    type = acc.type,
                    balance = acc.initialBalance,
                    iconName = acc.iconName,
                    colorHex = acc.colorHex,
                    includeInTotal = acc.includeInTotal,
                    isHidden = acc.isHidden
                )
            }
        } else {
            listOf(
                AccountOption(1L, "Cash Wallet", "CASH", 3420.00, "payments", "#16A34A"),
                AccountOption(2L, "HDFC Bank Account", "BANK", 45280.00, "account_balance", "#2563EB"),
                AccountOption(3L, "ICICI Bank Account", "BANK", 28150.00, "account_balance", "#059669"),
                AccountOption(4L, "SBI Bank Account", "BANK", 18900.00, "account_balance", "#D97706"),
                AccountOption(5L, "HDFC Regalia Credit Card", "CREDIT_CARD", 65000.00, "credit_card", "#DC2626")
            )
        }
    }

    // Form States
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var title by remember { mutableStateOf("") }
    var amountExpr by remember { mutableStateOf("0") }
    var isCalculatorExpanded by remember { mutableStateOf(false) }

    // Account Modal Bottom Sheet & Selection States
    var selectedAccount by remember(availableAccounts) { mutableStateOf(availableAccounts.firstOrNull() ?: AccountOption(name = "Cash Wallet", type = "CASH", balance = 0.0)) }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var showAccountBottomSheet by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var expandedPaymentMenu by remember { mutableStateOf(false) }

    // Add Account Dialog Form Inputs
    var newAccName by remember { mutableStateOf("") }
    var newAccType by remember { mutableStateOf("BANK") }
    var newAccBalance by remember { mutableStateOf("") }

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

    // Category Resolution Fix: Ensure correct default category for Income vs Expense
    val defaultCat = if (selectedType == TransactionType.INCOME) DEFAULT_INCOME_CATEGORIES.first() else DEFAULT_EXPENSE_CATEGORIES.first()
    val category = selectedCategory?.takeIf { it.type == selectedType } ?: defaultCat

    val isAmountEntered = amountExpr.isNotBlank() && amountExpr != "0"
    val amountTextColor = if (isAmountEntered) {
        if (selectedType == TransactionType.EXPENSE) Color(0xFFEF4444) else if (selectedType == TransactionType.INCOME) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    val isValidToSave = evaluateAmount() > 0.0

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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShButton(
                        text = "Save Transaction",
                        onClick = {
                            val finalAmount = evaluateAmount()
                            if (finalAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount greater than 0", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }

                            // Category Name Fallback for Title
                            val finalTitle = title.ifBlank { category.name }

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
                        containerColor = if (isValidToSave) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isValidToSave) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                        enabled = isValidToSave,
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .padding(vertical = 9.dp),
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

            // 2. Amount Hero Card (Vibrant Red/Green Color Roles + Keyboard Suppressed when Calculator Open)
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "AMOUNT",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currencySymbol,
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = amountTextColor,
                                    fontSize = 30.sp
                                )
                            )
                            BasicTextField(
                                value = amountExpr,
                                onValueChange = { newValue ->
                                    if (!isCalculatorExpanded) {
                                        if (newValue.isEmpty()) {
                                            amountExpr = "0"
                                        } else if (newValue.all { it.isDigit() || it == '.' || it == '+' || it == '-' || it == '×' || it == '÷' || it == '*' || it == '/' }) {
                                            amountExpr = if (amountExpr == "0" && newValue.length > 1 && !newValue.endsWith(".")) newValue.drop(1) else newValue
                                        }
                                    }
                                },
                                textStyle = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = amountTextColor,
                                    fontSize = 30.sp
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                readOnly = isCalculatorExpanded,
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                            )
                        }

                        // Calculator Action Button
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
                                .padding(top = 10.dp)
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
                                                .height(42.dp)
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

            // 3. Title Input Field (Category Name Fallback if Blank)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        text = "Title",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                placeholder = {
                    Text(
                        text = "e.g., ${category.name}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // 4. Category Card (Vibrant Icon Badges & Selection)
            if (selectedType != TransactionType.TRANSFER) {
                ShCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenCategorySelection(selectedType, category.name) },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Text(
                                    text = category.name,
                                    style = TextStyle(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
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
                                    fontSize = 12.sp,
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

            // 5. Account & Payment Method Cards (No Hardcoded Account Fallbacks)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Account Card
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ShCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showAccountBottomSheet = true },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(selectedAccount.iconName),
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
                                    ),
                                    maxLines = 1
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
                }

                // Payment Method Card
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val isBank = selectedAccount.type == "BANK"
                    ShCard(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = isBank) { expandedPaymentMenu = true },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
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
                                    ),
                                    maxLines = 1
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
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "DATE & TIME",
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
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Date Button Container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(10.dp)
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
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(10.dp)
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
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ADD NOTE (OPTIONAL)",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
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

    // ─── Modal Bottom Sheet Account Selector ─────────────────────────
    if (showAccountBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Account",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "${availableAccounts.size} Active Accounts",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Add New Account Action Button
                    TextButton(
                        onClick = { showAddAccountDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Add Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(availableAccounts) { acc ->
                        val isSelected = acc.name.equals(selectedAccount.name, ignoreCase = true)

                        ShCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedAccount = acc
                                    showAccountBottomSheet = false
                                },
                            contentPadding = PaddingValues(14.dp)
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
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(acc.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = acc.name,
                                            style = TextStyle(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "Account Balance: $currencySymbol${df.format(acc.balance)}",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.onBackground),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.background,
                                            modifier = Modifier.size(14.dp)
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

    // ─── Add New Account Dialog ──────────────────────────────────────
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = {
                Text(
                    text = "Add New Account",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newAccName,
                        onValueChange = { newAccName = it },
                        label = { Text("Account Name") },
                        placeholder = { Text("e.g., Paytm Wallet, Axis Bank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newAccBalance,
                        onValueChange = { newAccBalance = it },
                        label = { Text("Initial Balance ($currencySymbol)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Account Type",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("CASH", "BANK", "CREDIT_CARD").forEach { type ->
                            val isSel = newAccType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { newAccType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.replace("_", " "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bal = newAccBalance.toDoubleOrNull() ?: 0.0
                        if (newAccName.isNotBlank()) {
                            val icon = when (newAccType) {
                                "CASH" -> "payments"
                                "CREDIT_CARD" -> "credit_card"
                                else -> "account_balance"
                            }
                            onAddNewAccount(newAccName, newAccType, bal)
                            selectedAccount = AccountOption(name = newAccName, type = newAccType, balance = bal, iconName = icon)
                            showAddAccountDialog = false
                            showAccountBottomSheet = false
                            newAccName = ""
                            newAccBalance = ""
                        }
                    }
                ) {
                    Text("Save Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
