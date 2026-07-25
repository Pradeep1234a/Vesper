package com.vesper.ledger.ui.transactions

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.PaymentMethod
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

private fun evalMath(expr: String): Double? {
    try {
        val clean = expr.replace("×", "*").replace("÷", "/")
        val tokens = mutableListOf<String>()
        var current = ""
        for (ch in clean) {
            if (ch in listOf('+', '-', '*', '/')) {
                if (current.isNotBlank()) tokens.add(current.trim())
                tokens.add(ch.toString())
                current = ""
            } else {
                current += ch
            }
        }
        if (current.isNotBlank()) tokens.add(current.trim())
        if (tokens.isEmpty()) return null

        var total = tokens[0].toDoubleOrNull() ?: return null
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val nextVal = tokens[i + 1].toDoubleOrNull() ?: break
            when (op) {
                "+" -> total += nextVal
                "-" -> total -= nextVal
                "*" -> total *= nextVal
                "/" -> if (nextVal != 0.0) total /= nextVal
            }
            i += 2
        }
        return total
    } catch (e: Exception) {
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AddTransactionScreen(
    currencySymbol: String = "₹",
    categories: List<Category>,
    accounts: List<Account>,
    paymentMethods: List<PaymentMethod> = emptyList(),
    onBackClick: () -> Unit = {},
    onAddCategoryClick: () -> Unit = {},
    onAddAccountClick: () -> Unit = {},
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
    val keyboardController = LocalSoftwareKeyboardController.current

    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }

    // Filter categories by selected transaction type
    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }.ifEmpty { categories }
    }
    var selectedCategoryId by remember(filteredCategories) {
        mutableStateOf(filteredCategories.firstOrNull()?.id ?: 1L)
    }

    val activeAccounts = remember(accounts) { accounts.filter { !it.isHidden } }
    var selectedAccount by remember(activeAccounts) {
        mutableStateOf(activeAccounts.firstOrNull())
    }

    // Dynamic payment methods based on selected account type
    val dynamicPaymentMethods = remember(selectedAccount) {
        when (selectedAccount?.type) {
            "CASH" -> listOf("Cash")
            "CREDIT_CARD" -> listOf("Credit Card", "EMI", "Card Swipe")
            "WALLET" -> listOf("E-Wallet", "UPI", "App Balance")
            else -> listOf("UPI", "Debit Card", "Net Banking", "Bank Transfer", "Cash")
        }
    }

    var selectedPaymentMethod by remember(dynamicPaymentMethods) {
        mutableStateOf(dynamicPaymentMethods.firstOrNull() ?: "Cash")
    }

    // Default Date and Time = Current Date and Time
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var noteText by remember { mutableStateOf("") }

    // Calculator overlay states
    var isCalculatorActive by remember { mutableStateOf(false) }
    var calcExpression by remember { mutableStateOf("") }
    val calcPreview = remember(calcExpression) { evalMath(calcExpression) }

    // Modal Visibility States
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showDatePickerModal by remember { mutableStateOf(false) }
    var showTimePickerModal by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val df = remember { DecimalFormat("#,##0.00") }

    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()
    val parsedAmount = amountText.replace(",", "").toDoubleOrNull() ?: 0.0

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isCalculatorActive) {
                // INLINE CALCULATOR OVERLAY PAD (ALWAYS VISIBLE, ATTACHED TO BOTTOM)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Calculator Preview Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = calcExpression.ifBlank { "0" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                if (calcPreview != null) {
                                    Text(
                                        text = "= $currencySymbol ${df.format(calcPreview)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }

                            TextButton(onClick = {
                                isCalculatorActive = false
                                calcExpression = ""
                            }) {
                                Text("Close", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Calculator Keys Grid
                        val keys = listOf(
                            listOf("C", "÷", "×", "⌫"),
                            listOf("7", "8", "9", "-"),
                            listOf("4", "5", "6", "+"),
                            listOf("1", "2", "3", "="),
                            listOf("0", ".", "DONE", "=")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                listOf("C", "÷", "×", "⌫"),
                                listOf("7", "8", "9", "-"),
                                listOf("4", "5", "6", "+"),
                                listOf("1", "2", "3", "0"),
                                listOf(".", "=", "DONE", "")
                            ).forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { k ->
                                        if (k.isNotEmpty()) {
                                            val isOp = k in listOf("÷", "×", "-", "+", "=")
                                            val isDone = k == "DONE"
                                            Button(
                                                onClick = {
                                                    when (k) {
                                                        "C" -> calcExpression = ""
                                                        "⌫" -> if (calcExpression.isNotEmpty()) calcExpression = calcExpression.dropLast(1)
                                                        "DONE" -> {
                                                            val res = calcPreview ?: calcExpression.toDoubleOrNull()
                                                            if (res != null) {
                                                                amountText = df.format(res)
                                                            }
                                                            isCalculatorActive = false
                                                        }
                                                        "=" -> {
                                                            val res = calcPreview
                                                            if (res != null) {
                                                                amountText = df.format(res)
                                                                calcExpression = df.format(res)
                                                            }
                                                        }
                                                        else -> calcExpression += k
                                                    }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = when {
                                                        isDone -> MaterialTheme.colorScheme.onBackground
                                                        isOp -> MaterialTheme.colorScheme.primaryContainer
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    },
                                                    contentColor = when {
                                                        isDone -> MaterialTheme.colorScheme.background
                                                        isOp -> MaterialTheme.colorScheme.onPrimaryContainer
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            ) {
                                                Text(
                                                    text = k,
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // FIXED CTA BUTTON AT BOTTOM (OUTSIDE SCROLL, ALWAYS VISIBLE)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            if (parsedAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val finalTitle = titleText.ifBlank {
                                selectedCategory?.name ?: if (type == TransactionType.INCOME) "Income" else "Expense"
                            }

                            onSaveTransaction(
                                finalTitle,
                                parsedAmount,
                                type,
                                selectedCategoryId,
                                selectedAccount?.id ?: 1L,
                                selectedAccount?.name ?: "Cash",
                                selectedPaymentMethod,
                                selectedCalendar.timeInMillis,
                                noteText
                            )

                            Toast.makeText(context, "Transaction saved!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Transaction",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. SEGMENTED TAB SELECTOR (Income | Expense | Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    TransactionType.INCOME to "Income",
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.TRANSFER to "Transfer"
                ).forEach { (tabType, label) ->
                    val selected = type == tabType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent)
                            .clickable { type = tabType },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // 2. AMOUNT INPUT CARD WITH CALCULATOR TOGGLE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        // Toggle Calculator Icon
                        IconButton(
                            onClick = {
                                isCalculatorActive = !isCalculatorActive
                                if (isCalculatorActive) {
                                    keyboardController?.hide()
                                    calcExpression = amountText.replace(",", "")
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCalculatorActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Toggle Calculator",
                                tint = if (isCalculatorActive) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currencySymbol,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (!isCalculatorActive) {
                                    amountText = input.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' }
                                }
                            },
                            placeholder = { Text("0.00", fontFamily = SpaceGroteskFamily, fontSize = 28.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // 3. TRANSACTION TITLE INPUT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("Transaction Title (e.g. Dinner, Rent)", fontFamily = PlusJakartaSansFamily) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // 4. CATEGORY SELECTOR CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategorySheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(safeParseColor(selectedCategory?.colorHex ?: "#71717A").copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(selectedCategory?.iconName ?: "more_horiz"),
                                contentDescription = null,
                                tint = safeParseColor(selectedCategory?.colorHex ?: "#71717A"),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text("CATEGORY", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(selectedCategory?.name ?: "Select Category", style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Select", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 5. ACCOUNT SELECTOR CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAccountSheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(selectedAccount?.iconName ?: "account_balance_wallet"),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text("ACCOUNT", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(selectedAccount?.name ?: "Select Account", style = MaterialTheme.typography.titleMedium.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Select", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 6. DYNAMIC PAYMENT METHOD SELECTOR
            Text(
                text = "PAYMENT METHOD",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dynamicPaymentMethods.forEach { method ->
                    val selected = selectedPaymentMethod == method
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .clickable { selectedPaymentMethod = method },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            fontSize = 12.sp,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 7. DATE & TIME SELECTOR CARDS (TRIGGER MODERN DIALOG MODALS)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Selector
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePickerModal = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = "Date", tint = MaterialTheme.colorScheme.onSurface)
                        Column {
                            Text("DATE", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(dateFormatter.format(selectedCalendar.time), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }

                // Time Selector
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTimePickerModal = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Schedule, contentDescription = "Time", tint = MaterialTheme.colorScheme.onSurface)
                        Column {
                            Text("TIME", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceGroteskFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            Text(timeFormatter.format(selectedCalendar.time), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }
            }

            // 8. NOTE/MEMO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Add optional note or tag...", fontFamily = PlusJakartaSansFamily) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // CATEGORY SELECTION BOTTOM SHEET (ANCHORED FIRMLY TO BOTTOM)
    // ────────────────────────────────────────────────────────────────────────
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCategories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryId = cat.id
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(safeParseColor(cat.colorHex).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = safeParseColor(cat.colorHex)
                                )
                            }
                            Text(
                                text = cat.name,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // ACCOUNT SELECTION BOTTOM SHEET (ANCHORED FIRMLY TO BOTTOM)
    // ────────────────────────────────────────────────────────────────────────
    if (showAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Account",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeAccounts) { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedAccount = acc
                                    showAccountSheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(acc.iconName ?: "account_balance_wallet"),
                                    contentDescription = acc.name,
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = acc.name,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // MODERN CENTERED DATE PICKER DIALOG MODAL
    // ────────────────────────────────────────────────────────────────────────
    if (showDatePickerModal) {
        var currentCal by remember { mutableStateOf(selectedCalendar.clone() as Calendar) }
        AlertDialog(
            onDismissRequest = { showDatePickerModal = false },
            title = {
                Text("Select Date", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(dateFormatter.format(currentCal.time), fontFamily = SpaceGroteskFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentCal = (currentCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -1) }
                        }) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Day")
                        }
                        Text("Adjust Day", fontFamily = SpaceGroteskFamily)
                        IconButton(onClick = {
                            currentCal = (currentCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
                        }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedCalendar = currentCal
                    showDatePickerModal = false
                }) {
                    Text("Save Date", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerModal = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // ────────────────────────────────────────────────────────────────────────
    // MODERN CENTERED TIME PICKER DIALOG MODAL
    // ────────────────────────────────────────────────────────────────────────
    if (showTimePickerModal) {
        var currentCal by remember { mutableStateOf(selectedCalendar.clone() as Calendar) }
        AlertDialog(
            onDismissRequest = { showTimePickerModal = false },
            title = {
                Text("Select Time", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(timeFormatter.format(currentCal.time), fontFamily = SpaceGroteskFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentCal = (currentCal.clone() as Calendar).apply { add(Calendar.MINUTE, -15) }
                        }) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "-15 mins")
                        }
                        Text("15 Min Steps", fontFamily = SpaceGroteskFamily)
                        IconButton(onClick = {
                            currentCal = (currentCal.clone() as Calendar).apply { add(Calendar.MINUTE, 15) }
                        }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "+15 mins")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedCalendar = currentCal
                    showTimePickerModal = false
                }) {
                    Text("Save Time", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerModal = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
