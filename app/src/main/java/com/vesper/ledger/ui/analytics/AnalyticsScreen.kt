package com.vesper.ledger.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    var selectedFilter by remember { mutableStateOf("THIS_MONTH") } // THIS_MONTH, LAST_30_DAYS, THIS_WEEK, ALL
    val df = DecimalFormat("#,##0.00")
    val dfCompact = DecimalFormat("#,##0")

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
            else -> transactions
        }
    }

    // Key financial metrics
    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netFlow = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).coerceIn(0.0, 100.0) else 0.0

    // Days count calculation for daily average
    val daysInPeriod = remember(selectedFilter) {
        when (selectedFilter) {
            "THIS_WEEK" -> 7
            "THIS_MONTH" -> Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            "LAST_30_DAYS" -> 30
            else -> 30
        }
    }
    val dailyAvgExpense = if (daysInPeriod > 0) totalExpense / daysInPeriod else 0.0

    // Category breakdown
    val categoryGroup = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Payment method breakdown
    val paymentModeGroup = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.paymentMethod.ifBlank { "Cash" } }
            .mapValues { entry ->
                val sum = entry.value.sumOf { it.amount }
                val count = entry.value.size
                Pair(sum, count)
            }
            .toList()
            .sortedByDescending { it.second.first }
    }

    // Top merchants / single expenses
    val topMerchants = remember(filteredTransactions) {
        filteredTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sortedByDescending { it.amount }
            .take(5)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. TIME PERIOD FILTER SELECTOR BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF18181B))
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "THIS_MONTH" to "This Month",
                "LAST_30_DAYS" to "30 Days",
                "THIS_WEEK" to "This Week",
                "ALL" to "All Time"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color.Transparent)
                        .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color.Transparent, RoundedCornerShape(4.dp))
                        .clickable { selectedFilter = key },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                    )
                }
            }
        }

        // 2. BENTO GRID: 4 KEY FINANCIAL KPI CARDS (2x2 GRID)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Expenses Card
                BentoKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "EXPENSES",
                    value = "$currencySymbol${dfCompact.format(totalExpense)}",
                    badgeText = "Total Spent",
                    badgeColor = Color(0xFFEF4444),
                    icon = Icons.Outlined.TrendingDown
                )

                // Total Income Card
                BentoKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "INCOME",
                    value = "$currencySymbol${dfCompact.format(totalIncome)}",
                    badgeText = "Total Inflow",
                    badgeColor = Color(0xFF22C55E),
                    icon = Icons.Outlined.TrendingUp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Savings Rate Card
                BentoKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "SAVINGS RATE",
                    value = "${String.format(Locale.getDefault(), "%.1f", savingsRate)}%",
                    badgeText = if (netFlow >= 0) "Net +$currencySymbol${dfCompact.format(netFlow)}" else "Net -$currencySymbol${dfCompact.format(-netFlow)}",
                    badgeColor = if (netFlow >= 0) Color(0xFF38BDF8) else Color(0xFFEF4444),
                    icon = Icons.Outlined.Savings
                )

                // Daily Average Card
                BentoKpiCard(
                    modifier = Modifier.weight(1f),
                    title = "DAILY AVERAGE",
                    value = "$currencySymbol${dfCompact.format(dailyAvgExpense)}",
                    badgeText = "Per Day ($daysInPeriod Days)",
                    badgeColor = Color(0xFFA1A1AA),
                    icon = Icons.Outlined.Schedule
                )
            }
        }

        // 3. FL_CHART STYLE SMOOTH CURVED LINE & GRADIENT AREA CHART
        BentoCard(title = "ANALYTICAL SPENDING TREND (FL_CHART)", icon = Icons.Outlined.ShowChart) {
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

            FLCurvedLineChart(
                points = pointsList,
                currencySymbol = currencySymbol,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }

        // 4. BENTO GRID: CATEGORY DONUT & BREAKDOWN
        BentoCard(title = "CATEGORY BREAKDOWN", icon = Icons.Outlined.PieChart) {
            if (categoryGroup.isEmpty()) {
                Text(
                    text = "No category expense data recorded for this period.",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Donut Chart Canvas
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
                                color = Color(0xFFA1A1AA)
                            )
                            Text(
                                text = "$currencySymbol${dfCompact.format(totalExpense)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Right Column: Top Categories List
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(catColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(catIcon),
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = catName,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "$currencySymbol${dfCompact.format(amt)} ($percentage%)",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. BENTO GRID: MONTHLY CALENDAR SPENDING HEATMAP
        BentoCard(title = "MONTHLY SPENDING HEATMAP", icon = Icons.Outlined.CalendarMonth) {
            val cal = Calendar.getInstance()
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            val dailyExpensesMap = remember(filteredTransactions) {
                val map = mutableMapOf<Int, Double>()
                val c = Calendar.getInstance()
                filteredTransactions.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
                    c.timeInMillis = tx.dateEpochMillis
                    val day = c.get(Calendar.DAY_OF_MONTH)
                    map[day] = (map[day] ?: 0.0) + tx.amount
                }
                map
            }

            val maxExpenseDay = (dailyExpensesMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Calendar Weekday Headers
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
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }

                // Grid Cells (7 columns)
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
                                    amt <= 0 -> Color(0xFF242429)
                                    ratio < 0.3f -> Color(0xFF0284C7).copy(alpha = 0.35f)
                                    ratio < 0.7f -> Color(0xFF38BDF8).copy(alpha = 0.65f)
                                    else -> Color(0xFF38BDF8)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(cellBg)
                                        .border(
                                            1.dp,
                                            if (dayNumber == currentDay) Color.White else Color(0xFF3F3F46),
                                            RoundedCornerShape(4.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (amt > 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (amt > 0) Color.White else Color(0xFFA1A1AA)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 6. BENTO GRID: ACCOUNTS BREAKDOWN & RANKINGS
        BentoCard(title = "ACCOUNT DISTRIBUTION & BREAKDOWN", icon = Icons.Outlined.AccountBalance) {
            if (accounts.isEmpty()) {
                Text(
                    text = "No financial accounts created yet.",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
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

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF242429))
                                .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
                                .padding(10.dp),
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
                                        .background(Color(0xFF18181B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(acct.iconName),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = acct.name,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "$acctTxnCount transactions",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                            }

                            Text(
                                text = "$currencySymbol${df.format(currBal)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 7. BENTO GRID: PAYMENT METHOD BREAKDOWN
        BentoCard(title = "PAYMENT METHOD COMPARISON", icon = Icons.Outlined.CreditCard) {
            if (paymentModeGroup.isEmpty()) {
                Text(
                    text = "No payment method data recorded.",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
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
                                        color = Color.White
                                    )
                                    Text(
                                        text = "($modeCount txns)",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                                Text(
                                    text = "$currencySymbol${dfCompact.format(modeSpent)} (${(percentage * 100).toInt()}%)",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            LinearProgressIndicator(
                                progress = percentage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF242429)
                            )
                        }
                    }
                }
            }
        }

        // 8. BENTO GRID: TOP MERCHANT RANKINGS
        BentoCard(title = "TOP EXPENSE RANKINGS", icon = Icons.Outlined.Leaderboard) {
            if (topMerchants.isEmpty()) {
                Text(
                    text = "No expenses recorded for this period.",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topMerchants.forEachIndexed { index, tx ->
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
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF242429))
                                        .border(1.dp, Color(0xFF3F3F46), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = tx.title,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${tx.accountName} • ${tx.paymentMethod}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                }
                            }

                            Text(
                                text = "-$currencySymbol${df.format(tx.amount)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ────────────────────────────────────────────────────────────────────────────
// FL_CHART AESTHETIC INTERACTIVE SMOOTH CURVED LINE CHART
// ────────────────────────────────────────────────────────────────────────────
@Composable
fun FLCurvedLineChart(
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

            // 1. Draw dashed grid lines (FL_Chart GridData)
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

            // 2. Build smooth Cubic Bezier Path
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

            // Draw Area Gradient Fill (FL_Chart BarAreaData)
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.35f),
                        Color(0xFF38BDF8).copy(alpha = 0.02f)
                    )
                )
            )

            // Draw Curved Line Stroke (FL_Chart LineChartBarData)
            drawPath(
                path = strokePath,
                color = Color(0xFF38BDF8),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Draw Active Glowing Dot Markers on Key Points
            coordinates.forEachIndexed { index, point ->
                val (_, amt) = points[index]
                val isSelected = selectedPointIndex == index
                if (amt > 0 || isSelected) {
                    drawCircle(
                        color = if (isSelected) Color.White else Color(0xFF38BDF8),
                        radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                        radius = if (isSelected) 10.dp.toPx() else 6.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // 4. Interactive Touch Tooltip Badge (FL_Chart TouchCallback Tooltip)
        selectedPointIndex?.let { index ->
            if (index in points.indices) {
                val (day, amt) = points[index]
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF242429))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Day $day: $currencySymbol${dfCompact.format(amt)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
private fun BentoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF18181B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(14.dp)
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
                    color = Color(0xFFA1A1AA)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFA1A1AA),
                    modifier = Modifier.size(16.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun BentoKpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF18181B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(12.dp)
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
                    color = Color(0xFFA1A1AA)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = value,
                fontFamily = SpaceGroteskFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}
