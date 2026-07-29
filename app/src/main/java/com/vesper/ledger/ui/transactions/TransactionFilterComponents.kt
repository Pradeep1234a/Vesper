package com.vesper.ledger.ui.transactions

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Filter Master Navigation Sections
 */
enum class FilterSection {
    MASTER,
    DATE_TIME,
    TYPE,
    CATEGORIES,
    ACCOUNTS,
    AMOUNT_SORT
}

/**
 * Date Mode: Single Date vs Date Range
 */
enum class DateFilterMode {
    SINGLE_DATE,
    DATE_RANGE
}

/**
 * Material 3 Master-Detail Draggable Transaction Filter Sheet:
 * - Height: 45% screen height default, expandable up to 85% screen height.
 * - Top centered M3 drag handle bar.
 * - Master screen with 5 numbered bento section cards.
 * - STRICTLY CHIP-FREE Section Detail screens (Full-width Bento List Rows).
 * - Official M3 Date Picker & Date Range Picker integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3TransactionFilterSheet(
    viewModel: TransactionsViewModel,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String,
    filteredCount: Int,
    onDismissRequest: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var currentSection by remember { mutableStateOf(FilterSection.MASTER) }

    // Filter states collected from ViewModel
    val selectedDatePreset by viewModel.selectedDatePreset.collectAsState()
    val startDateFilter by viewModel.startDateFilter.collectAsState()
    val endDateFilter by viewModel.endDateFilter.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedAccount by viewModel.selectedAccount.collectAsState()
    val minAmountFilter by viewModel.minAmountFilter.collectAsState()
    val maxAmountFilter by viewModel.maxAmountFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    // Date Picker States
    var dateMode by remember { mutableStateOf(DateFilterMode.DATE_RANGE) }
    var showM3DatePickerDialog by remember { mutableStateOf(false) }
    var showM3DateRangePickerDialog by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
        windowInsets = WindowInsets(0, 0, 0, 0),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header (Master Title or Section Back Title)
                FilterSheetHeader(
                    currentSection = currentSection,
                    activeCount = activeFilterCount,
                    onBackClick = { currentSection = FilterSection.MASTER },
                    onResetAll = { viewModel.clearAllFilters() },
                    onClose = onDismissRequest
                )

                // Smooth AnimatedContent Master <-> Detail Transition
                AnimatedContent(
                    targetState = currentSection,
                    transitionSpec = {
                        if (targetState != FilterSection.MASTER) {
                            slideInHorizontally(tween(250)) { it } + fadeIn(tween(250)) togetherWith
                                    slideOutHorizontally(tween(200)) { -it } + fadeOut(tween(200))
                        } else {
                            slideInHorizontally(tween(250)) { -it } + fadeIn(tween(250)) togetherWith
                                    slideOutHorizontally(tween(200)) { it } + fadeOut(tween(200))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { section ->
                    when (section) {
                        FilterSection.MASTER -> FilterMasterView(
                            selectedDatePreset = selectedDatePreset,
                            startDateMillis = startDateFilter,
                            endDateMillis = endDateFilter,
                            selectedType = selectedType,
                            selectedCategoriesCount = selectedCategories.size,
                            selectedAccountName = accounts.find { it.id == selectedAccount }?.name,
                            minAmount = minAmountFilter,
                            maxAmount = maxAmountFilter,
                            sortBy = sortBy,
                            currencySymbol = currencySymbol,
                            onSectionClick = { currentSection = it }
                        )

                        FilterSection.DATE_TIME -> FilterDateDetailSection(
                            dateMode = dateMode,
                            onDateModeChange = { dateMode = it },
                            selectedPreset = selectedDatePreset,
                            startDateMillis = startDateFilter,
                            endDateMillis = endDateFilter,
                            dateFormat = dateFormat,
                            onPresetSelect = { preset -> viewModel.setDatePreset(preset) },
                            onOpenSinglePicker = { showM3DatePickerDialog = true },
                            onOpenRangePicker = { showM3DateRangePickerDialog = true }
                        )

                        FilterSection.TYPE -> FilterTypeDetailSection(
                            selectedType = selectedType,
                            onTypeSelect = { type -> viewModel.selectedType.value = type }
                        )

                        FilterSection.CATEGORIES -> FilterCategoryDetailSection(
                            categories = categories,
                            selectedCategories = selectedCategories,
                            onToggleCategory = { id ->
                                val newSet = if (selectedCategories.contains(id)) selectedCategories - id else selectedCategories + id
                                viewModel.selectedCategories.value = newSet
                            }
                        )

                        FilterSection.ACCOUNTS -> FilterAccountDetailSection(
                            accounts = accounts,
                            selectedAccount = selectedAccount,
                            onSelectAccount = { id ->
                                viewModel.selectedAccount.value = if (selectedAccount == id) null else id
                            }
                        )

                        FilterSection.AMOUNT_SORT -> FilterAmountSortDetailSection(
                            minAmount = minAmountFilter,
                            maxAmount = maxAmountFilter,
                            sortBy = sortBy,
                            currencySymbol = currencySymbol,
                            onMinAmountChange = { viewModel.minAmountFilter.value = it },
                            onMaxAmountChange = { viewModel.maxAmountFilter.value = it },
                            onSortChange = { viewModel.sortBy.value = it }
                        )
                    }
                }

                // Bottom Action Footer Bar
                FilterSheetFooter(
                    filteredCount = filteredCount,
                    activeFilterCount = activeFilterCount,
                    onResetAll = { viewModel.clearAllFilters() },
                    onApply = onDismissRequest
                )
            }
        }
    }

    // Official M3 Single Date Picker Dialog
    if (showM3DatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateFilter ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showM3DatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { ms ->
                        viewModel.setDatePreset(DatePreset.CUSTOM, ms, ms)
                    }
                    showM3DatePickerDialog = false
                }) {
                    Text("OK", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showM3DatePickerDialog = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Official M3 Date Range Picker Dialog (Matching user spec diagram)
    if (showM3DateRangePickerDialog) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = startDateFilter ?: System.currentTimeMillis(),
            initialSelectedEndDateMillis = endDateFilter ?: System.currentTimeMillis()
        )
        DateRangePickerDialog(
            state = dateRangePickerState,
            onDismiss = { showM3DateRangePickerDialog = false },
            onConfirm = {
                val start = dateRangePickerState.selectedStartDateMillis
                val end = dateRangePickerState.selectedEndDateMillis
                if (start != null && end != null) {
                    viewModel.setDatePreset(DatePreset.CUSTOM, start, end)
                }
                showM3DateRangePickerDialog = false
            }
        )
    }
}

/**
 * Top Navigation Header of Filter Bottom Sheet
 */
