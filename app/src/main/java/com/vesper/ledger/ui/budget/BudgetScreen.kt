package com.vesper.ledger.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import com.vesper.ledger.ui.components.M3SingleFab
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetsViewModel,
    currencySymbol: String = "₹",
    onBackClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onAddBudgetClick: () -> Unit = {},
    onEditBudgetClick: (Budget) -> Unit = {}
) {
    val budgetsWithStatus by viewModel.budgetsWithStatus.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriodFilter by remember { mutableStateOf("ALL") }

    val df = DecimalFormat("#,##0.00")
    val dfCompact = DecimalFormat("#,##0")
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val filteredBudgets = remember(budgetsWithStatus, searchQuery, selectedPeriodFilter) {
        budgetsWithStatus.filter { item ->
            val matchesSearch = item.budget.name.contains(searchQuery, ignoreCase = true) ||
                    item.categoryName.contains(searchQuery, ignoreCase = true)
            val matchesPeriod = selectedPeriodFilter == "ALL" || item.budget.period.equals(selectedPeriodFilter, ignoreCase = true)
            matchesSearch && matchesPeriod
        }
    }

    val totalBudgetLimit = budgetsWithStatus.sumOf { it.budget.amount }
    val totalBudgetSpent = budgetsWithStatus.sumOf { it.spentAmount }
    val totalRemaining = totalBudgetLimit - totalBudgetSpent
    val overallProgress = if (totalBudgetLimit > 0) (totalBudgetSpent / totalBudgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            M3SingleFab(
                onClick = onAddBudgetClick,
                contentDescription = "Add Budget"
            )
        },
        containerColor = Color(0xFF09090B)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. OVERALL BUDGET SUMMARY BENTO CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF18181B))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "TOTAL BUDGET LIMIT",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }

                        Text(
                            text = "$currencySymbol${df.format(totalBudgetLimit)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = overallProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (overallProgress >= 0.9f) Color(0xFFEF4444) else Color(0xFF38BDF8),
                        trackColor = Color(0xFF242429)
                    )

                    // 3 KPI Pills Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SPENT",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1A1AA)
                            )
                            Text(
                                text = "$currencySymbol${dfCompact.format(totalBudgetSpent)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalBudgetSpent > totalBudgetLimit && totalBudgetLimit > 0) Color(0xFFEF4444) else Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "UTILIZATION",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1A1AA)
                            )
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "REMAINING",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1A1AA)
                            )
                            Text(
                                text = "$currencySymbol${dfCompact.format(totalRemaining)}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalRemaining >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            // 2. PERIOD FILTER HORIZONTAL CHIPS ROW
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val periods = listOf("ALL", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY")
                items(periods) { period ->
                    val isSelected = selectedPeriodFilter == period
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF18181B))
                            .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF27272A), RoundedCornerShape(6.dp))
                            .clickable { selectedPeriodFilter = period }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = period,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            // 3. SEARCH BAR FIELD
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search budgets or categories...",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(6.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF18181B),
                    unfocusedContainerColor = Color(0xFF18181B),
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF27272A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // 4. BUDGET LIST OR EMPTY STATE
            if (filteredBudgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No budgets found for this period.",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 13.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredBudgets) { item ->
                        val budget = item.budget
                        val progress = item.progress
                        val catColor = safeParseColor(item.categoryColor)

                        val statusColor = when {
                            progress >= 1.0f -> Color(0xFFEF4444)
                            progress >= 0.85f -> Color(0xFFF59E0B)
                            else -> Color(0xFF22C55E)
                        }

                        val statusText = when {
                            progress >= 1.0f -> "EXCEEDED"
                            progress >= 0.85f -> "WARNING"
                            else -> "HEALTHY"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF18181B))
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                                .clickable { onEditBudgetClick(budget) }
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(catColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconByName(item.categoryIcon),
                                                contentDescription = null,
                                                tint = catColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = budget.name,
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${item.categoryName} • ${budget.period}",
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Status Pill Badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = statusText,
                                                fontFamily = SpaceGroteskFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                        }

                                        var showMoreMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(
                                                onClick = { showMoreMenu = true },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Actions",
                                                    tint = Color(0xFFA1A1AA),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMoreMenu,
                                                onDismissRequest = { showMoreMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Edit Budget", fontFamily = SpaceGroteskFamily) },
                                                    onClick = {
                                                        showMoreMenu = false
                                                        onEditBudgetClick(budget)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete", fontFamily = SpaceGroteskFamily, color = Color(0xFFEF4444)) },
                                                    onClick = {
                                                        showMoreMenu = false
                                                        viewModel.deleteBudget(budget)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Spent: $currencySymbol${df.format(item.spentAmount)}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFA1A1AA)
                                    )
                                    Text(
                                        text = "Limit: $currencySymbol${df.format(budget.amount)}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = progress.coerceIn(0f, 1f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = statusColor,
                                    trackColor = Color(0xFF242429)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Remaining: $currencySymbol${df.format(item.remainingAmount)}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.remainingAmount >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "${dateFormat.format(Date(budget.startDate))} - ${dateFormat.format(Date(budget.endDate))}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 10.sp,
                                        color = Color(0xFFA1A1AA)
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
