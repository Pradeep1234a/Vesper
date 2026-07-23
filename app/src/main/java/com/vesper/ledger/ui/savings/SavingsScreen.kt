package com.vesper.ledger.ui.savings

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
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
import com.vesper.ledger.ui.components.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit
) {
    val goals by viewModel.allSavingsGoals.collectAsState()
    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    var showAddDialog by remember { mutableStateOf(false) }
    var adjustGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var adjustIsDeposit by remember { mutableStateOf(true) }

    var newGoalName by remember { mutableStateOf("") }
    var newGoalTarget by remember { mutableStateOf("") }
    var newGoalDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var adjustAmount by remember { mutableStateOf("") }

    val context = LocalContext.current

    if (showAddDialog) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, day)
                newGoalDate = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "New Savings Goal",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShTextField(
                        value = newGoalName,
                        onValueChange = { newGoalName = it },
                        label = "Goal Name",
                        placeholder = "e.g., Summer Vacation"
                    )
                    ShTextField(
                        value = newGoalTarget,
                        onValueChange = { newGoalTarget = it },
                        label = "Target Amount",
                        placeholder = "e.g., 2000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShOutlinedButton(
                        text = "Target Date: ${dateFormat.format(Date(newGoalDate))}",
                        onClick = { datePickerDialog.show() }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = newGoalTarget.toDoubleOrNull() ?: 0.0
                        if (newGoalName.isNotBlank() && target > 0) {
                            viewModel.addSavingsGoal(newGoalName, target, newGoalDate)
                            newGoalName = ""
                            newGoalTarget = ""
                            newGoalDate = System.currentTimeMillis()
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Add Goal",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        )
    }

    if (adjustGoal != null) {
        AlertDialog(
            onDismissRequest = { adjustGoal = null },
            title = {
                Text(
                    text = if (adjustIsDeposit) "Deposit Money" else "Withdraw Money",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            text = {
                Column {
                    Text(
                        text = adjustGoal?.name ?: "",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ShTextField(
                        value = adjustAmount,
                        onValueChange = { adjustAmount = it },
                        label = "Amount",
                        placeholder = "e.g., 50.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = adjustAmount.toDoubleOrNull() ?: 0.0
                        adjustGoal?.let {
                            val adjustment = if (adjustIsDeposit) amount else -amount
                            viewModel.adjustGoalAmount(it, adjustment)
                        }
                        adjustAmount = ""
                        adjustGoal = null
                    }
                ) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { adjustGoal = null }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Savings Goals",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Savings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No savings goals created yet.",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ShButton(
                            text = "Create a Goal",
                            onClick = { showAddDialog = true },
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                items(goals) { goal ->
                    ShCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Target: ${dateFormat.format(Date(goal.targetDateEpochMillis))}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSavingsGoal(goal) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Goal",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$currencySymbol${df.format(goal.currentAmount)}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "of $currencySymbol${df.format(goal.targetAmount)}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ShOutlinedButton(
                                text = "Withdraw",
                                onClick = {
                                    adjustGoal = goal
                                    adjustIsDeposit = false
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ShButton(
                                text = "Deposit",
                                onClick = {
                                    adjustGoal = goal
                                    adjustIsDeposit = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
}
