package com.vesper.ledger.ui.addtransaction

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.*
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit
) {
    val amount by viewModel.amount.collectAsState()
    val type by viewModel.type.collectAsState()
    val categoryId by viewModel.categoryId.collectAsState()
    val dateEpochMillis by viewModel.dateEpochMillis.collectAsState()
    val note by viewModel.note.collectAsState()
    val accountName by viewModel.accountName.collectAsState()
    val paymentMethod by viewModel.paymentMethod.collectAsState()
    val recurringPattern by viewModel.recurringPattern.collectAsState()
    val location by viewModel.location.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val filteredCategories by viewModel.filteredCategories.collectAsState()
    val selectedCategory = categories.find { it.id == categoryId }
    val catColor = com.vesper.ledger.ui.components.safeParseColor(selectedCategory?.colorHex, MaterialTheme.colorScheme.primary)

    val sdfDate = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val dateText = remember(dateEpochMillis) {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
        val dateStr = when {
            today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Today"
            
            today.apply { add(Calendar.DAY_OF_YEAR, -1) }.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
            
            else -> sdfDate.format(Date(dateEpochMillis))
        }
        "$dateStr, ${sdfTime.format(Date(dateEpochMillis))}"
    }

    // Modal Sheet Visibility States
    var showCalculator by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showPaymentPicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showAdditionalDetails by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            ChildHeader(
                title = if (viewModel.isEditMode) "Edit Transaction" else "Add Transaction",
                onBackClick = onBackClick
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                // ── Transaction Type Segmented Control ──
                ShadcnSegmentedControl(
                    items = listOf("Expense", "Income", "Transfer"),
                    selectedIndex = when (type) {
                        TransactionType.EXPENSE -> 0
                        TransactionType.INCOME -> 1
                        TransactionType.TRANSFER -> 2
                    },
                    onItemSelected = { index ->
                        viewModel.type.value = when (index) {
                            0 -> TransactionType.EXPENSE
                            1 -> TransactionType.INCOME
                            else -> TransactionType.TRANSFER
                        }
                    }
                )

                // ── Amount Hero Card ──
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Amount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currencySymbol,
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                BasicTextField(
                                    value = amount,
                                    onValueChange = {
                                        if (it.length <= 15) {
                                            viewModel.amount.value = it
                                        }
                                    },
                                    textStyle = LocalTextStyle.current.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (amount.isEmpty()) {
                                            Text(
                                                text = "0.00",
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            IconButton(
                                onClick = { showCalculator = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Calculate,
                                    contentDescription = "Calculator",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ── Category Selector Field (Directly below Amount Hero) ──
                ShCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true },
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(selectedCategory?.iconName ?: "help"),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Category",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedCategory?.name ?: "Select Category",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // ── Unified Form Section Card ──
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Row 1: Date & Time
                    FormRow(
                        icon = Icons.Outlined.CalendarToday,
                        label = "Date & Time",
                        value = dateText,
                        onClick = { showDatePicker = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Row 2: Account
                    FormRow(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        label = "Account",
                        value = accountName,
                        onClick = { showAccountPicker = true }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Row 3: Payment Method
                    FormRow(
                        icon = Icons.Outlined.Payment,
                        label = "Payment Method",
                        value = paymentMethod,
                        onClick = { showPaymentPicker = true }
                    )
                }

                // ── Additional Details Toggle Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdditionalDetails = !showAdditionalDetails }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Additional Details",
                        fontSize = 14.sp,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector = if (showAdditionalDetails) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = "Toggle Additional Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showAdditionalDetails) {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        // Repeat Option
                        FormRow(
                            icon = Icons.Outlined.Repeat,
                            label = "Repeat Transaction",
                            value = recurringPattern,
                            onClick = { showRepeatPicker = true }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Notes Field
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Notes",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = note,
                                onValueChange = { if (it.length <= 300) viewModel.note.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                decorationBox = { innerTextField ->
                                    if (note.isEmpty()) {
                                        Text(
                                            text = "Add notes about this transaction...",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                            if (note.isNotEmpty()) {
                                Text(
                                    text = "${note.length}/300",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Location Field
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Location / Merchant",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            BasicTextField(
                                value = location,
                                onValueChange = { viewModel.location.value = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                decorationBox = { innerTextField ->
                                    if (location.isEmpty()) {
                                        Text(
                                            text = "Add merchant or location",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (location.isEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    listOf("Starbucks", "Amazon", "Netflix", "Uber").forEach { sug ->
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                                .clickable { viewModel.location.value = sug }
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(sug, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Sticky Save Button ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                    .padding(24.dp)
            ) {
                val saveLabel = when (type) {
                    TransactionType.EXPENSE -> "Save Expense"
                    TransactionType.INCOME -> "Save Income"
                    TransactionType.TRANSFER -> "Save Transfer"
                }

                ShButton(
                    text = if (viewModel.isEditMode) "Save Changes" else saveLabel,
                    onClick = {
                        viewModel.saveTransaction {
                            onBackClick()
                        }
                    },
                    enabled = amount.isNotBlank() && (amount.replace(",", ".").toDoubleOrNull() ?: 0.0) > 0.0
                )
            }
        }
    }
}

    // ── Calculator Bottom Sheet ──
    if (showCalculator) {
        var calcExpr by remember { mutableStateOf(amount) }
        val evalPreview = remember(calcExpr) {
            val eval = evaluateExpression(calcExpr)
            DecimalFormat("#,##0.00").format(eval)
        }

        ModalBottomSheet(
            onDismissRequest = { showCalculator = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Enter Amount",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Expression display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = calcExpr.ifBlank { "0" },
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol$evalPreview",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Keyboard Grid
                val buttons = listOf(
                    listOf("7", "8", "9", "÷"),
                    listOf("4", "5", "6", "×"),
                    listOf("1", "2", "3", "-"),
                    listOf("0", ".", "C", "+"),
                    listOf("%", "Split", "Tax", "Del")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    buttons.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { char ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (char in listOf("+", "-", "×", "÷", "C", "Del", "Split", "Tax"))
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            when (char) {
                                                "C" -> calcExpr = ""
                                                "Del" -> if (calcExpr.isNotEmpty()) calcExpr = calcExpr.dropLast(1)
                                                "Tax" -> {
                                                    val eval = evaluateExpression(calcExpr)
                                                    calcExpr = if (eval > 0.0) (eval * 1.18).toString() else ""
                                                }
                                                "Split" -> {
                                                    // Append divide by 2 for quick split
                                                    calcExpr += "÷2"
                                                }
                                                "×" -> calcExpr += "×"
                                                "÷" -> calcExpr += "÷"
                                                else -> calcExpr += char
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }

                // Apply Button
                Button(
                    onClick = {
                        val eval = evaluateExpression(calcExpr)
                        viewModel.amount.value = if (eval > 0.0) DecimalFormat("#.##").format(eval) else ""
                        showCalculator = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("Apply Amount", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── Category Selector Bottom Sheet ──
    if (showCategoryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCategoryPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Category",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    items(filteredCategories) { cat ->
                        val isSelected = categoryId == cat.id
                        val cColor = com.vesper.ledger.ui.components.safeParseColor(cat.colorHex)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) cColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                )
                                .border(
                                    if (isSelected) BorderStroke(1.5.dp, cColor)
                                    else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.categoryId.value = cat.id
                                    showCategoryPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = getIconByName(cat.iconName),
                                    contentDescription = cat.name,
                                    tint = if (isSelected) cColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Date & Time Picker Bottom Sheet ──
    if (showDatePicker) {
        var isCustomDateTime by remember { mutableStateOf(false) }
        var showM3DatePicker by remember { mutableStateOf(false) }
        var showM3TimePicker by remember { mutableStateOf(false) }

        val calendar = remember(dateEpochMillis) {
            Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
        }

        ModalBottomSheet(
            onDismissRequest = { showDatePicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Date & Time Source",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Radio options: Current Time vs Custom Date & Time
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCustomDateTime = false }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = !isCustomDateTime,
                            onClick = { isCustomDateTime = false }
                        )
                        Column {
                            Text("Current Time", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Uses system current time when saving", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCustomDateTime = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = isCustomDateTime,
                            onClick = { isCustomDateTime = true }
                        )
                        Column {
                            Text("Custom Date & Time", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Specify date and time manually", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isCustomDateTime) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text("Presets", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val now = System.currentTimeMillis()
                        val isToday = isSameDay(dateEpochMillis, now)
                        val isYesterday = isSameDay(dateEpochMillis, now - 86400000L)

                        PresetChip(
                            label = "Today",
                            isSelected = isToday,
                            onClick = {
                                val c = Calendar.getInstance()
                                c.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                                c.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                                viewModel.dateEpochMillis.value = c.timeInMillis
                            }
                        )

                        PresetChip(
                            label = "Yesterday",
                            isSelected = isYesterday,
                            onClick = {
                                val c = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
                                c.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                                c.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                                viewModel.dateEpochMillis.value = c.timeInMillis
                            }
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Date & Time Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Select Field
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { showM3DatePicker = true }
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(dateEpochMillis)),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(Icons.Outlined.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }

                        // Time Select Field
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .clickable { showM3TimePicker = true }
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(dateEpochMillis)),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(Icons.Outlined.AccessTime, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apply Button
                Button(
                    onClick = {
                        if (!isCustomDateTime) {
                            viewModel.dateEpochMillis.value = System.currentTimeMillis()
                        }
                        showDatePicker = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("Apply Date & Time", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Native Material 3 Date Picker Dialog
        if (showM3DatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateEpochMillis)
            DatePickerDialog(
                onDismissRequest = { showM3DatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDate = datePickerState.selectedDateMillis
                            if (selectedDate != null) {
                                val cSelected = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                val cCurrent = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
                                cCurrent.set(cSelected.get(Calendar.YEAR), cSelected.get(Calendar.MONTH), cSelected.get(Calendar.DAY_OF_MONTH))
                                viewModel.dateEpochMillis.value = cCurrent.timeInMillis
                            }
                            showM3DatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showM3DatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Native Material 3 Time Picker Dialog
        if (showM3TimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = calendar.get(Calendar.MINUTE),
                is24Hour = false
            )
            var keyboardMode by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showM3TimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val cCurrent = Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
                            cCurrent.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cCurrent.set(Calendar.MINUTE, timePickerState.minute)
                            viewModel.dateEpochMillis.value = cCurrent.timeInMillis
                            showM3TimePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { keyboardMode = !keyboardMode }) {
                            Icon(
                                imageVector = if (keyboardMode) Icons.Outlined.AccessTime else Icons.Outlined.Keyboard,
                                contentDescription = "Toggle Input Mode"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { showM3TimePicker = false }) {
                            Text("Cancel")
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (keyboardMode) {
                            TimeInput(state = timePickerState)
                        } else {
                            TimePicker(state = timePickerState)
                        }
                    }
                }
            )
        }
    }

    // ── Account Selector Bottom Sheet ──
    if (showAccountPicker) {
        val accounts = listOf(
            Pair("Cash Wallet", "Available: $currencySymbol" + "12,500.00"),
            Pair("Bank Account", "Available: $currencySymbol" + "84,200.00"),
            Pair("Savings Goal", "Goal Target: $currencySymbol" + "15,000.00")
        )
        var searchQuery by remember { mutableStateOf("") }
        val filtered = accounts.filter { it.first.contains(searchQuery, ignoreCase = true) }

        ModalBottomSheet(
            onDismissRequest = { showAccountPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Account",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Search box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search account...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.accountName.value = acc.first
                                    showAccountPicker = false
                                }
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(acc.first, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(acc.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (accountName == acc.first) {
                                Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Payment Method Bottom Sheet ──
    if (showPaymentPicker) {
        val methods = listOf("Cash", "Debit Card", "Credit Card", "UPI", "Bank Transfer", "Wallet")

        ModalBottomSheet(
            onDismissRequest = { showPaymentPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Payment Method",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(methods) { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.paymentMethod.value = method
                                    showPaymentPicker = false
                                }
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(method, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (paymentMethod == method) {
                                Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Repeat Option Selector Sheet ──
    if (showRepeatPicker) {
        val patterns = listOf("One Time", "Daily", "Weekly", "Monthly", "Yearly")

        ModalBottomSheet(
            onDismissRequest = { showRepeatPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Repeat Transaction",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(patterns) { pattern ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.recurringPattern.value = pattern
                                    showRepeatPicker = false
                                }
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pattern, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            if (recurringPattern == pattern) {
                                Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ShadcnSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(surfaceColor)
            .border(1.dp, outlineColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else Color.Transparent
                    )
                    .clickable { onItemSelected(index) }
                    .then(
                        if (isSelected) Modifier.border(1.dp, outlineColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    fontSize = 14.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) onBgColor else secTextColor
                )
            }
        }
    }
}

// Helper math expression evaluator
fun evaluateExpression(expr: String): Double {
    if (expr.isBlank()) return 0.0
    try {
        val cleanExpr = expr.replace("×", "*").replace("÷", "/")
        val tokens = mutableListOf<String>()
        var currentNum = ""
        for (char in cleanExpr) {
            if (char in listOf('+', '-', '*', '/')) {
                if (currentNum.isNotEmpty()) {
                    tokens.add(currentNum)
                    currentNum = ""
                }
                tokens.add(char.toString())
            } else if (char != ' ') {
                currentNum += char
            }
        }
        if (currentNum.isNotEmpty()) {
            tokens.add(currentNum)
        }

        if (tokens.isEmpty()) return 0.0

        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "*" || tokens[i] == "/") {
                val op = tokens[i]
                val prev = tokens.getOrNull(i - 1)?.toDoubleOrNull() ?: 0.0
                val next = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 1.0
                val res = if (op == "*") prev * next else {
                    if (next == 0.0) 0.0 else prev / next
                }
                tokens[i - 1] = res.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
                i--
            }
            i++
        }

        var result = tokens.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val next = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
            result = if (op == "+") result + next else result - next
            i += 2
        }
        return result
    } catch (e: Exception) {
        return 0.0
    }
}

fun isSameDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
           c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(containerColor)
            .border(borderStroke, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            color = contentColor
        )
    }
}
