package com.vesper.ledger.ui.transactions

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.window.Dialog
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
    transactionToEdit: com.vesper.ledger.data.model.Transaction? = null,
    currencySymbol: String = "₹",
    categories: List<Category>,
    accounts: List<Account>,
    paymentMethods: List<PaymentMethod> = emptyList(),
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit = {},
    onAddAccountClick: () -> Unit = {},
    onDeleteTransaction: ((com.vesper.ledger.data.model.Transaction) -> Unit)? = null,
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
    val isEditMode = transactionToEdit != null

    var type by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE) }
    var amountText by remember(transactionToEdit) {
        mutableStateOf(transactionToEdit?.amount?.let { if (it > 0) DecimalFormat("0.##").format(it) else "" } ?: "")
    }
    var titleText by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.title ?: "") }

    // Filter categories by selected transaction type
    val filteredCategories = remember(categories, type) {
        categories.filter { it.type == type }.ifEmpty { categories }
    }
    var selectedCategoryId by remember(filteredCategories, transactionToEdit) {
        if (transactionToEdit != null) {
            mutableStateOf<Long?>(transactionToEdit.categoryId)
        } else {
            mutableStateOf<Long?>(null)
        }
    }

    val activeAccounts = remember(accounts) { accounts.filter { !it.isHidden } }
    var selectedAccount by remember(activeAccounts, transactionToEdit) {
        if (transactionToEdit != null) {
            mutableStateOf(accounts.find { it.id == transactionToEdit.accountId } ?: activeAccounts.firstOrNull())
        } else {
            mutableStateOf(activeAccounts.firstOrNull())
        }
    }

    var selectedPaymentMethod by remember(paymentMethods, transactionToEdit) {
        if (transactionToEdit != null) {
            mutableStateOf(transactionToEdit.paymentMethod)
        } else {
            mutableStateOf(paymentMethods.firstOrNull()?.name ?: "Cash")
        }
    }

    var selectedCalendar by remember(transactionToEdit) {
        val cal = Calendar.getInstance()
        if (transactionToEdit != null) {
            cal.timeInMillis = transactionToEdit.dateEpochMillis
        }
        mutableStateOf(cal)
    }
    var isTimeManuallySet by remember(transactionToEdit) { mutableStateOf(transactionToEdit != null) }
    var noteText by remember(transactionToEdit) { mutableStateOf(transactionToEdit?.note ?: "") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // REAL-TIME CLOCK TICKER IF NO MANUAL TIME IS SELECTED
    LaunchedEffect(isTimeManuallySet) {
        if (!isTimeManuallySet) {
            while (true) {
                val now = Calendar.getInstance()
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = selectedCalendar.timeInMillis
                    set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                    set(Calendar.SECOND, now.get(Calendar.SECOND))
                }
                selectedCalendar = newCal
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    // Menu & Sheet visibility states
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    var showPaymentMenu by remember { mutableStateOf(false) }
    var showDatePickerSheet by remember { mutableStateOf(false) }
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val df = remember { DecimalFormat("#,##0.00") }

    val selectedCategory = categories.find { it.id == selectedCategoryId }
    val parsedAmount = amountText.replace(",", "").toDoubleOrNull() ?: 0.0

    // Delete Confirmation Dialog for Edit Mode
    if (showDeleteConfirmDialog && transactionToEdit != null && onDeleteTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Transaction",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${transactionToEdit.title.ifBlank { "this transaction" }}'? This action cannot be undone.",
                    fontFamily = SpaceGroteskFamily,
                    color = Color(0xFFA1A1AA)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTransaction(transactionToEdit)
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Delete", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily, color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF18181B),
            shape = RoundedCornerShape(6.dp)
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            val acct = selectedAccount
                            val cat = selectedCategory
                            if (parsedAmount <= 0.0) {
                                Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            } else if (cat == null) {
                                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                            } else if (acct == null) {
                                Toast.makeText(context, "Please select an account", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveTransaction(
                                    titleText.ifBlank { if (type == TransactionType.INCOME) "Income" else "Expense" },
                                    parsedAmount,
                                    type,
                                    cat.id,
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
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = if (isEditMode) "Save Changes" else "Save Transaction",
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
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SEGMENTED TAB SELECTOR (Income | Expense | Transfer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
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
                                .clip(RoundedCornerShape(6.dp))
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
                    shape = RoundedCornerShape(6.dp),
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
                    }
                }

                // 3. TRANSACTION TITLE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
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
                                .clip(RoundedCornerShape(6.dp))
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

                // 4. CATEGORY SELECTION CARD & MODAL DIALOG
                Box(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showCategorySheet = true },
                        shape = RoundedCornerShape(6.dp),
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
                                val catColor = if (selectedCategory != null) safeParseColor(selectedCategory.colorHex) else Color(0xFFA1A1AA)
                                val catBg = if (selectedCategory != null) catColor.copy(alpha = 0.15f) else Color(0xFF27272A)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(catBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedCategory != null) getIconByName(selectedCategory.iconName) else Icons.Outlined.Category,
                                        contentDescription = null,
                                        tint = if (selectedCategory != null) catColor else Color(0xFFA1A1AA),
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
                                            color = if (selectedCategory != null) Color.White else Color(0xFFA1A1AA)
                                        )
                                    )
                                }
                            }

                            Text("▾", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFA1A1AA))
                        }
                    }

                    if (showCategorySheet) {
                        AlertDialog(
                            onDismissRequest = { showCategorySheet = false },
                            title = {
                                Text(
                                    text = "Select Category",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    filteredCategories.forEach { cat ->
                                        val isSelected = cat.id == selectedCategoryId
                                        val catColor = safeParseColor(cat.colorHex)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(0xFF27272A) else Color(0xFF09090B))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable {
                                                    selectedCategoryId = cat.id
                                                    showCategorySheet = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(catColor.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getIconByName(cat.iconName),
                                                        contentDescription = null,
                                                        tint = catColor,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Text(
                                                    text = cat.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF38BDF8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showCategorySheet = false }) {
                                    Text("Close", color = Color(0xFFA1A1AA), fontFamily = SpaceGroteskFamily)
                                }
                            },
                            containerColor = Color(0xFF18181B),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                // 5. ACCOUNT & PAYMENT METHOD GRID WITH MODAL DIALOGS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Account Modal Dialog Card
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showAccountMenu = true },
                            shape = RoundedCornerShape(6.dp),
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
                                            .clip(RoundedCornerShape(6.dp))
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
                                                text = selectedAccount?.name ?: activeAccounts.firstOrNull()?.name ?: "Cash",
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
                                            text = "Bal: $currencySymbol${df.format(selectedAccount?.initialBalance ?: 0.0)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (showAccountMenu) {
                            AlertDialog(
                                onDismissRequest = { showAccountMenu = false },
                                title = {
                                    Text(
                                        text = "Select Account",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                },
                                text = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        activeAccounts.forEach { account ->
                                            val isSelected = account.id == selectedAccount?.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) Color(0xFF27272A) else Color(0xFF09090B))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        selectedAccount = account
                                                        showAccountMenu = false
                                                    }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF27272A)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = getIconByName(account.iconName),
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = account.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                                fontFamily = SpaceGroteskFamily,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White
                                                            )
                                                        )
                                                        Text(
                                                            text = "Balance: $currencySymbol${df.format(account.initialBalance)}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Color(0xFFA1A1AA)
                                                            )
                                                        )
                                                    }
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showAccountMenu = false }) {
                                        Text("Close", color = Color(0xFFA1A1AA), fontFamily = SpaceGroteskFamily)
                                    }
                                },
                                containerColor = Color(0xFF18181B),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    // Right Column: Payment Method Modal Dialog Card
                    Box(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showPaymentMenu = true },
                            shape = RoundedCornerShape(6.dp),
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
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF27272A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getPaymentMethodIcon(selectedPaymentMethod),
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
                                            text = "Select method",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (showPaymentMenu) {
                            AlertDialog(
                                onDismissRequest = { showPaymentMenu = false },
                                title = {
                                    Text(
                                        text = "Select Payment Method",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                },
                                text = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        paymentMethods.forEach { method ->
                                            val isSelected = method.name == selectedPaymentMethod
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSelected) Color(0xFF27272A) else Color(0xFF09090B))
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A),
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        selectedPaymentMethod = method.name
                                                        showPaymentMenu = false
                                                    }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFF27272A)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = getPaymentMethodIcon(method.name),
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = method.name,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontFamily = SpaceGroteskFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showPaymentMenu = false }) {
                                        Text("Close", color = Color(0xFFA1A1AA), fontFamily = SpaceGroteskFamily)
                                    }
                                },
                                containerColor = Color(0xFF18181B),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }

                // 6. DATE & TIME GRID (2 Columns)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
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
                                    .clip(RoundedCornerShape(6.dp))
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
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF27272A))
                                    .clickable {
                                        isTimeManuallySet = true
                                        showTimePickerSheet = true
                                    }
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

                // 7. ADD NOTE CARD (MULTILINE AUTO-EXPANDING WITH 100% VISIBILITY)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
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
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.foundation.text.BasicTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                singleLine = false,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
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

                // 8. DESTRUCTIVE CTA BUTTON (EDIT MODE ONLY)
                if (isEditMode && transactionToEdit != null && onDeleteTransaction != null) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF270F11),
                            contentColor = Color(0xFFEF4444)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Transaction",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Transaction",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // DIALOG 3: OFFICIAL MATERIAL 3 SPEC DATE PICKER DIALOG
            // ────────────────────────────────────────────────────────────────────────
            if (showDatePickerSheet) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedCalendar.timeInMillis
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePickerSheet = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val cal = Calendar.getInstance().apply { timeInMillis = millis }
                                    selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                        set(Calendar.YEAR, cal.get(Calendar.YEAR))
                                        set(Calendar.MONTH, cal.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
                                    }
                                }
                                showDatePickerSheet = false
                            }
                        ) {
                            Text("OK", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerSheet = false }) {
                            Text("Cancel", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA))
                        }
                    },
                    colors = DatePickerDefaults.colors(containerColor = Color(0xFF18181B))
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF18181B),
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = Color(0xFFA1A1AA),
                            subheadContentColor = Color.White,
                            yearContentColor = Color.White,
                            currentYearContentColor = Color(0xFF38BDF8),
                            selectedYearContentColor = Color.Black,
                            selectedYearContainerColor = Color.White,
                            dayContentColor = Color.White,
                            disabledDayContentColor = Color(0xFF52525B),
                            selectedDayContentColor = Color.Black,
                            selectedDayContainerColor = Color.White,
                            todayContentColor = Color(0xFF38BDF8),
                            todayDateBorderColor = Color(0xFF38BDF8)
                        )
                    )
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // DIALOG 4: OFFICIAL MATERIAL 3 TIME PICKER DIALOG (EXACT SPEC MATCH)
            // ────────────────────────────────────────────────────────────────────────
            if (showTimePickerSheet) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY),
                    initialMinute = selectedCalendar.get(Calendar.MINUTE),
                    is24Hour = false
                )
                var showKeyboardInputMode by remember { mutableStateOf(false) }

                Dialog(onDismissRequest = { showTimePickerSheet = false }) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color(0xFF18181B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A)),
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (showKeyboardInputMode) "Enter time" else "Select time",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (showKeyboardInputMode) {
                                    TimeInput(
                                        state = timePickerState,
                                        colors = TimePickerDefaults.colors(
                                            clockDialColor = Color(0xFF09090B),
                                            clockDialSelectedContentColor = Color.Black,
                                            clockDialUnselectedContentColor = Color.White,
                                            selectorColor = Color.White,
                                            containerColor = Color(0xFF18181B),
                                            periodSelectorBorderColor = Color(0xFF27272A),
                                            periodSelectorSelectedContainerColor = Color.White,
                                            periodSelectorUnselectedContainerColor = Color(0xFF09090B),
                                            periodSelectorSelectedContentColor = Color.Black,
                                            periodSelectorUnselectedContentColor = Color.White,
                                            timeSelectorSelectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                            timeSelectorUnselectedContainerColor = Color(0xFF09090B),
                                            timeSelectorSelectedContentColor = Color.White,
                                            timeSelectorUnselectedContentColor = Color.White
                                        )
                                    )
                                } else {
                                    TimePicker(
                                        state = timePickerState,
                                        colors = TimePickerDefaults.colors(
                                            clockDialColor = Color(0xFF09090B),
                                            clockDialSelectedContentColor = Color.Black,
                                            clockDialUnselectedContentColor = Color.White,
                                            selectorColor = Color.White,
                                            containerColor = Color(0xFF18181B),
                                            periodSelectorBorderColor = Color(0xFF27272A),
                                            periodSelectorSelectedContainerColor = Color.White,
                                            periodSelectorUnselectedContainerColor = Color(0xFF09090B),
                                            periodSelectorSelectedContentColor = Color.Black,
                                            periodSelectorUnselectedContentColor = Color.White,
                                            timeSelectorSelectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.25f),
                                            timeSelectorUnselectedContainerColor = Color(0xFF09090B),
                                            timeSelectorSelectedContentColor = Color.White,
                                            timeSelectorUnselectedContentColor = Color.White
                                        )
                                    )
                                }
                            }

                            // Bottom row: Mode toggle icon on left, Cancel / OK buttons on right
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { showKeyboardInputMode = !showKeyboardInputMode }) {
                                    Icon(
                                        imageVector = if (showKeyboardInputMode) Icons.Outlined.Schedule else Icons.Outlined.Keyboard,
                                        contentDescription = if (showKeyboardInputMode) "Switch to clock dial" else "Switch to keyboard input",
                                        tint = Color.White
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showTimePickerSheet = false }) {
                                        Text("Cancel", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA))
                                    }
                                    TextButton(
                                        onClick = {
                                            selectedCalendar = (selectedCalendar.clone() as Calendar).apply {
                                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                                set(Calendar.MINUTE, timePickerState.minute)
                                            }
                                            showTimePickerSheet = false
                                        }
                                    ) {
                                        Text("OK", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }
                }
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
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    containerColor = Color(0xFF18181B),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF27272A)) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
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

fun getPaymentMethodIcon(methodName: String): androidx.compose.ui.graphics.vector.ImageVector {
    val name = methodName.lowercase()
    return when {
        name.contains("cash") -> Icons.Outlined.Payments
        name.contains("upi") || name.contains("gpay") || name.contains("phonepe") || name.contains("paytm") || name.contains("qr") -> Icons.Outlined.QrCodeScanner
        name.contains("credit") -> Icons.Outlined.CreditCard
        name.contains("debit") -> Icons.Outlined.Payment
        name.contains("bank") || name.contains("transfer") || name.contains("netbanking") -> Icons.Outlined.AccountBalance
        name.contains("wallet") -> Icons.Outlined.AccountBalanceWallet
        else -> Icons.Outlined.CreditCard
    }
}

