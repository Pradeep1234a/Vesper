package com.vesper.ledger.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import java.util.Calendar
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.RadioButton
import androidx.compose.animation.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ReceiptLong
import kotlinx.coroutines.delay

enum class FilterSheetView {
    FILTERS, CATEGORIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    currencySymbol: String,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onAddTransactionClick: (type: String?, id: Long?) -> Unit,
    onAddCategoryClick: () -> Unit = {}
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    var showAction1 by remember { mutableStateOf(false) }
    var showAction2 by remember { mutableStateOf(false) }
    var showAction3 by remember { mutableStateOf(false) }

    LaunchedEffect(isFabExpanded) {
        if (isFabExpanded) {
            showAction1 = true
            delay(35)
            showAction2 = true
            delay(35)
            showAction3 = true
        } else {
            showAction3 = false
            delay(35)
            showAction2 = false
            delay(35)
            showAction1 = false
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val adaptiveCategories by viewModel.adaptiveCategories.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    val sdfShort = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val context = LocalContext.current

    val groupedTransactions = remember(transactions) {
        transactions.groupBy { tx ->
            dateFormat.format(Date(tx.dateEpochMillis))
        }
    }

    val listState = rememberLazyListState()
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Delete Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top action: Add Transaction
                ExpandedActionItem(
                    label = "Add Transaction",
                    icon = Icons.Outlined.ReceiptLong,
                    visible = showAction3,
                    onClick = {
                        isFabExpanded = false
                        onAddTransactionClick(null, null)
                    }
                )
                
                // Middle action: Add Category
                ExpandedActionItem(
                    label = "Add Category",
                    icon = Icons.Outlined.Category,
                    visible = showAction2,
                    onClick = {
                        isFabExpanded = false
                        onAddCategoryClick()
                    }
                )


                // FAB Origin Button
                FabOriginButton(
                    isExpanded = isFabExpanded,
                    onClick = { isFabExpanded = !isFabExpanded }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            RootHeader(
                title = "Transactions",
                onMenuClick = onMenuClick
            )
            // Search Input Field & Filter Menu Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Grouped Transaction list
            if (groupedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // extra padding to clear the FAB!
                ) {
                    groupedTransactions.forEach { (dateStr, txList) ->
                        item {
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(txList) { tx ->
                            var showMenu by remember { mutableStateOf(false) }
                            val category = categories.find { it.id == tx.categoryId }
                            val isIncome = tx.type == TransactionType.INCOME
                            val accentColor = if (isIncome) Color(0xFF16A34A) else Color(0xFFDC2626)
                            val accentBg = if (isIncome) Color(0xFF16A34A).copy(alpha = 0.08f) else Color(0xFFDC2626).copy(alpha = 0.08f)
                            val iconName = category?.iconName ?: "category"
                            val categoryLabel = category?.name ?: "Uncategorized"

                            ShCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { showMenu = true },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category Icon Container
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    color = accentBg,
                                                    shape = RoundedCornerShape(10.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconByName(iconName),
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        
                                        // Title/Category & Amount/Date aligned up-and-down
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left Column: Title on top, Category Name on bottom
                                            Column(modifier = Modifier.weight(1f)) {
                                                val displayTitle = if (tx.title.isBlank() || tx.title == "Untitled Transaction") categoryLabel else tx.title
                                                Text(
                                                    text = displayTitle,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    maxLines = 1
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "$categoryLabel • ${tx.accountName}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    maxLines = 1
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            // Right Column: Amount on top, Date on bottom
                                            Column(horizontalAlignment = Alignment.End) {
                                                val prefix = if (isIncome) "+" else "-"
                                                Text(
                                                    text = "$prefix$currencySymbol${df.format(tx.amount)}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 14.sp,
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${sdfShort.format(Date(tx.dateEpochMillis))} • ${timeFormat.format(Date(tx.dateEpochMillis))}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    // CRUD Action Menu
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Edit,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurface,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Edit", style = MaterialTheme.typography.bodyMedium)
                                                }
                                            },
                                            onClick = {
                                                showMenu = false
                                                onAddTransactionClick(tx.type.name, tx.id)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.DeleteOutline,
                                                        contentDescription = null,
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        "Delete",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            color = Color(0xFFDC2626)
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                showMenu = false
                                                transactionToDelete = tx
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Background scrim overlay
            if (isFabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { isFabExpanded = false })
                        }
                )
        }
    }
}
}
}

@Composable
private fun ExpandedActionItem(
    label: String,
    icon: ImageVector,
    visible: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 } +
                scaleIn(spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.8f),
        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
               slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { it / 3 } +
               scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.8f)
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scalePress by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "actionPressScale"
        )
        
        val isDark = isSystemInDarkTheme()
        val bgColor = if (isDark) Color(0xFF111111) else Color(0xFFF7F7F8)
        
        Row(
            modifier = Modifier
                .scale(scalePress)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(18.dp)
                )
                .background(bgColor, RoundedCornerShape(18.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun FabOriginButton(
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scalePress by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fabPressScale"
    )
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )
    
    val containerColor = MaterialTheme.colorScheme.onBackground
    val iconColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .scale(scalePress)
            .size(64.dp)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = iconColor.copy(alpha = 0.1f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Menu",
            tint = iconColor,
            modifier = Modifier
                .size(28.dp)
                .rotate(rotationAngle)
        )
    }
}
