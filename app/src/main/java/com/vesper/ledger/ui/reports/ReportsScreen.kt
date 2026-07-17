package com.vesper.ledger.ui.reports

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.RootHeader
import java.text.DecimalFormat

import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    currencySymbol: String
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val df = DecimalFormat("#,##0")
    val df2 = DecimalFormat("#,##0.00")

    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val bgColor = MaterialTheme.colorScheme.background

    var periodMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        RootHeader(
            title = "Analytics",
            actions = {
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "Help",
                    tint = secTextColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Help click */ }
                )
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = periodMenuExpanded,
                onExpandedChange = { periodMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPeriod.label,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, tint = secTextColor, modifier = Modifier.size(16.dp)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = outlineColor,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
                ExposedDropdownMenu(
                    expanded = periodMenuExpanded,
                    onDismissRequest = { periodMenuExpanded = false }
                ) {
                    AnalyticsPeriod.values().forEach { period ->
                        DropdownMenuItem(
                            text = { Text(period.label) },
                            onClick = {
                                viewModel.setPeriod(period)
                                periodMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

        // ── Summary Bento Grid ──
        item {
            val changePctText = if (uiState.spendingChangePercent >= 0) "↑ ${df.format(uiState.spendingChangePercent.toDouble())}%" else "↓ ${df.format((-uiState.spendingChangePercent).toDouble())}%"
            val txChangePctText = if (uiState.transactionChangePercent >= 0) "↑ ${df.format(uiState.transactionChangePercent.toDouble())}%" else "↓ ${df.format((-uiState.transactionChangePercent).toDouble())}%"
            val avgChangePctText = if (uiState.avgChangePercent >= 0) "↑ ${df.format(uiState.avgChangePercent.toDouble())}%" else "↓ ${df.format((-uiState.avgChangePercent).toDouble())}%"

            // Row 1: Total Spending | Transactions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.TrendingUp,
                    title = "Total Spending",
                    value = "$currencySymbol${df.format(uiState.totalSpending.toLong())}",
                    subtitle = "$changePctText vs last period",
                    subtitleColor = if (uiState.spendingChangePercent >= 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )
                BentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.ListAlt,
                    title = "Transactions",
                    value = "${uiState.transactionCount}",
                    subtitle = "$txChangePctText vs last period",
                    subtitleColor = if (uiState.transactionChangePercent >= 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )
            }
        }

        item {
            val avgChangePctText = if (uiState.avgChangePercent >= 0) "↑ ${df.format(uiState.avgChangePercent.toDouble())}%" else "↓ ${df.format((-uiState.avgChangePercent).toDouble())}%"
            // Row 2: Avg Daily | Largest Expense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.AccessTime,
                    title = "Avg. Daily Spend",
                    value = "$currencySymbol${df.format(uiState.averageDailySpend.toLong())}",
                    subtitle = "$avgChangePctText vs last period",
                    subtitleColor = if (uiState.avgChangePercent >= 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )
                BentoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Receipt,
                    title = "Largest Expense",
                    value = "$currencySymbol${df.format(uiState.largestExpenseAmount.toLong())}",
                    subtitle = if (uiState.largestExpenseName.isNotEmpty()) "${uiState.largestExpenseName} • ${uiState.largestExpenseDate}" else "—",
                    subtitleColor = secTextColor
                )
            }
        }

        // ── Spending Trend (Canvas Area Chart) ──
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

        // ── Category Breakdown ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Category Breakdown", fontFamily = SpaceGroteskFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secTextColor)
            }
        }
        if (uiState.categoryReports.isEmpty()) {
            item {
                Text("No category data for this period.", fontSize = 13.sp, color = secTextColor.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
            }
        } else {
            items(uiState.categoryReports.take(5)) { report ->
                CategoryProgressRow(
                    report = report,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor,
                    outlineColor = outlineColor
                )
            }
        }

        // ── Monthly Comparison ──
        item {
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text("Monthly Comparison", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secTextColor)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("This Period", fontSize = 11.sp, color = secTextColor)
                        Text("$currencySymbol${df.format(uiState.totalSpending.toLong())}", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Last Period", fontSize = 11.sp, color = secTextColor)
                        Text("$currencySymbol${df.format(uiState.previousPeriodSpending.toLong())}", fontFamily = SpaceGroteskFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Difference", fontSize = 11.sp, color = secTextColor)
                        val diff = uiState.totalSpending - uiState.previousPeriodSpending
                        val diffSign = if (diff >= 0) "+" else ""
                        val diffPctText = if (uiState.spendingChangePercent >= 0) "↑ ${df.format(uiState.spendingChangePercent.toDouble())}%" else "↓ ${df.format((-uiState.spendingChangePercent).toDouble())}%"
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                diffPctText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                            Text(
                                "$diffSign$currencySymbol${df.format(kotlin.math.abs(diff).toLong())}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }
        }

        // ── Spending Activity Heatmap ──
        item {
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text("Spending Activity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secTextColor)
                Spacer(modifier = Modifier.height(12.dp))
                HeatmapGrid(
                    heatmapDays = uiState.heatmapDays,
                    accentColor = MaterialTheme.colorScheme.primary,
                    outlineColor = outlineColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", fontSize = 9.sp, color = secTextColor)
                    for (i in 0..4) {
                        val alpha = i * 0.25f
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    if (i == 0) outlineColor.copy(alpha = 0.3f)
                                    else MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    Text("More", fontSize = 9.sp, color = secTextColor)
                }
            }
        }

        // ── Top Merchants ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top Merchants", fontFamily = SpaceGroteskFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secTextColor)
            }
        }
        if (uiState.topMerchants.isEmpty()) {
            item {
                Text("No merchant data for this period.", fontSize = 13.sp, color = secTextColor.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
            }
        } else {
            items(uiState.topMerchants) { merchant ->
                MerchantRow(
                    merchant = merchant,
                    currencySymbol = currencySymbol,
                    onBgColor = onBgColor,
                    secTextColor = secTextColor,
                    outlineColor = outlineColor
                )
            }
        }
    }
}
}

// ────────────────────────────────────
// BENTO CARD COMPONENT
// ────────────────────────────────────
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color
) {
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    ShCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secTextColor)
            Icon(icon, null, tint = secTextColor, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            fontFamily = SpaceGroteskFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 10.sp, color = subtitleColor)
    }
}

