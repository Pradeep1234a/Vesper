package com.vesper.ledger.ui.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.SavingsGoal
import com.vesper.ledger.ui.accounts.ElasticBounceContainer
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel,
    currencySymbol: String = "$",
    onBackClick: () -> Unit = {},
    onAddGoalClick: () -> Unit = {},
    onEditGoalClick: (SavingsGoal) -> Unit = {}
) {
    val goals by viewModel.allSavingsGoals.collectAsState()
    val df = remember { DecimalFormat("#,##0.00") }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var adjustGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var adjustIsDeposit by remember { mutableStateOf(true) }
    var adjustAmountText by remember { mutableStateOf("") }

    val totalCurrentSavings = remember(goals) { goals.sumOf { it.currentAmount } }
    val totalTargetSavings = remember(goals) { goals.sumOf { it.targetAmount } }
    val overallProgress = if (totalTargetSavings > 0) (totalCurrentSavings / totalTargetSavings).toFloat().coerceIn(0f, 1f) else 0f

    // Deposit / Withdraw Adjustment Dialog
    if (adjustGoal != null) {
        AlertDialog(
            onDismissRequest = { adjustGoal = null },
            title = {
                Text(
                    text = if (adjustIsDeposit) "Deposit to Goal" else "Withdraw from Goal",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = adjustGoal?.name ?: "",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ShTextField(
                        value = adjustAmountText,
                        onValueChange = { adjustAmountText = it },
                        label = "Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = adjustAmountText.toDoubleOrNull() ?: 0.0
                        adjustGoal?.let {
                            val adjustment = if (adjustIsDeposit) amount else -amount
                            viewModel.adjustGoalAmount(it, adjustment)
                        }
                        adjustAmountText = ""
                        adjustGoal = null
                    }
                ) {
                    Text(
                        text = "Confirm",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustGoal = null }) {
                    Text(
                        text = "Cancel",
                        fontFamily = SpaceGroteskFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
        )
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()

    val isFabVisible by remember {
        derivedStateOf { !lazyListState.isScrollInProgress || lazyListState.firstVisibleItemIndex == 0 }
    }

    Scaffold(
        topBar = {
            com.vesper.ledger.ui.components.VesperUnifiedTopBar(
                title = "Savings Goals",
                isRoot = false,
                onNavigationClick = onBackClick,
                actions = {
                    IconButton(onClick = onAddGoalClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Savings Goal",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SingleFab(
                onClick = onAddGoalClick,
                contentDescription = "Add Savings Goal",
                visible = isFabVisible,
                hasBottomBar = false
            )
        }
    ) { innerPadding ->
        ElasticBounceContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                // 1. TOTAL SAVINGS SUMMARY BANNER CARD (Reference to AccountsScreen summary banner)
                item {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SAVINGS ACCUMULATED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$currencySymbol${df.format(totalCurrentSavings)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${goals.size} active goals • $currencySymbol${df.format(totalTargetSavings)} total target savings",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = PlusJakartaSansFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )

                            if (totalTargetSavings > 0) {
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "OVERALL PROGRESS",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${(overallProgress * 100).toInt()}% SAVED",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = overallProgress,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF38BDF8),
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }

                // 2. SAVINGS GOAL LIST OR EMPTY STATE (Reference to AccountsScreen items)
                if (goals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Savings,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No savings goals created yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap + to create your first goal for travel, gadgets or emergency funds.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = PlusJakartaSansFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(goals, key = { it.id }) { goal ->
                        val isCompleted = goal.targetAmount > 0 && goal.currentAmount >= goal.targetAmount
                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f

                        ShCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEditGoalClick(goal) },
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
                                            .background(
                                                if (isCompleted) Color(0xFF22C55E).copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isCompleted) Color(0xFF22C55E).copy(alpha = 0.3f)
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getSavingsIcon("savings"),
                                            contentDescription = null,
                                            tint = if (isCompleted) Color(0xFF22C55E) else Color(0xFF38BDF8),
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
                                                text = goal.name,
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
                                                    .background(
                                                        if (isCompleted) Color(0xFF22C55E).copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isCompleted) "COMPLETED" else "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = if (isCompleted) Color(0xFF22C55E) else Color(0xFF38BDF8)
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "Target Date: ${dateFormat.format(Date(goal.targetDateEpochMillis))}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = PlusJakartaSansFamily,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit Goal",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                                // Progress & Amounts Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "CURRENT SAVINGS",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currencySymbol${df.format(goal.currentAmount)}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = if (isCompleted) Color(0xFF22C55E) else Color.White
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "TARGET",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$currencySymbol${df.format(goal.targetAmount)}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Linear Progress Indicator
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (isCompleted) Color(0xFF22C55E) else Color(0xFF38BDF8),
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(progress * 100).toInt()}% completed",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                                        Text(
                                            text = if (remaining > 0) "$currencySymbol${df.format(remaining)} left" else "Goal reached!",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (remaining > 0) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF22C55E)
                                        )
                                    }
                                }

                                // Action Buttons (Deposit / Withdraw)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            adjustGoal = goal
                                            adjustIsDeposit = false
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Remove,
                                            contentDescription = "Withdraw",
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "WITHDRAW",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            adjustGoal = goal
                                            adjustIsDeposit = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Add,
                                            contentDescription = "Deposit",
                                            tint = Color.Black,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "DEPOSIT",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
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
