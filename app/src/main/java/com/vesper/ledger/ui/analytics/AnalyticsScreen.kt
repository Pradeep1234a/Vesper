package com.vesper.ledger.ui.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalyticsScreen(
    transactions: List<Transaction>,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String = "₹",
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null
) {
    var selectedFilter by remember { mutableStateOf("THIS_MONTH") } // THIS_MONTH, LAST_30_DAYS, THIS_WEEK, THIS_YEAR, ALL
    val df = remember { DecimalFormat("#,##0.00") }
    val dfCompact = remember { DecimalFormat("#,##0") }

    // Calendar Heatmap month offset state (0 = current month, -1 = previous month, etc.)
    var calendarMonthOffset by remember { mutableStateOf(0) }
    var selectedHeatmapDay by remember { mutableStateOf<Int?>(null) }

    // Filter transactions based on selected time period
    val filteredTransactions = remember(transactions, selectedFilter) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        when (selectedFilter) {
            "THIS_WEEK" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val weekStart = cal.timeInMillis
                transactions.filter { it.dateEpochMillis >= weekStart }
            }
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val monthStart = cal.timeInMillis
                transactions.filter { it.dateEpochMillis >= monthStart }
            }
            "LAST_30_DAYS" -> {
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
                transactions.filter { it.dateEpochMillis >= thirtyDaysAgo }
            }
            "THIS_YEAR" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val yearStart = cal.timeInMillis
                transactions.filter { it.dateEpochMillis >= yearStart }
            }
            else -> transactions
        }
    }

    // Financial totals
    val expenseList = remember(filteredTransactions) { filteredTransactions.filter { it.type == TransactionType.EXPENSE } }
    val incomeList = remember(filteredTransactions) { filteredTransactions.filter { it.type == TransactionType.INCOME } }

    val totalExpense = remember(expenseList) { expenseList.sumOf { it.amount } }
    val totalIncome = remember(incomeList) { incomeList.sumOf { it.amount } }
    val netCashflow = totalIncome - totalExpense

    val transactionCount = filteredTransactions.size
    val avgTransactionValue = if (transactionCount > 0) (totalExpense + totalIncome) / transactionCount else 0.0

    // Top spending transaction
    val topSpendingTransaction = remember(expenseList) { expenseList.maxByOrNull { it.amount } }

    // Ratio of Expense vs Income
    val spendIncomeRatio = if (totalIncome > 0) (totalExpense / totalIncome * 100).coerceIn(0.0, 100.0) else if (totalExpense > 0) 100.0 else 0.0

    // Days in selected period
    val daysInPeriod = remember(selectedFilter) {
        when (selectedFilter) {
            "THIS_WEEK" -> 7
            "THIS_MONTH" -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            "LAST_30_DAYS" -> 30
            "THIS_YEAR" -> Calendar.getInstance().get(Calendar.DAY_OF_YEAR).coerceAtLeast(1)
            else -> 30
        }
    }
    val dailyAvgExpense = if (daysInPeriod > 0) totalExpense / daysInPeriod else 0.0

    // Category Breakdown
    val categoryGroup = remember(expenseList) {
        expenseList
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Top Payees / Merchants
    val topMerchants = remember(expenseList) {
        expenseList
            .groupBy { it.title.ifBlank { "General Expense" } }
            .mapValues { entry -> Pair(entry.value.sumOf { it.amount }, entry.value.size) }
            .toList()
            .sortedByDescending { it.second.first }
            .take(5)
    }

    // Payment Method Breakdown
    val paymentModeGroup = remember(expenseList) {
        expenseList
            .groupBy { it.paymentMethod.ifBlank { "Cash" } }
            .mapValues { entry -> Pair(entry.value.sumOf { it.amount }, entry.value.size) }
            .toList()
            .sortedByDescending { it.second.first }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. TOP PERIOD FILTER SELECTOR TABS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "THIS_MONTH" to "This Month",
                "LAST_30_DAYS" to "30 Days",
                "THIS_WEEK" to "Week",
                "THIS_YEAR" to "Year",
                "ALL" to "All Time"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else Color.Transparent)
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = key },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. OVERVIEW FINANCIAL SUMMARY BANNER
        ShCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FINANCIAL SUMMARY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (netCashflow >= 0) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (netCashflow >= 0) Color(0xFF22C55E) else Color(0xFFEF4444))
                    ) {
                        Text(
                            text = if (netCashflow >= 0) "Net Flow +$currencySymbol${dfCompact.format(netCashflow)}" else "Net Flow -$currencySymbol${dfCompact.format(-netCashflow)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netCashflow >= 0) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expenses
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Text("Total Expenses", fontFamily = PlusJakartaSansFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "$currencySymbol${df.format(totalExpense)}",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Divider
                    Box(modifier = Modifier.height(36.dp).width(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))

                    // Income
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                            Text("Total Income", fontFamily = PlusJakartaSansFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "$currencySymbol${df.format(totalIncome)}",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. 2x2 GRID OF QUICK METRIC CARDS
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Row 1: Transactions Count & Monthly Total Transactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "TRANSACTIONS",
                    value = "$transactionCount Total",
                    subtitle = "Avg: $currencySymbol${dfCompact.format(avgTransactionValue)}",
                    accentColor = Color(0xFF38BDF8),
                    icon = Icons.Outlined.ReceiptLong
                )

                AnalyticsKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "DAILY AVG SPEND",
                    value = "$currencySymbol${dfCompact.format(dailyAvgExpense)}",
                    subtitle = "Per Day ($daysInPeriod Days)",
                    accentColor = Color(0xFFA855F7),
                    icon = Icons.Outlined.Schedule
                )
            }

            // Row 2: Top Single Spending & Spending % of Income Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnalyticsKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "TOP SPENDING",
                    value = topSpendingTransaction?.let { "$currencySymbol${dfCompact.format(it.amount)}" } ?: "${currencySymbol}0",
                    subtitle = topSpendingTransaction?.title?.take(14) ?: "No transactions",
                    accentColor = Color(0xFFF59E0B),
                    icon = Icons.Outlined.MilitaryTech
                )

                AnalyticsKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "SPEND / INCOME",
                    value = "${String.format(Locale.getDefault(), "%.1f", spendIncomeRatio)}%",
                    subtitle = if (spendIncomeRatio <= 70) "Healthy Ratio" else "High Ratio",
                    accentColor = if (spendIncomeRatio <= 70) Color(0xFF22C55E) else Color(0xFFEF4444),
                    icon = Icons.Outlined.PieChartOutline
                )
            }
        }

        // 4. CATEGORY BREAKDOWN BENTO CARD
        AnalyticsBentoCard(
            title = "CATEGORY BREAKDOWN",
            icon = Icons.Outlined.Category
        ) {
            if (categoryGroup.isEmpty()) {
                Text(
                    text = "No category expense data recorded for this period.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Donut Chart Canvas
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                            var startAngle = -90f

                            categoryGroup.take(5).forEach { (catId, amt) ->
                                val cat = categories.find { it.id == catId }
                                val color = safeParseColor(cat?.colorHex ?: "#71717A")
                                val sweepAngle = if (totalExpense > 0) ((amt / totalExpense) * 360f).toFloat() else 0f

                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                startAngle += sweepAngle
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TOTAL",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$currencySymbol${dfCompact.format(totalExpense)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Right: Top Category Bento Cards List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryGroup.take(4).forEach { (catId, amt) ->
                            val cat = categories.find { it.id == catId }
                            val catName = cat?.name ?: "Uncategorized"
                            val catColor = safeParseColor(cat?.colorHex ?: "#71717A")
                            val catIcon = cat?.iconName ?: "category"
                            val percentage = if (totalExpense > 0) ((amt / totalExpense) * 100).toInt() else 0

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(catColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconByName(catIcon),
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = catName,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Text(
                                        text = "$currencySymbol${dfCompact.format(amt)} ($percentage%)",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. TOP MERCHANTS & PAYEES BENTO CARD
        AnalyticsBentoCard(
            title = "TOP MERCHANTS & PAYEES",
            icon = Icons.Outlined.Storefront
        ) {
            if (topMerchants.isEmpty()) {
                Text(
                    text = "No merchant expense records found.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topMerchants.forEachIndexed { index, (merchantName, pair) ->
                        val (spent, count) = pair
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = merchantName,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$count transactions",
                                            fontFamily = PlusJakartaSansFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "$currencySymbol${df.format(spent)}",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. SPENDING TREND LINE CHART
        AnalyticsBentoCard(
            title = "SPENDING TREND (LINE & AREA)",
            icon = Icons.Outlined.ShowChart
        ) {
            val dailyExpenses = remember(filteredTransactions) {
                val map = mutableMapOf<Int, Double>()
                val cal = Calendar.getInstance()
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    map[day] = (map[day] ?: 0.0) + tx.amount
                }
                map
            }

            val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val pointsList = remember(dailyExpenses, daysInMonth) {
                (1..daysInMonth).map { day ->
                    Pair(day, dailyExpenses[day] ?: 0.0)
                }
            }

            AnalyticsLineChart(
                points = pointsList,
                currencySymbol = currencySymbol,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        // 7. DAILY EXPENSE GRADIENT BAR CHART
        AnalyticsBentoCard(
            title = "DAILY EXPENSE GRADIENT BARS",
            icon = Icons.Outlined.BarChart
        ) {
            val dailyExpenses = remember(filteredTransactions) {
                val map = mutableMapOf<Int, Double>()
                val cal = Calendar.getInstance()
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    map[day] = (map[day] ?: 0.0) + tx.amount
                }
                map
            }

            val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val barList = remember(dailyExpenses, daysInMonth) {
                (1..daysInMonth).map { day ->
                    Pair(day, dailyExpenses[day] ?: 0.0)
                }
            }

            AnalyticsBarChart(
                bars = barList,
                currencySymbol = currencySymbol,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
        }

        // 8. INTERACTIVE MONTHLY CALENDAR SPENDING HEATMAP
        AnalyticsBentoCard(
            title = "MONTHLY SPENDING HEATMAP",
            icon = Icons.Outlined.CalendarMonth
        ) {
            val heatmapCal = remember(calendarMonthOffset) {
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, calendarMonthOffset)
                }
            }

            val monthName = remember(heatmapCal) {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(heatmapCal.time)
            }

            val daysInMonth = heatmapCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentActualDay = if (calendarMonthOffset == 0) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else -1

            val dailyExpensesMap = remember(filteredTransactions, calendarMonthOffset) {
                val map = mutableMapOf<Int, Double>()
                val c = Calendar.getInstance()
                val targetYear = heatmapCal.get(Calendar.YEAR)
                val targetMonth = heatmapCal.get(Calendar.MONTH)

                filteredTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
                    c.timeInMillis = tx.dateEpochMillis
                    if (c.get(Calendar.YEAR) == targetYear && c.get(Calendar.MONTH) == targetMonth) {
                        val day = c.get(Calendar.DAY_OF_MONTH)
                        map[day] = (map[day] ?: 0.0) + tx.amount
                    }
                }
                map
            }

            val maxExpenseDay = (dailyExpensesMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Month Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { calendarMonthOffset -= 1 }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Prev Month", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text(
                        text = monthName,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { calendarMonthOffset += 1 }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next Month", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Day Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Days Grid
                val rows = (daysInMonth + 6) / 7
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (c in 0..6) {
                            val dayNumber = r * 7 + c + 1
                            if (dayNumber <= daysInMonth) {
                                val amt = dailyExpensesMap[dayNumber] ?: 0.0
                                val ratio = (amt / maxExpenseDay).toFloat()
                                val cellBg = when {
                                    amt <= 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    ratio < 0.3f -> Color(0xFF0284C7).copy(alpha = 0.35f)
                                    ratio < 0.7f -> Color(0xFF38BDF8).copy(alpha = 0.65f)
                                    else -> Color(0xFF38BDF8)
                                }

                                val isSelected = selectedHeatmapDay == dayNumber

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(cellBg)
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else if (dayNumber == currentActualDay) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedHeatmapDay = if (selectedHeatmapDay == dayNumber) null else dayNumber
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (amt > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (amt > 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Heatmap Tooltip Detail Box
                selectedHeatmapDay?.let { day ->
                    val daySpend = dailyExpensesMap[day] ?: 0.0
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Day $day Spending:",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currencySymbol${df.format(daySpend)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 9. ACCOUNTS DISTRIBUTION BENTO CARD
        AnalyticsBentoCard(
            title = "ACCOUNT DISTRIBUTION",
            icon = Icons.Outlined.AccountBalance
        ) {
            if (accounts.isEmpty()) {
                Text(
                    text = "No financial accounts created yet.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { acct ->
                        val acctIncome = transactions.filter { it.accountId == acct.id && it.type == TransactionType.INCOME }.sumOf { it.amount }
                        val acctExpense = transactions.filter { it.accountId == acct.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        val acctOut = transactions.filter { it.accountId == acct.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
                        val acctIn = transactions.filter { it.targetAccountId == acct.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
                        val currBal = acct.initialBalance + acctIncome - acctExpense - acctOut + acctIn
                        val acctTxnCount = transactions.count { it.accountId == acct.id || it.targetAccountId == acct.id }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(acct.iconName),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = acct.name,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$acctTxnCount transactions",
                                            fontFamily = PlusJakartaSansFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "$currencySymbol${df.format(currBal)}",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 10. PAYMENT METHOD COMPARISON BENTO CARD
        AnalyticsBentoCard(
            title = "PAYMENT METHOD COMPARISON",
            icon = Icons.Outlined.CreditCard
        ) {
            if (paymentModeGroup.isEmpty()) {
                Text(
                    text = "No payment method data recorded.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    paymentModeGroup.forEach { (mode, pair) ->
                        val modeSpent = pair.first
                        val modeCount = pair.second
                        val percentage = if (totalExpense > 0) (modeSpent / totalExpense).toFloat().coerceIn(0f, 1f) else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "($modeCount txns)",
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$currencySymbol${dfCompact.format(modeSpent)} (${(percentage * 100).toInt()}%)",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            LinearProgressIndicator(
                                progress = percentage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 11. ADVANCED EXCEL EXPORT BENTO CARD
        val exportContext = LocalContext.current
        AnalyticsBentoCard(
            title = "ADVANCED EXCEL REPORT EXPORT",
            icon = Icons.Outlined.FileDownload
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Generate a multi-worksheet Excel workbook (.xlsx / .xlsm) with Executive Dashboard KPIs, Transactions Master, SUMIF/XLOOKUP formulas, Pivot tables, and VBA Macro helpers.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        val file = com.vesper.ledger.util.AdvancedExcelExporter.exportToExcel(
                            context = exportContext,
                            transactions = filteredTransactions,
                            categories = categories,
                            accounts = accounts,
                            currencySymbol = currencySymbol
                        )
                        if (file != null) {
                            Toast.makeText(exportContext, "Advanced Excel Report Generated! Opening...", Toast.LENGTH_SHORT).show()
                            com.vesper.ledger.util.AdvancedExcelExporter.shareExcelFile(exportContext, file)
                        } else {
                            Toast.makeText(exportContext, "Failed to generate Excel file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Export Advanced Excel Workbook (.xlsx)",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ────────────────────────────────────────────────────────────────────────────
// ANALYTICS LINE CHART COMPONENT
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsLineChart(
    points: List<Pair<Int, Double>>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val dfCompact = remember { DecimalFormat("#,##0") }
    val maxVal = remember(points) { (points.maxOfOrNull { it.second } ?: 1000.0).coerceAtLeast(100.0) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val stepX = width / (points.size - 1).coerceAtLeast(1)
                        val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                        selectedPointIndex = if (selectedPointIndex == tappedIndex) null else tappedIndex
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val stepX = width / (points.size - 1).coerceAtLeast(1)

            // Horizontal Grid Lines
            val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = height * (i.toFloat() / gridLines)
                drawLine(
                    color = Color(0xFF27272A),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = gridPathEffect
                )
            }

            if (points.isEmpty()) return@Canvas

            // Build smooth Cubic Bezier Path
            val strokePath = Path()
            val fillPath = Path()

            val coordinates = points.mapIndexed { index, pair ->
                val x = index * stepX
                val y = height - ((pair.second / maxVal) * (height - 20.dp.toPx())).toFloat()
                Offset(x, y)
            }

            strokePath.moveTo(coordinates.first().x, coordinates.first().y)
            fillPath.moveTo(coordinates.first().x, height)
            fillPath.lineTo(coordinates.first().x, coordinates.first().y)

            for (i in 0 until coordinates.size - 1) {
                val p1 = coordinates[i]
                val p2 = coordinates[i + 1]
                val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)

                strokePath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
            }

            fillPath.lineTo(coordinates.last().x, height)
            fillPath.close()

            // Area Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                        Color(0xFF38BDF8).copy(alpha = 0.02f)
                    )
                )
            )

            // Curved Line Stroke
            drawPath(
                path = strokePath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Glowing Dots
            coordinates.forEachIndexed { index, point ->
                val (_, amt) = points[index]
                val isSelected = selectedPointIndex == index
                if (amt > 0 || isSelected) {
                    drawCircle(
                        color = if (isSelected) Color.White else Color(0xFF38BDF8),
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // Interactive Touch Tooltip
        selectedPointIndex?.let { index ->
            if (index in points.indices) {
                val (day, amt) = points[index]
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Day $day: $currencySymbol${dfCompact.format(amt)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// ANALYTICS BAR CHART COMPONENT
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsBarChart(
    bars: List<Pair<Int, Double>>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
    val dfCompact = remember { DecimalFormat("#,##0") }
    val maxVal = remember(bars) { (bars.maxOfOrNull { it.second } ?: 1000.0).coerceAtLeast(100.0) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(bars) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val barCount = bars.size.coerceAtLeast(1)
                        val stepX = width / barCount
                        val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, bars.size - 1)
                        selectedBarIndex = if (selectedBarIndex == tappedIndex) null else tappedIndex
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val barCount = bars.size.coerceAtLeast(1)
            val spacing = 2.5.dp.toPx()
            val barWidth = ((width - (spacing * (barCount - 1))) / barCount).coerceAtLeast(2.dp.toPx())

            // Dashed Grid Lines
            val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = Color(0xFF27272A),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = gridPathEffect
                )
            }

            bars.forEachIndexed { index, (day, amount) ->
                val ratio = (amount / maxVal).toFloat().coerceIn(0f, 1f)
                val barHeight = (height * ratio).coerceAtLeast(if (amount > 0) 4.dp.toPx() else 1.5.dp.toPx())
                val x = index * (barWidth + spacing)
                val y = height - barHeight

                val isSelected = selectedBarIndex == index
                val barGradient = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(Color.White, Color(0xFF38BDF8))
                    } else if (amount >= maxVal * 0.85) {
                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                    } else {
                        listOf(Color(0xFF0284C7).copy(alpha = 0.8f), Color(0xFF0284C7).copy(alpha = 0.3f))
                    }
                )

                drawRoundRect(
                    brush = barGradient,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        selectedBarIndex?.let { index ->
            if (index in bars.indices) {
                val (day, amt) = bars[index]
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "Day $day: $currencySymbol${dfCompact.format(amt)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// BENTO GRID HELPER COMPONENTS
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun AnalyticsBentoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    ShCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun AnalyticsKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ShCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = value,
                fontFamily = SpaceGroteskFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