// ────────────────────────────────────
// SPENDING TREND (CANVAS AREA CHART)
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Spending Trend", fontFamily = SpaceGroteskFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = onBgColor)
        }

        ShCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            if (trendPoints.isEmpty() || trendPoints.all { it.amount == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No trend data for this period.", fontSize = 13.sp, color = secTextColor.copy(alpha = 0.6f))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                ) {
                val maxVal = trendPoints.maxOf { it.amount }.coerceAtLeast(1.0)
                val chartLeftPad = 48f
                val chartTopPad = 8f
                val chartBottomPad = 28f

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val chartWidth = size.width - chartLeftPad
                    val chartHeight = size.height - chartTopPad - chartBottomPad
                    val points = trendPoints.mapIndexed { i, pt ->
                        val x = chartLeftPad + (i.toFloat() / (trendPoints.size - 1).coerceAtLeast(1)) * chartWidth
                        val y = chartTopPad + chartHeight * (1f - (pt.amount / maxVal).toFloat())
                        Offset(x, y)
                    }

                    // Y-axis gridlines
                    val ySteps = 4
                    for (i in 0..ySteps) {
                        val y = chartTopPad + (chartHeight * i / ySteps)
                        val labelVal = maxVal * (ySteps - i) / ySteps
                        drawLine(
                            color = outlineColor.copy(alpha = 0.3f),
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

                    // X-axis labels (show ~5 labels max)
                    val labelStep = (trendPoints.size / 5).coerceAtLeast(1)
                    for (i in trendPoints.indices step labelStep) {
                        val x = chartLeftPad + (i.toFloat() / (trendPoints.size - 1).coerceAtLeast(1)) * chartWidth
                        drawContext.canvas.nativeCanvas.drawText(
                            trendPoints[i].label,
                            x - 16f,
                            size.height - 2f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 20f
                                isAntiAlias = true
                            }
                        )
                    }

                    // Area fill (gradient)
                    if (points.size >= 2) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, chartTopPad + chartHeight)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, chartTopPad + chartHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0.0f)),
                                startY = 0f,
                                endY = chartTopPad + chartHeight
                            )
                        )

                        // Line
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = accentColor,
                            style = Stroke(width = 2.5f)
                        )

                        // Peak dot
                        val peakIdx = trendPoints.indices.maxByOrNull { trendPoints[it].amount } ?: 0
                        if (trendPoints[peakIdx].amount > 0) {
                            drawCircle(
                                color = accentColor,
                                radius = 5f,
                                center = points[peakIdx]
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// ────────────────────────────────────
// CATEGORY PROGRESS ROW
// ────────────────────────────────────
@Composable
fun CategoryProgressRow(
    report: CategoryReport,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color,
    outlineColor: Color
) {
    val df = DecimalFormat("#,##0")
    val catColor = try {
        Color(AndroidColor.parseColor(report.category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category icon circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(catColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Category, null, tint = catColor, modifier = Modifier.size(16.dp))
        }

        // Name + progress bar
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(report.category.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
            LinearProgressIndicator(
                progress = report.percentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = outlineColor.copy(alpha = 0.3f)
            )
        }

        // Percentage + amount
        Column(horizontalAlignment = Alignment.End) {
            Text("${(report.percentage * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = secTextColor)
            Text("$currencySymbol${df.format(report.totalAmount.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = onBgColor)
        }
    }
}

// ────────────────────────────────────
// HEATMAP GRID
// ────────────────────────────────────
@Composable
fun HeatmapGrid(
    heatmapDays: List<HeatmapDay>,
    accentColor: Color,
    outlineColor: Color
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val maxWeek = heatmapDays.maxOfOrNull { it.weekIndex } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // Show rows only for Mon, Wed, Fri, Sun to keep compact
        for (dayIdx in listOf(0, 2, 4, 6)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dayLabels.getOrElse(dayIdx) { "" },
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (weekIdx in 0..maxWeek) {
                        val day = heatmapDays.firstOrNull { it.dayOfWeek == dayIdx && it.weekIndex == weekIdx }
                        val color = if (day != null && day.intensity > 0f) {
                            accentColor.copy(alpha = (day.intensity * 0.8f + 0.2f).coerceIn(0.2f, 1f))
                        } else {
                            outlineColor.copy(alpha = 0.2f)
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────
// MERCHANT ROW
// ────────────────────────────────────
@Composable
fun MerchantRow(
    merchant: MerchantEntry,
    currencySymbol: String,
    onBgColor: Color,
    secTextColor: Color,
    outlineColor: Color
) {
    val df = DecimalFormat("#,##0")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Merchant icon badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                merchant.name.take(1).uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(merchant.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
            Text("${merchant.transactionCount} transactions", fontSize = 11.sp, color = secTextColor)
        }

        // Amount
        Text(
            "$currencySymbol${df.format(merchant.totalAmount.toLong())}",
            fontFamily = SpaceGroteskFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor
        )
    }
}
