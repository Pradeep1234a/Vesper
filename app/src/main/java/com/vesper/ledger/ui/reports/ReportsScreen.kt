package com.vesper.ledger.ui.reports

import android.graphics.Color as AndroidColor
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.getIconByName
import java.text.DecimalFormat
import androidx.compose.foundation.interaction.MutableInteractionSource
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    currencySymbol: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val df = DecimalFormat("#,##0")

    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surface
    val bgColor = MaterialTheme.colorScheme.background

    var showPeriodSheet by remember { mutableStateOf(false) }
    var showCustomRangePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Standard RootHeader with Hamburger and Help Actions
        RootHeader(
            title = "Analytics",
            actions = {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Help",
                    tint = secTextColor,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Help click */ }
                )
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 24.dp), // Unified 24dp horizontal margin
            verticalArrangement = Arrangement.spacedBy(24.dp), // 24dp spacing between sections
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
        ) {
            // ── Section 1: Period Selector ──
            item {
                PeriodSelectorCard(
                    selectedPeriod = selectedPeriod,
                    onClick = { showPeriodSheet = true }
                )
            }

            // ── Section 2: Overview Metrics (2x2 Grid) ──
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Overview",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBgColor
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp) // 16dp spacing between cards
                    ) {
                        AnalyticsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Spending",
                            value = "$currencySymbol${df.format(uiState.totalSpending)}",
                            subtitle = if (uiState.spendingChangePercent >= 0) {
                                "↑ ${df.format(uiState.spendingChangePercent.toDouble())}% vs last period"
                            } else {
                                "↓ ${df.format((-uiState.spendingChangePercent).toDouble())}% vs last period"
                            },
                            subtitleColor = if (uiState.spendingChangePercent >= 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                        )
                        AnalyticsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Transaction Count",
                            value = "${uiState.transactionCount}",
                            subtitle = if (uiState.transactionChangePercent >= 0) {
                                "↑ ${df.format(uiState.transactionChangePercent.toDouble())}% vs last period"
                            } else {
                                "↓ ${df.format((-uiState.transactionChangePercent).toDouble())}% vs last period"
                            },
                            subtitleColor = if (uiState.transactionChangePercent >= 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnalyticsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Average Daily Spend",
                            value = "$currencySymbol${df.format(uiState.averageDailySpend)}",
                            subtitle = if (uiState.avgChangePercent >= 0) {
                                "↑ ${df.format(uiState.avgChangePercent.toDouble())}% vs last period"
                            } else {
                                "↓ ${df.format((-uiState.avgChangePercent).toDouble())}% vs last period"
                            },
                            subtitleColor = if (uiState.avgChangePercent >= 0) Color(0xFFEF4444) else Color(0xFF22C55E)
                        )
                        AnalyticsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Largest Expense",
                            value = "$currencySymbol${df.format(uiState.largestExpenseAmount)}",
                            subtitle = if (uiState.largestExpenseName.isNotEmpty()) {
                                "${uiState.largestExpenseName} • ${uiState.largestExpenseDate}"
                            } else {
                                "No transactions yet"
                            },
                            subtitleColor = secTextColor
                        )
                    }
                }
            }

            // ── Section 3: Spending Trend Bezier Chart ──
            item {
                SpendingTrendSection(
                    trendPoints = uiState.trendPoints,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor,
                    outlineColor = outlineColor,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            // ── Section 4: Spending Calendar Heatmap ──
            item {
                SpendingCalendarSection(
                    heatmapDays = uiState.heatmapDays,
                    accentColor = MaterialTheme.colorScheme.primary,
                    outlineColor = outlineColor,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor
                )
            }

            // ── Section 5: Category Breakdown Bento Grid ──
            item {
                CategoryBreakdownSection(
                    reports = uiState.categoryReports,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor
                )
            }

            // ── Section 6: Monthly Comparison ──
            item {
                MonthlyComparisonSection(
                    uiState = uiState,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor,
                    df = df
                )
            }

            // ── Section 7: Top Merchants ──
            item {
                TopMerchantsSection(
                    merchants = uiState.topMerchants,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor
                )
            }

            // ── Section 8: Financial Distribution (Optional) ──
            val showDistribution = uiState.totalIncome > uiState.totalSpending
            if (showDistribution) {
                item {
                    FinancialDistributionSection(
                        totalSpending = uiState.totalSpending,
                        totalIncome = uiState.totalIncome,
                        currencySymbol = currencySymbol,
                        onBgColor = onBgColor,
                        secTextColor = secTextColor
                    )
                }
            }
        }
    }

    // Predefined Period Selection Modal Bottom Sheet
    if (showPeriodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPeriodSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = surfaceColor,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Analytics Period",
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBgColor,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AnalyticsPeriod.values().toList()) { period ->
                        val isSelected = period == selectedPeriod
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    if (period == AnalyticsPeriod.CUSTOM) {
                                        showPeriodSheet = false
                                        showCustomRangePicker = true
                                    } else {
                                        viewModel.setPeriod(period)
                                        showPeriodSheet = false
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else onBgColor
                                )
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Date Range Picker Dialog
    if (showCustomRangePicker) {
        val rangeState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showCustomRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = rangeState.selectedStartDateMillis
                        val end = rangeState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.setCustomRange(start, end)
                        }
                        showCustomRangePicker = false
                    }
                ) {
                    Text("Apply Range", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRangePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = rangeState,
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ────────────────────────────────────
// CUSTOM ANALYTICS CARD
// ────────────────────────────────────
@Composable
fun AnalyticsCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
            .then(clickableModifier),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            content()
        }
    }
}

