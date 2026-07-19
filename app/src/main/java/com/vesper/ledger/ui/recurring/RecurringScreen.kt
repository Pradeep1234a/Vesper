package com.vesper.ledger.ui.recurring

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    viewModel: RecurringViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit
) {
    val items by viewModel.recurringWithDetails.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recurring Transactions",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Recurring")
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recurring schedules set up.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(items) { item ->
                    val rec = item.recurring
                    val isActive = !rec.isPaused

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(
                                if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rec.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${rec.type} • ${rec.frequency} • ${item.accountName}" +
                                        (if (item.targetAccountName != null) " → ${item.targetAccountName}" else ""),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Category: ${item.categoryName} • Starts ${dateFormat.format(Date(rec.startDate))}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$currencySymbol${df.format(rec.amount)}",
                                fontSize = 14.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(onClick = { viewModel.togglePause(rec) }) {
                                Icon(
                                    imageVector = if (isActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isActive) "Pause" else "Resume",
                                    tint = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { viewModel.deleteRecurring(rec) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("EXPENSE") }
        var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
        var targetAccountId by remember { mutableStateOf<Long?>(null) }
        var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 0L) }
        var paymentMethod by remember { mutableStateOf("Cash") }
        var frequency by remember { mutableStateOf("MONTHLY") }
        var autoCreate by remember { mutableStateOf(true) }
        var notes by remember { mutableStateOf("") }

        var expandedType by remember { mutableStateOf(false) }
        var expandedAccount by remember { mutableStateOf(false) }
        var expandedTargetAccount by remember { mutableStateOf(false) }
        var expandedCategory by remember { mutableStateOf(false) }
        var expandedFrequency by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add Recurring Schedule",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Transaction Type") },
                            trailingIcon = {
                                IconButton(onClick = { expandedType = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandedType = true }
                        )
                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            listOf("EXPENSE", "INCOME", "TRANSFER").forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        type = t
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val activeAccName = accounts.find { it.id == accountId }?.name ?: "Select Account"
                        OutlinedTextField(
                            value = activeAccName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Source Account") },
                            trailingIcon = {
                                IconButton(onClick = { expandedAccount = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandedAccount = true }
                        )
                        DropdownMenu(
                            expanded = expandedAccount,
                            onDismissRequest = { expandedAccount = false }
                        ) {
                            accounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.name) },
                                    onClick = {
                                        accountId = acc.id
                                        expandedAccount = false
                                    }
                                )
                            }
                        }
                    }

                    if (type == "TRANSFER") {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val activeTargetName = accounts.find { it.id == targetAccountId }?.name ?: "Select Destination Account"
                            OutlinedTextField(
                                value = activeTargetName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Destination Account") },
                                trailingIcon = {
                                    IconButton(onClick = { expandedTargetAccount = true }) {
                                        Icon(Icons.Outlined.ArrowDropDown, null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { expandedTargetAccount = true }
                            )
                            DropdownMenu(
                                expanded = expandedTargetAccount,
                                onDismissRequest = { expandedTargetAccount = false }
                            ) {
                                accounts.filter { it.id != accountId }.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            targetAccountId = acc.id
                                            expandedTargetAccount = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val activeCatName = categories.find { it.id == categoryId }?.name ?: "Select Category"
                        OutlinedTextField(
                            value = activeCatName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                IconButton(onClick = { expandedCategory = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandedCategory = true }
                        )
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        categoryId = cat.id
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = frequency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Frequency") },
                            trailingIcon = {
                                IconButton(onClick = { expandedFrequency = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandedFrequency = true }
                        )
                        DropdownMenu(
                            expanded = expandedFrequency,
                            onDismissRequest = { expandedFrequency = false }
                        ) {
                            listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { f ->
                                DropdownMenuItem(
                                    text = { Text(f) },
                                    onClick = {
                                        frequency = f
                                        expandedFrequency = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-Create Transactions", fontSize = 14.sp)
                        Switch(checked = autoCreate, onCheckedChange = { autoCreate = it })
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0.0) {
                            viewModel.addRecurringTransaction(
                                title = title,
                                amount = amount,
                                type = type,
                                accountId = accountId,
                                targetAccountId = if (type == "TRANSFER") targetAccountId else null,
                                categoryId = categoryId,
                                paymentMethod = paymentMethod,
                                frequency = frequency,
                                startDate = System.currentTimeMillis(),
                                endDate = null,
                                notes = notes.ifBlank { null },
                                autoCreate = autoCreate
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