@Composable
private fun FilterSheetHeader(
    currentSection: FilterSection,
    activeCount: Int,
    onBackClick: () -> Unit,
    onResetAll: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (currentSection != FilterSection.MASTER) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Master",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = when (currentSection) {
                    FilterSection.DATE_TIME -> "1. Date & Time Range"
                    FilterSection.TYPE -> "2. Transaction Type"
                    FilterSection.CATEGORIES -> "3. Categories"
                    FilterSection.ACCOUNTS -> "4. Accounts & Methods"
                    FilterSection.AMOUNT_SORT -> "5. Amount & Sorting"
                    else -> "Filters"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Transaction Filters",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (activeCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$activeCount active",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Sheet",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Master Navigation Screen: 5 Numbered Bento Cards
 */
@Composable
private fun FilterMasterView(
    selectedDatePreset: DatePreset,
    startDateMillis: Long?,
    endDateMillis: Long?,
    selectedType: TransactionType?,
    selectedCategoriesCount: Int,
    selectedAccountName: String?,
    minAmount: Double?,
    maxAmount: Double?,
    sortBy: SortOption,
    currencySymbol: String,
    onSectionClick: (FilterSection) -> Unit
) {
    val df = remember { DecimalFormat("#,##0") }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Date & Time
        item {
            val dateSubtitle = when {
                startDateMillis != null && endDateMillis != null -> "${dateFormat.format(Date(startDateMillis))} - ${dateFormat.format(Date(endDateMillis))}"
                selectedDatePreset != DatePreset.ALL -> selectedDatePreset.label
                else -> "All Time"
            }
            FilterMasterBentoCard(
                number = "1",
                title = "Date & Time Range",
                subtitle = dateSubtitle,
                icon = Icons.Outlined.DateRange,
                isActive = selectedDatePreset != DatePreset.ALL || startDateMillis != null,
                onClick = { onSectionClick(FilterSection.DATE_TIME) }
            )
        }

        // Section 2: Transaction Type
        item {
            val typeSubtitle = when (selectedType) {
                TransactionType.EXPENSE -> "Expenses Only"
                TransactionType.INCOME -> "Income Only"
                TransactionType.TRANSFER -> "Transfers Only"
                null -> "All Types (Expenses, Income, Transfers)"
            }
            FilterMasterBentoCard(
                number = "2",
                title = "Transaction Type",
                subtitle = typeSubtitle,
                icon = Icons.Outlined.SwapHoriz,
                isActive = selectedType != null,
                onClick = { onSectionClick(FilterSection.TYPE) }
            )
        }

        // Section 3: Categories
        item {
            val catSubtitle = if (selectedCategoriesCount > 0) "$selectedCategoriesCount categories selected" else "All Categories"
            FilterMasterBentoCard(
                number = "3",
                title = "Categories",
                subtitle = catSubtitle,
                icon = Icons.Outlined.Category,
                isActive = selectedCategoriesCount > 0,
                onClick = { onSectionClick(FilterSection.CATEGORIES) }
            )
        }

        // Section 4: Accounts & Methods
        item {
            val accountSubtitle = selectedAccountName ?: "All Accounts & Payment Methods"
            FilterMasterBentoCard(
                number = "4",
                title = "Accounts & Payment Methods",
                subtitle = accountSubtitle,
                icon = Icons.Outlined.AccountBalance,
                isActive = selectedAccountName != null,
                onClick = { onSectionClick(FilterSection.ACCOUNTS) }
            )
        }

        // Section 5: Amount Range & Sorting
        item {
            val minText = minAmount?.let { "${currencySymbol}${df.format(it)}" } ?: "${currencySymbol}0"
            val maxText = maxAmount?.let { "${currencySymbol}${df.format(it)}" } ?: "Max"
            val sortText = when (sortBy) {
                SortOption.DATE_DESC -> "Newest First"
                SortOption.DATE_ASC -> "Oldest First"
                SortOption.AMOUNT_DESC -> "Highest Amount"
                SortOption.AMOUNT_ASC -> "Lowest Amount"
                else -> "Custom Sort"
            }
            FilterMasterBentoCard(
                number = "5",
                title = "Amount Range & Sorting",
                subtitle = "$minText - $maxText • $sortText",
                icon = Icons.Outlined.Sort,
                isActive = minAmount != null || maxAmount != null || sortBy != SortOption.DATE_DESC,
                onClick = { onSectionClick(FilterSection.AMOUNT_SORT) }
            )
        }
    }
}