// ────────────────────────────────────
// PERIOD SELECTOR CARD
// ────────────────────────────────────
@Composable
fun PeriodSelectorCard(
    selectedPeriod: AnalyticsPeriod,
    onClick: () -> Unit
) {
    AnalyticsCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analytics Period",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedPeriod.label,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select Period",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ────────────────────────────────────
// ANALYTICS GRID CARD
// ────────────────────────────────────
@Composable
fun AnalyticsGridCard(
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color,
    modifier: Modifier = Modifier
) {
    AnalyticsCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = subtitleColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ────────────────────────────────────
// SPENDING TREND SECTION (SMOOTH BEZIER)
// ────────────────────────────────────
@Composable
fun SpendingTrendSection(
    trendPoints: List<TrendPoint>,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color,
    outlineColor: Color,
    accentColor: Color
) {
    val df = DecimalFormat("#,##0")
    val density = LocalDensity.current
    var activePointIndex by remember { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Spending Trend",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
            if (trendPoints.isEmpty() || trendPoints.all { it.amount == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No trend data for this period.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = secTextColor.copy(alpha = 0.6f))
                    )
                }
            } else {
                val maxVal = trendPoints.maxOf { it.amount }.coerceAtLeast(1.0)
                val chartLeftPad = 48f
                val chartTopPad = 12f
                val chartBottomPad = 28f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // Precompute points for line curve and gesture logic
                    var pointsList by remember { mutableStateOf<List<Offset>>(emptyList()) }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(trendPoints) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        activePointIndex = findClosestPoint(offset.x, pointsList)
                                    },
                                    onDrag = { change, _ ->
                                        activePointIndex = findClosestPoint(change.position.x, pointsList)
                                    },
                                    onDragEnd = { activePointIndex = null },
                                    onDragCancel = { activePointIndex = null }
                                )
                            }
                    ) {
                        val chartWidth = size.width - chartLeftPad
                        val chartHeight = size.height - chartTopPad - chartBottomPad
                        
                        val points = trendPoints.mapIndexed { i, pt ->
                            val x = chartLeftPad + (i.toFloat() / (trendPoints.size - 1).coerceAtLeast(1)) * chartWidth
                            val y = chartTopPad + chartHeight * (1f - (pt.amount / maxVal).toFloat())
                            Offset(x, y)
                        }
                        pointsList = points

                        // Grid lines
                        val ySteps = 4
                        for (i in 0..ySteps) {
                            val y = chartTopPad + (chartHeight * i / ySteps)
                            val labelVal = maxVal * (ySteps - i) / ySteps
                            drawLine(
                                color = outlineColor.copy(alpha = 0.2f),
                                start = Offset(chartLeftPad, y),
                                end = Offset(size.width, y),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                "$currencySymbol${df.format(labelVal.toLong())}",
                                2f,
                                y + 4f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 22f
                                    isAntiAlias = true
                                }
                            )
                        }

                        // X-axis labels
                        val labelStep = (trendPoints.size / 5).coerceAtLeast(1)
                        for (i in trendPoints.indices step labelStep) {
                            val x = chartLeftPad + (i.toFloat() / (trendPoints.size - 1).coerceAtLeast(1)) * chartWidth
                            drawContext.canvas.nativeCanvas.drawText(
                                trendPoints[i].label,
                                x - 20f,
                                size.height - 2f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    textSize = 20f
                                    isAntiAlias = true
                                }
                            )
                        }

                        // Smooth Bezier Curve Path
                        if (points.size >= 2) {
                            val bezierPath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    // Control points logic for smooth curve
                                    val cp1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                                    val cp2 = Offset(curr.x - (curr.x - prev.x) / 2f, curr.y)
                                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                                }
                            }

                            // Area gradient fill
                            val fillPath = Path().apply {
                                addPath(bezierPath)
                                lineTo(points.last().x, chartTopPad + chartHeight)
                                lineTo(points.first().x, chartTopPad + chartHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.25f), accentColor.copy(alpha = 0f)),
                                    startY = chartTopPad,
                                    endY = chartTopPad + chartHeight
                                )
                            )

                            // Outline stroke
                            drawPath(
                                path = bezierPath,
                                color = accentColor,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Highlight selected point on gesture drag/tap
                        activePointIndex?.let { index ->
                            val pt = points.getOrNull(index)
                            if (pt != null) {
                                drawLine(
                                    color = onBgColor.copy(alpha = 0.3f),
                                    start = Offset(pt.x, chartTopPad),
                                    end = Offset(pt.x, chartTopPad + chartHeight),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                                )
                                drawCircle(
                                    color = accentColor,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = onBgColor,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    // Render floating Tooltip above highlighted point
                    activePointIndex?.let { index ->
                        val pt = pointsList.getOrNull(index)
                        val dataPt = trendPoints.getOrNull(index)
                        if (pt != null && dataPt != null) {
                            val tooltipX = with(density) { pt.x.toDp() }
                            val tooltipY = with(density) { pt.y.toDp() }

                            Box(
                                modifier = Modifier
                                    .offset(x = tooltipX - 50.dp, y = (tooltipY - 54.dp).coerceAtLeast(0.dp))
                                    .background(onBgColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, outlineColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dataPt.label,
                                        color = MaterialTheme.colorScheme.background,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "$currencySymbol${df.format(dataPt.amount)}",
                                        color = MaterialTheme.colorScheme.background,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGroteskFamily
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

private fun findClosestPoint(x: Float, points: List<Offset>): Int? {
    if (points.isEmpty()) return null
    return points.indices.minByOrNull { kotlin.math.abs(points[it].x - x) }
}

// ────────────────────────────────────
// SPENDING CALENDAR Heatmap (MONTH GRID)
// ────────────────────────────────────
@Composable
fun SpendingCalendarSection(
    heatmapDays: List<HeatmapDay>,
    accentColor: Color,
    outlineColor: Color,
    onBgColor: Color,
    secTextColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Spending Calendar",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Activity overview",
                style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val maxWeek = heatmapDays.maxOfOrNull { it.weekIndex } ?: 0

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header row of days Mon..Sun
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(30.dp))
                    dayLabels.forEach { label ->
                        Text(
                            text = label.take(1),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = secTextColor,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid rows for weeks
                for (w in 0..maxWeek) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "W${w + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(color = secTextColor, fontSize = 9.sp),
                            modifier = Modifier.width(30.dp)
                        )

                        for (d in 0..6) {
                            val day = heatmapDays.firstOrNull { it.dayOfWeek == d && it.weekIndex == w }
                            val isSpending = day != null && day.intensity > 0f
                            val color = if (isSpending) {
                                accentColor.copy(alpha = (day!!.intensity * 0.8f + 0.2f).coerceIn(0.2f, 1f))
                            } else {
                                outlineColor.copy(alpha = 0.15f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSpending) Color.Transparent else outlineColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────
// CATEGORY BREAKDOWN BENTO GRID
// ────────────────────────────────────
@Composable
fun CategoryBreakdownSection(
    reports: List<CategoryReport>,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color
) {
    val df = DecimalFormat("#,##0")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Category Breakdown",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        if (reports.isEmpty()) {
            AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No category data to display.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = secTextColor)
                )
            }
        } else {
            // Weighted Bento Grid Layout (displays top 5 categories)
            val topCategory = reports.getOrNull(0)
            val medium1 = reports.getOrNull(1)
            val medium2 = reports.getOrNull(2)
            val small1 = reports.getOrNull(3)
            val small2 = reports.getOrNull(4)

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Large Card (Top Category) - Full Width
                topCategory?.let { rep ->
                    BentoCategoryCard(
                        report = rep,
                        currencySymbol = currencySymbol,
                        df = df,
                        height = 110.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Medium Cards Row - 2 Columns (equal weight)
                if (medium1 != null || medium2 != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        medium1?.let { rep ->
                            BentoCategoryCard(
                                report = rep,
                                currencySymbol = currencySymbol,
                                df = df,
                                height = 96.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        medium2?.let { rep ->
                            BentoCategoryCard(
                                report = rep,
                                currencySymbol = currencySymbol,
                                df = df,
                                height = 96.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Small Cards Row - 2 Columns (equal weight)
                if (small1 != null || small2 != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        small1?.let { rep ->
                            BentoCategoryCard(
                                report = rep,
                                currencySymbol = currencySymbol,
                                df = df,
                                height = 80.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        small2?.let { rep ->
                            BentoCategoryCard(
                                report = rep,
                                currencySymbol = currencySymbol,
                                df = df,
                                height = 80.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BentoCategoryCard(
    report: CategoryReport,
    currencySymbol: String,
    df: DecimalFormat,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val catColor = try {
        Color(AndroidColor.parseColor(report.category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .height(height)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(catColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconByName(report.category.iconName),
                            contentDescription = report.category.name,
                            tint = catColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = report.category.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "${(report.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "$currencySymbol${df.format(report.totalAmount)}",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                // Colorized indicator progress bar
                LinearProgressIndicator(
                    progress = report.percentage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = catColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            }
        }
    }
}

// ────────────────────────────────────
// MONTHLY COMPARISON SECTION
// ────────────────────────────────────
@Composable
fun MonthlyComparisonSection(
    uiState: ReportsUiState,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color,
    df: DecimalFormat
) {
    val diff = uiState.totalSpending - uiState.previousPeriodSpending
    val isIncrease = diff >= 0
    val diffSign = if (isIncrease) "+" else "-"

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Monthly Comparison",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Spending comparison against last period",
                style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Current Period",
                        style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
                    )
                    Text(
                        text = "$currencySymbol${df.format(uiState.totalSpending)}",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = onBgColor
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Previous Period",
                        style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
                    )
                    Text(
                        text = "$currencySymbol${df.format(uiState.previousPeriodSpending)}",
                        style = TextStyle(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = onBgColor.copy(alpha = 0.8f)
                        )
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Difference",
                        style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isIncrease) Color(0xFFEF4444) else Color(0xFF22C55E),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$diffSign$currencySymbol${df.format(kotlin.math.abs(diff))}",
                            style = TextStyle(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isIncrease) Color(0xFFEF4444) else Color(0xFF22C55E)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────
// TOP MERCHANTS SECTION
// ────────────────────────────────────
@Composable
fun TopMerchantsSection(
    merchants: List<MerchantEntry>,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color
) {
    val df = DecimalFormat("#,##0")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Top Merchants",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        if (merchants.isEmpty()) {
            AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No merchant transactions recorded in this period.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = secTextColor)
                )
            }
        } else {
            AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    merchants.forEach { merchant ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circular merchant initial badge
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = merchant.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = onBgColor
                                    )
                                )
                            }

                            // Merchant details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = merchant.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = onBgColor
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${merchant.transactionCount} transactions",
                                    style = MaterialTheme.typography.labelSmall.copy(color = secTextColor)
                                )
                            }

                            // Total spent
                            Text(
                                text = "$currencySymbol${df.format(merchant.totalAmount)}",
                                style = TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = onBgColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────
// FINANCIAL DISTRIBUTION (donut ring)
// ────────────────────────────────────
@Composable
fun FinancialDistributionSection(
    totalSpending: Double,
    totalIncome: Double,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color
) {
    val df = DecimalFormat("#,##0")
    
    // Distribute remaining income to savings and investments
    val expenses = totalSpending
    val remaining = (totalIncome - totalSpending).coerceAtLeast(0.0)
    val savings = remaining * 0.7
    val investments = remaining * 0.3
    val total = expenses + savings + investments

    val expensePct = if (total > 0) (expenses / total).toFloat() else 1f
    val savingsPct = if (total > 0) (savings / total).toFloat() else 0f
    val investmentPct = if (total > 0) (investments / total).toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Financial Distribution",
            fontFamily = FontFamily.Serif,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )

        AnalyticsCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        val ringSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val offset = Offset(strokeWidth / 2, strokeWidth / 2)

                        // 1. Draw Expenses Arc
                        val expenseAngle = expensePct * 360f
                        drawArc(
                            color = Color(0xFFEF4444),
                            startAngle = -90f,
                            sweepAngle = expenseAngle,
                            useCenter = false,
                            topLeft = offset,
                            size = ringSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Draw Savings Arc
                        val savingsAngle = savingsPct * 360f
                        if (savingsPct > 0) {
                            drawArc(
                                color = Color(0xFF22C55E),
                                startAngle = -90f + expenseAngle,
                                sweepAngle = savingsAngle,
                                useCenter = false,
                                topLeft = offset,
                                size = ringSize,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // 3. Draw Investments Arc
                        val investmentAngle = investmentPct * 360f
                        if (investmentPct > 0) {
                            drawArc(
                                color = Color(0xFF3B82F6),
                                startAngle = -90f + expenseAngle + savingsAngle,
                                sweepAngle = investmentAngle,
                                useCenter = false,
                                topLeft = offset,
                                size = ringSize,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                    
                    // Inside circle label
                    Text(
                        text = "Asset Mix",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = secTextColor
                        )
                    )
                }

                // Legend layout
                Column(
                    modifier = Modifier.padding(start = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DistributionLegendRow(
                        color = Color(0xFFEF4444),
                        label = "Expenses",
                        percentage = "${(expensePct * 100).toInt()}%",
                        amount = "$currencySymbol${df.format(expenses)}",
                        onBgColor = onBgColor,
                        secTextColor = secTextColor
                    )
                    DistributionLegendRow(
                        color = Color(0xFF22C55E),
                        label = "Savings",
                        percentage = "${(savingsPct * 100).toInt()}%",
                        amount = "$currencySymbol${df.format(savings)}",
                        onBgColor = onBgColor,
                        secTextColor = secTextColor
                    )
                    DistributionLegendRow(
                        color = Color(0xFF3B82F6),
                        label = "Investments",
                        percentage = "${(investmentPct * 100).toInt()}%",
                        amount = "$currencySymbol${df.format(investments)}",
                        onBgColor = onBgColor,
                        secTextColor = secTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun DistributionLegendRow(
    color: Color,
    label: String,
    percentage: String,
    amount: String,
    onBgColor: Color,
    secTextColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$label ($percentage)",
            style = MaterialTheme.typography.labelSmall.copy(color = secTextColor),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = amount,
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = onBgColor
            )
        )
    }
}
