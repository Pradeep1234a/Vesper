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
    var amountText by remember { mutableStateOf("2,450") }
    var titleText by remember { mutableStateOf("Grocery Shopping") }

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
        containerColor = Color(0xFF09090B), // Sleek Dark Surface
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. SEGMENTED TAB SELECTOR (Income | Expense | Transfer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF18181B))
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
                                .background(if (selected) Color.White else Color.Transparent)
                                .clickable { type = tabType },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (selected) Color.Black else Color(0xFFA1A1AA)
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
                            Text(
                                text = "$currencySymbol ${if (amountText.isBlank()) "0" else amountText}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = Color.White
                                )
                            )
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

                Spacer(modifier = Modifier.height(6.dp))

                // 8. SAVE TRANSACTION BUTTON
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
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
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

                Spacer(modifier = Modifier.height(32.dp))
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 1: SELECT CATEGORY SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showCategorySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCategorySheet = false },
                    containerColor = Color(0xFF18181B),
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
            // MODAL SHEET 2: CALCULATOR KEYPAD SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showCalculatorSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCalculatorSheet = false },
                    containerColor = Color(0xFF09090B),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$currencySymbol ${if (amountText.isBlank()) "0" else amountText}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = Color.White
                                )
                            )
                            IconButton(onClick = { showCalculatorSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        // 4x3 Numeric Keypad Grid
                        val keys = listOf(
                            "1", "2", "3",
                            "4", "5", "6",
                            "7", "8", "9",
                            "+/-", "0", "⌫"
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(keys) { key ->
                                Surface(
                                    modifier = Modifier
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            when (key) {
                                                "⌫" -> {
                                                    if (amountText.length > 1) {
                                                        amountText = amountText.dropLast(1)
                                                    } else {
                                                        amountText = ""
                                                    }
                                                }
                                                "+/-" -> {}
                                                else -> {
                                                    if (amountText == "0") amountText = key else amountText += key
                                                }
                                            }
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF18181B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showCalculatorSheet = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily, fontSize = 16.sp)
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 3: SELECT DATE PICKER SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showDatePickerSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showDatePickerSheet = false },
                    containerColor = Color(0xFF09090B),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    var currentMonthCal by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedCalendar.timeInMillis }) }
                    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { showDatePickerSheet = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                                }
                                Text(
                                    text = "Select Date",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        // Month Switcher Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthFormat.format(currentMonthCal.time),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
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
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                )
                            }
                        }

                        // Calendar Grid Days
                        val daysInMonth = currentMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val tempCal = (currentMonthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                        val firstDayOfWeekIndex = tempCal.get(Calendar.DAY_OF_WEEK) - 1

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Blank slots before day 1
                            items(firstDayOfWeekIndex) {
                                Box(modifier = Modifier.size(36.dp))
                            }

                            items(daysInMonth) { dayIndex ->
                                val dayNumber = dayIndex + 1
                                val isSelected = selectedCalendar.get(Calendar.YEAR) == currentMonthCal.get(Calendar.YEAR) &&
                                        selectedCalendar.get(Calendar.MONTH) == currentMonthCal.get(Calendar.MONTH) &&
                                        selectedCalendar.get(Calendar.DAY_OF_MONTH) == dayNumber

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
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
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showDatePickerSheet = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily, fontSize = 16.sp)
                        }
                    }
                }
            }

            // ────────────────────────────────────────────────────────────────────────
            // MODAL SHEET 4: SELECT TIME PICKER SHEET
            // ────────────────────────────────────────────────────────────────────────
            if (showTimePickerSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showTimePickerSheet = false },
                    containerColor = Color(0xFF09090B),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    var selectedHour by remember { mutableStateOf(selectedCalendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }) }
                    var selectedMinute by remember { mutableStateOf(selectedCalendar.get(Calendar.MINUTE)) }
                    var isAm by remember { mutableStateOf(selectedCalendar.get(Calendar.AM_PM) == Calendar.AM) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { showTimePickerSheet = false }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                                }
                                Text(
                                    text = "Select Time",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            // AM / PM Toggle Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF18181B))
                                    .padding(2.dp)
                            ) {
                                Text(
                                    text = "AM",
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isAm) Color.White else Color.Transparent)
                                        .clickable { isAm = true }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
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
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isAm) Color.Black else Color(0xFFA1A1AA)
                                    )
                                )
                            }
                        }

                        // Time Display Box
                        Text(
                            text = String.format("%02d : %02d %s", selectedHour, selectedMinute, if (isAm) "AM" else "PM"),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color.White
                            )
                        )

                        // Analog Clock Face Dial
                        Surface(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape),
                            color = Color(0xFF18181B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27272A))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                listOf(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).forEach { hour ->
                                    val isSelectedHour = hour == selectedHour
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelectedHour) Color.White else Color.Transparent)
                                            .clickable { selectedHour = hour },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$hour",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = SpaceGroteskFamily,
                                                fontWeight = if (isSelectedHour) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelectedHour) Color.Black else Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val newCal = (selectedCalendar.clone() as Calendar).apply {
                                    var h = selectedHour % 12
                                    if (!isAm) h += 12
                                    set(Calendar.HOUR_OF_DAY, h)
                                    set(Calendar.MINUTE, selectedMinute)
                                }
                                selectedCalendar = newCal
                                showTimePickerSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily, fontSize = 16.sp)
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