/**
 * Master Bento Navigation Card Component
 */
@Composable
private fun FilterMasterBentoCard(
    number: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Section 1 Detail: Date & Time Range (STRICTLY CHIP-FREE Full-Width Bento Rows)
 */
@Composable
private fun FilterDateDetailSection(
    dateMode: DateFilterMode,
    onDateModeChange: (DateFilterMode) -> Unit,
    selectedPreset: DatePreset,
    startDateMillis: Long?,
    endDateMillis: Long?,
    dateFormat: SimpleDateFormat,
    onPresetSelect: (DatePreset) -> Unit,
    onOpenSinglePicker: () -> Unit,
    onOpenRangePicker: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Selection Mode Toggle (Single Date vs Date Range)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BentoModeTabRow(
                    title = "Single Date Picker",
                    icon = Icons.Default.CalendarToday,
                    isSelected = dateMode == DateFilterMode.SINGLE_DATE,
                    onClick = { onDateModeChange(DateFilterMode.SINGLE_DATE) },
                    modifier = Modifier.weight(1f)
                )
                BentoModeTabRow(
                    title = "Date Range Picker",
                    icon = Icons.Default.DateRange,
                    isSelected = dateMode == DateFilterMode.DATE_RANGE,
                    onClick = { onDateModeChange(DateFilterMode.DATE_RANGE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Picker Trigger Card
        item {
            Surface(
                onClick = {
                    if (dateMode == DateFilterMode.SINGLE_DATE) onOpenSinglePicker() else onOpenRangePicker()
                },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (dateMode == DateFilterMode.SINGLE_DATE) Icons.Default.CalendarToday else Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (dateMode == DateFilterMode.SINGLE_DATE) "Select Specific Date" else "Select Custom Date Range",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val displayDate = when {
                            dateMode == DateFilterMode.SINGLE_DATE && startDateMillis != null -> dateFormat.format(Date(startDateMillis))
                            dateMode == DateFilterMode.DATE_RANGE && startDateMillis != null && endDateMillis != null -> "${dateFormat.format(Date(startDateMillis))} - ${dateFormat.format(Date(endDateMillis))}"
                            else -> "Tap to open Material 3 Date Picker"
                        }
                        Text(
                            text = displayDate,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Presets List Bento Rows
        item {
            Text(
                text = "PRESET RANGES",
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        val presets = listOf(
            DatePreset.ALL to "All Time",
            DatePreset.TODAY to "Today",
            DatePreset.THIS_WEEK to "This Week",
            DatePreset.THIS_MONTH to "This Month",
            DatePreset.CUSTOM to "Custom Date Range"
        )

        items(presets) { (preset, label) ->
            FilterBentoSelectableRow(
                title = label,
                isSelected = selectedPreset == preset,
                onClick = { onPresetSelect(preset) }
            )
        }
    }
}

/**
 * Tab Row component for switching single date vs date range mode
 */
@Composable
private fun BentoModeTabRow(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Section 2 Detail: Transaction Type (STRICTLY CHIP-FREE Full-Width Bento Rows)
 */
@Composable
private fun FilterTypeDetailSection(
    selectedType: TransactionType?,
    onTypeSelect: (TransactionType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilterBentoSelectableRow(
            title = "All Transaction Types",
            subtitle = "Show Expenses, Income & Transfers",
            icon = Icons.Outlined.SwapHoriz,
            isSelected = selectedType == null,
            onClick = { onTypeSelect(null) }
        )

        FilterBentoSelectableRow(
            title = "Expenses Only",
            subtitle = "Outflowing transactions",
            icon = Icons.Outlined.TrendingDown,
            accentColor = Color(0xFFEF4444),
            isSelected = selectedType == TransactionType.EXPENSE,
            onClick = { onTypeSelect(TransactionType.EXPENSE) }
        )

        FilterBentoSelectableRow(
            title = "Income Only",
            subtitle = "Inflowing earnings & deposits",
            icon = Icons.Outlined.TrendingUp,
            accentColor = Color(0xFF22C55E),
            isSelected = selectedType == TransactionType.INCOME,
            onClick = { onTypeSelect(TransactionType.INCOME) }
        )

        FilterBentoSelectableRow(
            title = "Transfers Only",
            subtitle = "Account-to-account transfers",
            icon = Icons.Outlined.SwapHoriz,
            accentColor = Color(0xFF38BDF8),
            isSelected = selectedType == TransactionType.TRANSFER,
            onClick = { onTypeSelect(TransactionType.TRANSFER) }
        )
    }
}

/**
 * Section 3 Detail: Categories (STRICTLY CHIP-FREE Full-Width Bento Rows with Search)
 */
@Composable
private fun FilterCategoryDetailSection(
    categories: List<Category>,
    selectedCategories: Set<Long>,
    onToggleCategory: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search categories...", fontFamily = PlusJakartaSansFamily, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCategories, key = { it.id }) { category ->
                val isSelected = selectedCategories.contains(category.id)
                FilterBentoSelectableRow(
                    title = category.name,
                    subtitle = category.type.name,
                    icon = getIconByName(category.iconName),
                    accentColor = try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary },
                    isSelected = isSelected,
                    onClick = { onToggleCategory(category.id) }
                )
            }
        }
    }
}

/**
 * Section 4 Detail: Accounts & Payment Methods (STRICTLY CHIP-FREE Full-Width Bento Rows)
 */
@Composable
private fun FilterAccountDetailSection(
    accounts: List<Account>,
    selectedAccount: Long?,
    onSelectAccount: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FilterBentoSelectableRow(
                title = "All Accounts",
                subtitle = "Include transactions across all accounts",
                icon = Icons.Outlined.AccountBalance,
                isSelected = selectedAccount == null,
                onClick = { onSelectAccount(-1) }
            )
        }

        items(accounts, key = { it.id }) { account ->
            val isSelected = selectedAccount == account.id
            FilterBentoSelectableRow(
                title = account.name,
                subtitle = account.type,
                icon = getIconByName(account.iconName),
                isSelected = isSelected,
                onClick = { onSelectAccount(account.id) }
            )
        }
    }
}

/**
 * Section 5 Detail: Amount Range & Sorting (STRICTLY CHIP-FREE Full-Width Bento Rows)
 */
@Composable
private fun FilterAmountSortDetailSection(
    minAmount: Double?,
    maxAmount: Double?,
    sortBy: SortOption,
    currencySymbol: String,
    onMinAmountChange: (Double?) -> Unit,
    onMaxAmountChange: (Double?) -> Unit,
    onSortChange: (SortOption) -> Unit
) {
    val df = remember { DecimalFormat("#,##0") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Amount Range Slider Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMOUNT RANGE",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val minLabel = minAmount?.let { "${currencySymbol}${df.format(it)}" } ?: "${currencySymbol}0"
                        val maxLabel = maxAmount?.let { "${currencySymbol}${df.format(it)}" } ?: "${currencySymbol}50,000+"
                        Text(
                            text = "$minLabel - $maxLabel",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    var sliderRange by remember(minAmount, maxAmount) {
                        val currentMin = minAmount?.toFloat() ?: 0f
                        val currentMax = maxAmount?.toFloat() ?: 50000f
                        mutableStateOf(currentMin..currentMax)
                    }

                    RangeSlider(
                        value = sliderRange,
                        onValueChange = { range ->
                            sliderRange = range
                            onMinAmountChange(if (range.start > 0f) range.start.toDouble() else null)
                            onMaxAmountChange(if (range.endInclusive < 50000f) range.endInclusive.toDouble() else null)
                        },
                        valueRange = 0f..50000f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        // Sorting Bento List Rows
        item {
            Text(
                text = "SORTING ORDER",
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
            )
        }

        val sortOptions = listOf(
            SortOption.DATE_DESC to "Newest First",
            SortOption.DATE_ASC to "Oldest First",
            SortOption.AMOUNT_DESC to "Highest Amount",
            SortOption.AMOUNT_ASC to "Lowest Amount"
        )

        items(sortOptions) { (option, label) ->
            FilterBentoSelectableRow(
                title = label,
                isSelected = sortBy == option,
                onClick = { onSortChange(option) }
            )
        }
    }
}

/**
 * Universal Full-Width Bento Selectable Row Component (Strictly Chip-Free Design)
 */
@Composable
private fun FilterBentoSelectableRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) accentColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Action Footer Bar
 */
@Composable
private fun FilterSheetFooter(
    filteredCount: Int,
    activeFilterCount: Int,
    onResetAll: () -> Unit,
    onApply: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onResetAll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeFilterCount > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (activeFilterCount > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (activeFilterCount > 0) "Reset All ($activeFilterCount)" else "Reset All",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Apply ($filteredCount)",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Official Material 3 Date Range Picker Dialog Composable (Matching M3 Spec Diagram)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    state: DateRangePickerState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGroteskFamily)
            }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text(
                    text = "Select Date Range",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                Text(
                    text = state.selectedStartDateMillis?.let { start ->
                        state.selectedEndDateMillis?.let { end ->
                            val df = SimpleDateFormat("MMM dd", Locale.getDefault())
                            "${df.format(Date(start))} – ${df.format(Date(end))}"
                        } ?: SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(start))
                    } ?: "Start - End dates",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            },
            modifier = Modifier.height(450.dp)
        )
    }
}
