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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.vesper.ledger.ui.components.ChildHeader
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

private fun evaluateMathExpression(expr: String): Double? {
    try {
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        var total = 0.0
        val tokens = mutableListOf<String>()
        var currentToken = ""
        for (ch in sanitized) {
            if (ch in listOf('+', '-', '*', '/')) {
                if (currentToken.isNotBlank()) tokens.add(currentToken.trim())
                tokens.add(ch.toString())
                currentToken = ""
            } else {
                currentToken += ch
            }
        }
        if (currentToken.isNotBlank()) tokens.add(currentToken.trim())

        if (tokens.isEmpty()) return null

        val pass1 = mutableListOf<String>()
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx]
            if (token == "*" || token == "/") {
                val prevVal = pass1.removeAt(pass1.size - 1).toDoubleOrNull() ?: 0.0
                val nextVal = tokens.getOrNull(idx + 1)?.toDoubleOrNull() ?: 1.0
                val res = if (token == "*") prevVal * nextVal else if (nextVal != 0.0) prevVal / nextVal else 0.0
                pass1.add(res.toString())
                idx += 2
            } else {
                pass1.add(token)
                idx++
            }
        }

        var result = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
        idx = 1
        while (idx < pass1.size) {
            val op = pass1[idx]
            val nextVal = pass1.getOrNull(idx + 1)?.toDoubleOrNull() ?: 0.0
            if (op == "+") result += nextVal
            else if (op == "-") result -= nextVal
            idx += 2
        }
        return result
    } catch (e: Exception) {
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    currencySymbol: String = "₹",
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

    var selectedPaymentMethod by remember(paymentMethods) {
        mutableStateOf(paymentMethods.firstOrNull()?.name ?: "Cash")
    }

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var noteText by remember { mutableStateOf("") }

    // Sheet visibility states
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showCalculatorSheet by remember { mutableStateOf(false) }
    var showDatePickerSheet by remember { mutableStateOf(false) }
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val df = remember { DecimalFormat("#,##0.00") }

    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()
    val parsedAmount = amountText.replace(",", "").toDoubleOrNull() ?: 0.0

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF121215))
                    .navigationBarsPadding(),
                color = Color(0xFF121215),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            val acct = selectedAccount
                            if (parsedAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            } else if (acct == null) {
                                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveTransaction(
                                    titleText.ifBlank { if (type == TransactionType.INCOME) "Income" else "Expense" },
                                    parsedAmount,
                                    type,
                                    selectedCategoryId,
                                    acct.id,
                                    acct.name,
                                    selectedPaymentMethod,
                                    selectedCalendar.timeInMillis,
                                    noteText.trim()
                                )
                                showSuccessSheet = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Save Transaction",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SEGMENTED TAB SELECTOR (Income | Expense | Transfer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
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

                // 2. AMOUNT CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "AMOUNT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currencySymbol ",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                        color = if (amountText.isBlank()) Color.White.copy(alpha = 0.38f) else Color.White
                                    )
                                )
                                androidx.compose.foundation.text.BasicTextField(
                                    value = amountText,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() || it == '.' || it == ',' }) {
                                            amountText = newValue
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.headlineLarge.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                        color = Color.White
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (amountText.isBlank()) {
                                                Text(
                                                    text = "0",
                                                    style = MaterialTheme.typography.headlineLarge.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 32.sp,
                                                        color = Color.White.copy(alpha = 0.38f)
                                                    )
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                        }

                        // Calculator icon action button
                        IconButton(
                            onClick = { showCalculatorSheet = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF27272A))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Keypad Calculator",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 3. TRANSACTION TITLE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF27272A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TRANSACTION TITLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )
                            androidx.compose.foundation.text.BasicTextField(
                                value = titleText,
                                onValueChange = { titleText = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (titleText.isEmpty()) {
                                            Text("Enter title...", color = Color(0xFF71717A), fontSize = 16.sp, fontFamily = SpaceGroteskFamily)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }

                // 4. CATEGORY SELECTION CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { showCategorySheet = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val catColor = safeParseColor(selectedCategory?.colorHex ?: "#10B981")
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(selectedCategory?.iconName ?: "shopping_bag"),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "CATEGORY (${type.name})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 1.2.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                )
                                Text(
                                    text = selectedCategory?.name ?: "Select Category",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Select",
                            tint = Color(0xFFA1A1AA)
                        )
                    }
                }

                // 5. ACCOUNT & PAYMENT METHOD GRID (2 Columns)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Account
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showAccountSheet = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ACCOUNT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF27272A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(selectedAccount?.iconName ?: "wallet"),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = selectedAccount?.name ?: "Cash Wallet",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            ),
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFFA1A1AA),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Bal: $currencySymbol${df.format(selectedAccount?.initialBalance ?: 3420.0)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Payment Method
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { showPaymentSheet = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PAYMENT METHOD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF27272A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = selectedPaymentMethod,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            ),
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFFA1A1AA),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(
                                        text = "Auto-selected",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. DATE & TIME GRID (2 Columns)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DATE & TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.2.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Date selector
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF27272A))
                                    .clickable { showDatePickerSheet = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Date",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        )
                                        Text(
                                            text = dateFormatter.format(selectedCalendar.time),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // Time selector
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF27272A))
                                    .clickable { showTimePickerSheet = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Time",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        )
                                        Text(
                                            text = timeFormatter.format(selectedCalendar.time),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. ADD NOTE CARD (OPTIONAL)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF27272A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ADD NOTE (OPTIONAL)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )
                            androidx.compose.foundation.text.BasicTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 14.sp,
                                    color = Color.White
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (noteText.isEmpty()) {
                                            Text("Add a note...", color = Color(0xFF71717A), fontSize = 14.sp, fontFamily = SpaceGroteskFamily)
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 1: SELECT CATEGORY SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showCategorySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCategorySheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    containerColor = Color(0xFF18181B),
                    scrimColor = Color.Black.copy(alpha = 0.6f),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF52525B)) },
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    var searchCatQuery by remember { mutableStateOf("") }
                    val suggestedCats = filteredCategories.take(6)
                    val allCats = filteredCategories.filter { it.name.contains(searchCatQuery, ignoreCase = true) }

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
                            Text(
                                text = "Select Category",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            IconButton(onClick = { showCategorySheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Search Input
                        OutlinedTextField(
                            value = searchCatQuery,
                            onValueChange = { searchCatQuery = it },
                            placeholder = { Text("Search categories", color = Color(0xFF71717A)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFA1A1AA)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color(0xFF27272A),
                                focusedContainerColor = Color(0xFF09090B),
                                unfocusedContainerColor = Color(0xFF09090B)
                            )
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (searchCatQuery.isBlank()) {
                                item {
                                    Text(
                                        text = "SUGGESTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.2.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                                items(suggestedCats) { cat ->
                                    CategoryRowItem(
                                        category = cat,
                                        isSelected = cat.id == selectedCategoryId,
                                        onClick = {
                                            selectedCategoryId = cat.id
                                            showCategorySheet = false
                                        }
                                    )
                                }
                            }

                            item {
                                Text(
                                    text = "ALL CATEGORIES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                )
                            }

                            items(allCats) { cat ->
                                CategoryRowItem(
                                    category = cat,
                                    isSelected = cat.id == selectedCategoryId,
                                    onClick = {
                                        selectedCategoryId = cat.id
                                        showCategorySheet = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 2: REAL WORKING CALCULATOR SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showCalculatorSheet) {
                var calcExpression by remember { mutableStateOf(if (amountText.isBlank()) "0" else amountText) }

                val evaluatedVal = remember(calcExpression) {
                    evaluateMathExpression(calcExpression)
                }

                ModalBottomSheet(
                    onDismissRequest = { showCalculatorSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    containerColor = Color(0xFF18181B),
                    scrimColor = Color.Black.copy(alpha = 0.6f),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF52525B)) },
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATOR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            )
                            IconButton(onClick = { showCalculatorSheet = false }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // Calculator Formula & Result Display Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF09090B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = calcExpression,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFA1A1AA)
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (evaluatedVal != null) "= $currencySymbol${DecimalFormat("#,##0.##").format(evaluatedVal)}" else "= $currencySymbol 0",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF22C55E)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        // 4x4 Real Calculator Grid
                        val calcKeys = listOf(
                            "C", "÷", "×", "⌫",
                            "7", "8", "9", "-",
                            "4", "5", "6", "+",
                            "1", "2", "3", "=",
                            "00", "0", ".", "AC"
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(calcKeys) { key ->
                                val isOperator = key in listOf("+", "-", "×", "÷", "=")
                                val isAction = key in listOf("C", "AC", "⌫")
                                val btnBg = when {
                                    key == "=" -> Color(0xFF22C55E)
                                    isOperator -> Color(0xFF27272A)
                                    isAction -> Color(0xFF3F3F46)
                                    else -> Color(0xFF18181B)
                                }
                                val btnTextClr = when {
                                    key == "=" -> Color.Black
                                    isOperator -> Color(0xFF38BDF8)
                                    isAction -> Color(0xFFF43F5E)
                                    else -> Color.White
                                }

                                Surface(
                                    modifier = Modifier
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            when (key) {
                                                "C", "AC" -> calcExpression = "0"
                                                "⌫" -> {
                                                    calcExpression = if (calcExpression.length > 1) calcExpression.dropLast(1) else "0"
                                                }
                                                "=" -> {
                                                    if (evaluatedVal != null && evaluatedVal > 0.0) {
                                                        amountText = DecimalFormat("0.##").format(evaluatedVal)
                                                    }
                                                }
                                                else -> {
                                                    if (calcExpression == "0" && key !in listOf(".", "+", "-", "×", "÷")) {
                                                        calcExpression = key
                                                    } else {
                                                        calcExpression += key
                                                    }
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = btnBg,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = btnTextClr
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (evaluatedVal != null && evaluatedVal > 0.0) {
                                    amountText = DecimalFormat("0.##").format(evaluatedVal)
                                }
                                showCalculatorSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("Apply Result", fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily, fontSize = 16.sp)
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // DIALOG MODAL 3: SELECT DATE PICKER MODAL (FIXED HEIGHT & MANUAL ENTRY)
            // ────────────────────────────────────────────────────────────────────────
            if (showDatePickerSheet) {
                var isManualMode by remember { mutableStateOf(false) }
                var showYearSelector by remember { mutableStateOf(false) }
                var currentMonthCal by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedCalendar.timeInMillis }) }
                val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

                var manualDayText by remember { mutableStateOf(selectedCalendar.get(Calendar.DAY_OF_MONTH).toString()) }
                var manualMonthText by remember { mutableStateOf((selectedCalendar.get(Calendar.MONTH) + 1).toString()) }
                var manualYearText by remember { mutableStateOf(selectedCalendar.get(Calendar.YEAR).toString()) }

                AlertDialog(
                    onDismissRequest = { showDatePickerSheet = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (isManualMode) {
                                val d = manualDayText.toIntOrNull() ?: selectedCalendar.get(Calendar.DAY_OF_MONTH)
                                val m = (manualMonthText.toIntOrNull() ?: (selectedCalendar.get(Calendar.MONTH) + 1)) - 1
                                val y = manualYearText.toIntOrNull() ?: selectedCalendar.get(Calendar.YEAR)
                                val newCal = (selectedCalendar.clone() as Calendar).apply {
                                    set(Calendar.YEAR, y)
                                    set(Calendar.MONTH, m.coerceIn(0, 11))
                                    set(Calendar.DAY_OF_MONTH, d.coerceIn(1, 31))
                                }
                                selectedCalendar = newCal
                            }
                            showDatePickerSheet = false
                        }) {
                            Text("Done", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF18181B),
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Date", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                            
                            // Mode Switcher Pill (Calendar | Type Date)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF27272A))
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "Calendar",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isManualMode) Color.White else Color.Transparent)
                                        .clickable { isManualMode = false }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isManualMode) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                                Text(
                                    text = "Type Date",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isManualMode) Color.White else Color.Transparent)
                                        .clickable { isManualMode = true }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isManualMode) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isManualMode) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("MANUAL DATE ENTRY", color = Color(0xFFA1A1AA), fontSize = 11.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = manualDayText,
                                            onValueChange = { if (it.length <= 2) manualDayText = it },
                                            label = { Text("Day", color = Color(0xFFA1A1AA), fontSize = 10.sp, fontFamily = SpaceGroteskFamily) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color(0xFF3F3F46))
                                        )
                                        OutlinedTextField(
                                            value = manualMonthText,
                                            onValueChange = { if (it.length <= 2) manualMonthText = it },
                                            label = { Text("Month", color = Color(0xFFA1A1AA), fontSize = 10.sp, fontFamily = SpaceGroteskFamily) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color(0xFF3F3F46))
                                        )
                                        OutlinedTextField(
                                            value = manualYearText,
                                            onValueChange = { if (it.length <= 4) manualYearText = it },
                                            label = { Text("Year", color = Color(0xFFA1A1AA), fontSize = 10.sp, fontFamily = SpaceGroteskFamily) },
                                            modifier = Modifier.weight(1.3f),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.White, unfocusedBorderColor = Color(0xFF3F3F46))
                                        )
                                    }
                                }
                            } else if (showYearSelector) {
                                // YEAR SELECTOR GRID (2020..2035)
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Select Year", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                        TextButton(onClick = { showYearSelector = false }) {
                                            Text("Back to Calendar", fontFamily = SpaceGroteskFamily, color = Color(0xFF38BDF8), fontSize = 12.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val yearsList = (2020..2035).toList()
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(yearsList) { yr ->
                                            val isSelectedYr = currentMonthCal.get(Calendar.YEAR) == yr
                                            Box(
                                                modifier = Modifier
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelectedYr) Color.White else Color(0xFF27272A))
                                                    .clickable {
                                                        currentMonthCal = (currentMonthCal.clone() as Calendar).apply { set(Calendar.YEAR, yr) }
                                                        showYearSelector = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$yr",
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelectedYr) Color.Black else Color.White,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Clickable Month & Year Header to open Year Selector
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showYearSelector = true }
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = monthFormat.format(currentMonthCal.time) + " ▾",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = {
                                            currentMonthCal = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                                        }) {
                                            Icon(Icons.Default.ChevronLeft, null, tint = Color.White)
                                        }
                                        IconButton(onClick = {
                                            currentMonthCal = (currentMonthCal.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                                        }) {
                                            Icon(Icons.Default.ChevronRight, null, tint = Color.White)
                                        }
                                    }
                                }

                                // Days Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        )
                                    }
                                }

                                // Calendar Grid Days (Fixed 42-cell layout for constant card height)
                                val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                val tempCal = (currentMonthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                                val firstDayOfWeekIndex = tempCal.get(Calendar.DAY_OF_WEEK) - 1
                                val totalSlots = 42 // 6 rows * 7 columns

                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(7),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                ) {
                                    items(totalSlots) { slotIndex ->
                                        val dayNumber = slotIndex - firstDayOfWeekIndex + 1
                                        if (dayNumber in 1..daysInMonth) {
                                            val isSelected = selectedCalendar.get(Calendar.YEAR) == currentMonthCal.get(Calendar.YEAR) &&
                                                    selectedCalendar.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH) &&
                                                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == dayNumber

                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else Color.Transparent)
                                                    .clickable {
                                                        val newCal = (selectedCalendar.clone() as Calendar).apply {
                                                            set(Calendar.YEAR, currentMonthCal.get(Calendar.YEAR))
                                                            set(Calendar.MONTH, currentMonthCal.get(Calendar.MONTH))
                                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                                        }
                                                        selectedCalendar = newCal
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.Black else Color.White
                                                    )
                                                )
                                            }
                                        } else {
                                            Box(modifier = Modifier.size(30.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // ────────────────────────────────────────────────────────────────────────
            // DIALOG MODAL 4: ADVANCED TIME PICKER CLOCK MODAL
            // ────────────────────────────────────────────────────────────────────────
            if (showTimePickerSheet) {
                var selectedHour by remember { mutableStateOf(selectedCalendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }) }
                var selectedMinute by remember { mutableStateOf(selectedCalendar.get(Calendar.MINUTE)) }
                var isAm by remember { mutableStateOf(selectedCalendar.get(Calendar.AM_PM) == Calendar.AM) }
                var isPickMinutes by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showTimePickerSheet = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val newCal = (selectedCalendar.clone() as Calendar).apply {
                                var h = selectedHour % 12
                                if (!isAm) h += 12
                                set(Calendar.HOUR_OF_DAY, h)
                                set(Calendar.MINUTE, selectedMinute)
                            }
                            selectedCalendar = newCal
                            showTimePickerSheet = false
                        }) {
                            Text("Set Time", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    },
                    containerColor = Color(0xFF18181B),
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Select Time", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                            
                            // Mode Switcher Pill (Hours | Minutes)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF27272A))
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "Hours",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isPickMinutes) Color.White else Color.Transparent)
                                        .clickable { isPickMinutes = false }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isPickMinutes) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                                Text(
                                    text = "Minutes",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isPickMinutes) Color.White else Color.Transparent)
                                        .clickable { isPickMinutes = true }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPickMinutes) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                            }
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // AM / PM Toggle Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF27272A))
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "AM",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAm) Color.White else Color.Transparent)
                                        .clickable { isAm = true }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAm) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                                Text(
                                    text = "PM",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isAm) Color.White else Color.Transparent)
                                        .clickable { isAm = false }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAm) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                            }

                            // Digital Time Display Box
                            Text(
                                text = String.format("%02d : %02d %s", selectedHour, selectedMinute, if (isAm) "AM" else "PM"),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    color = Color.White
                                )
                            )

                            if (!isPickMinutes) {
                                // 3x4 GRID HOUR SELECTOR (1..12)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("SELECT HOUR", fontSize = 11.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA), letterSpacing = 1.2.sp)
                                    val hoursList = (1..12).toList()
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        items(hoursList) { hr ->
                                            val isSelectedHr = hr == selectedHour
                                            Box(
                                                modifier = Modifier
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelectedHr) Color.White else Color(0xFF27272A))
                                                    .clickable { selectedHour = hr },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = String.format("%02d", hr),
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelectedHr) Color.Black else Color.White,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // 3x4 GRID MINUTE SELECTOR (00, 05, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("SELECT MINUTE", fontSize = 11.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA), letterSpacing = 1.2.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = { selectedMinute = (selectedMinute - 1 + 60) % 60 },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("−1m", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = SpaceGroteskFamily)
                                            }
                                            IconButton(
                                                onClick = { selectedMinute = (selectedMinute + 1) % 60 },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Text("+1m", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = SpaceGroteskFamily)
                                            }
                                        }
                                    }
                                    val minutesList = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        items(minutesList) { mn ->
                                            val isSelectedMn = mn == selectedMinute
                                            Box(
                                                modifier = Modifier
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelectedMn) Color.White else Color(0xFF27272A))
                                                    .clickable { selectedMinute = mn },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = String.format("%02d", mn),
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelectedMn) Color.Black else Color.White,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 5: TRANSACTION SAVED SUCCESS SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showSuccessSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSuccessSheet = false
                        onBackClick()
                    },
                    containerColor = Color(0xFF09090B),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Success", tint = Color.Black, modifier = Modifier.size(36.dp))
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Transaction Saved",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Your expense has been saved successfully.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 13.sp
                                )
                            )
                        }

                        // Details Card Summary
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(selectedCategory?.name ?: "Groceries", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("$currencySymbol$amountText", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                }
                                Text("📅 ${dateFormatter.format(selectedCalendar.time)}, ${timeFormatter.format(selectedCalendar.time)}", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                                Text("👛 ${selectedAccount?.name ?: "Cash Wallet"} • $selectedPaymentMethod", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                showSuccessSheet = false
                                onBackClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("View Transaction", fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRowItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val catColor = safeParseColor(category.colorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(catColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconByName(category.iconName),
                    contentDescription = null,
                    tint = catColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color.White,
                unselectedColor = Color(0xFF71717A)
            )
        )
    }
}
