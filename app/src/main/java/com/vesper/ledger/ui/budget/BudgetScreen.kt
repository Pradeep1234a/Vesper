package com.vesper.ledger.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.ui.accounts.ElasticBounceContainer
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetsViewModel,
    currencySymbol: String = "$",
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onAddBudgetClick: () -> Unit = {},
    onEditBudgetClick: (Budget) -> Unit = {}
) {
    val budgetsWithStatus by viewModel.budgetsWithStatus.collectAsState()
    var selectedPeriodFilter by remember { mutableStateOf("ALL") }

    val df = remember { DecimalFormat("#,##0.00") }

    val filteredBudgets = remember(budgetsWithStatus, selectedPeriodFilter) {
        if (selectedPeriodFilter == "ALL") {
            budgetsWithStatus
        } else {
            budgetsWithStatus.filter { it.budget.period.equals(selectedPeriodFilter, ignoreCase = true) }
        }
    }

    val totalBudgetLimit = remember(budgetsWithStatus) { budgetsWithStatus.sumOf { it.budget.amount } }
    val totalBudgetSpent = remember(budgetsWithStatus) { budgetsWithStatus.sumOf { it.spentAmount } }
    val overallProgress = if (totalBudgetLimit > 0) (totalBudgetSpent / totalBudgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    val lazyListState = rememberLazyListState()
    val isFabVisible by com.vesper.ledger.ui.components.rememberFabVisibility(lazyListState)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SingleFab(
                onClick = onAddBudgetClick,
                contentDescription = "Add Budget",
                visible = isFabVisible,
                hasBottomBar = true
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
                // 1. TOTAL BUDGET SUMMARY BANNER CARD (Reference to AccountsScreen)
                item {
                    val overallStatusColor = when {
                        overallProgress >= 1f -> Color(0xFFF43F5E)
                        overallProgress >= 0.8f -> Color(0xFFF59E0B)
                        else -> Color(0xFF22C55E)
                    }
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL MONTHLY BUDGET ALLOCATION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${currencySymbol}${df.format(totalBudgetSpent)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = "SPENT OF ${currencySymbol}${df.format(totalBudgetLimit)} ALLOCATED",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                if (budgetsWithStatus.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(overallStatusColor.copy(alpha = 0.15f))
                                            .border(1.dp, overallStatusColor, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "${budgetsWithStatus.size} ACTIVE",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = overallStatusColor
                                        )
                                    }
                                }
                            }

                            if (totalBudgetLimit > 0) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "OVERALL BUDGET USAGE",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(overallProgress * 100).toInt()}%",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = overallStatusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = overallProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = overallStatusColor,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }

                // 2. BUDGET LIST ITEMS OR REDESIGNED ELEGANT M3 EMPTY STATE
                if (filteredBudgets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PieChart,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "No Budgets Yet",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Create monthly spending limits for categories like dining, shopping, or utilities.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    ),
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredBudgets, key = { it.budget.id }) { item ->
                        val limit = item.budget.amount
                        val spent = item.spentAmount
                        val remaining = limit - spent
                        val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f

                        val statusColor = when {
                            progress >= 1f -> Color(0xFFF43F5E) // Red Exceeded
                            progress >= 0.8f -> Color(0xFFF59E0B) // Amber Warning
                            else -> Color(0xFF22C55E) // Emerald Healthy
                        }

                        val statusText = when {
                            progress >= 1f -> "EXCEEDED"
                            progress >= 0.8f -> "WARNING"
                            else -> "HEALTHY"
                        }

                        ShCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditBudgetClick(item.budget) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 48dp Icon Container matching AccountsScreen
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(statusColor.copy(alpha = 0.12f))
                                            .border(
                                                1.dp,
                                                statusColor.copy(alpha = 0.3f),
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(item.categoryIcon),
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = item.budget.name.ifBlank { item.categoryName },
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(statusColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = statusText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = statusColor
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "${item.categoryName} • ${item.budget.period.uppercase()}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PlusJakartaSansFamily,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit Budget",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                                // Amounts Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "SPENT THIS MONTH",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currencySymbol${df.format(spent)}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = statusColor
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "BUDGET LIMIT",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currencySymbol${df.format(limit)}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Linear Progress Bar & Remaining Readout
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = statusColor,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(progress * 100).toInt()}% utilized",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (remaining >= 0) "$currencySymbol${df.format(remaining)} remaining" else "$currencySymbol${df.format(-remaining)} over limit",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remaining >= 0) statusColor else Color(0xFFF43F5E)
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
}
