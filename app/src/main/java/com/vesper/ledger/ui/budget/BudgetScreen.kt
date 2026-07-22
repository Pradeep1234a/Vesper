package com.vesper.ledger.ui.budget

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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
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

import com.vesper.ledger.ui.components.ChildHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetsViewModel,
    currencySymbol: String = "$",
    onBackClick: () -> Unit
) {
    val budgetsWithStatus by viewModel.budgetsWithStatus.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<com.vesper.ledger.data.model.Budget?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriodFilter by remember { mutableStateOf("ALL") }

    val df = DecimalFormat("#,##0.00")
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val filteredBudgets = budgetsWithStatus.filter { item ->
        val matchesSearch = item.budget.name.contains(searchQuery, ignoreCase = true) ||
                item.categoryName.contains(searchQuery, ignoreCase = true)
        val matchesPeriod = selectedPeriodFilter == "ALL" || item.budget.period == selectedPeriodFilter
        matchesSearch && matchesPeriod
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Budgets",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBudget = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, "Add Budget")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // Search & Filter controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search budgets...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                var showFilterMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(
                        onClick = { showFilterMenu = true },
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .size(52.dp)
                    ) {
                        Icon(Icons.Default.FilterList, "Filter")
                    }
                    DropdownMenu(
                        expanded = showFilterMenu,
                        onDismissRequest = { showFilterMenu = false }
                    ) {
                        listOf("ALL", "WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY", "CUSTOM").forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriodFilter = period
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredBudgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No budgets found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredBudgets) { item ->
                        val budget = item.budget
                        val progress = item.progress

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = budget.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(android.graphics.Color.parseColor(item.categoryColor)))
                                        )
                                        Text(
                                            text = "${item.categoryName} • ${budget.period}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                var showMoreMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { showMoreMenu = true }) {
                                        Icon(Icons.Default.MoreVert, "Actions")
                                    }
                                    DropdownMenu(
                                        expanded = showMoreMenu,
                                        onDismissRequest = { showMoreMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit") },
                                            onClick = {
                                                editingBudget = budget
                                                showAddDialog = true
                                                showMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Duplicate") },
                                            onClick = {
                                                viewModel.addBudget(
                                                    name = "${budget.name} (Copy)",
                                                    amount = budget.amount,
                                                    period = budget.period,
                                                    categoryId = budget.categoryId,
                                                    startDate = budget.startDate,
                                                    endDate = budget.endDate,
                                                    notes = budget.notes
                                                )
                                                showMoreMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                viewModel.deleteBudget(budget)
                                                showMoreMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Spent: $currencySymbol${df.format(item.spentAmount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Limit: $currencySymbol${df.format(budget.amount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (progress >= 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remaining: $currencySymbol${df.format(item.remainingAmount)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.remainingAmount >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "${dateFormat.format(Date(budget.startDate))} - ${dateFormat.format(Date(budget.endDate))}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf(editingBudget?.name ?: "") }
        var amountStr by remember { mutableStateOf(editingBudget?.amount?.toString() ?: "") }
        var period by remember { mutableStateOf(editingBudget?.period ?: "MONTHLY") }
        var categoryId by remember { mutableStateOf(editingBudget?.categoryId ?: (categories.firstOrNull()?.id ?: 0L)) }
        var notes by remember { mutableStateOf(editingBudget?.notes ?: "") }

        var expandedPeriod by remember { mutableStateOf(false) }
        var expandedCategory by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (editingBudget == null) "Add Budget" else "Edit Budget",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Budget Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Limit Amount ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = period,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Budget Period") },
                            trailingIcon = {
                                IconButton(onClick = { expandedPeriod = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { expandedPeriod = true }
                        )
                        DropdownMenu(
                            expanded = expandedPeriod,
                            onDismissRequest = { expandedPeriod = false }
                        ) {
                            listOf("WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY").forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p) },
                                    onClick = {
                                        period = p
                                        expandedPeriod = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val activeCategoryName = categories.find { it.id == categoryId }?.name ?: "Select Category"
                        OutlinedTextField(
                            value = activeCategoryName,
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
                        if (name.isNotBlank() && amount > 0.0) {
                            val startCal = Calendar.getInstance()
                            val endCal = Calendar.getInstance()
                            when (period) {
                                "WEEKLY" -> {
                                    startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                                    endCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek + 6)
                                }
                                "MONTHLY" -> {
                                    startCal.set(Calendar.DAY_OF_MONTH, 1)
                                    endCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                }
                                "QUARTERLY" -> {
                                    val currentMonth = startCal.get(Calendar.MONTH)
                                    val startMonth = (currentMonth / 3) * 3
                                    startCal.set(Calendar.MONTH, startMonth)
                                    startCal.set(Calendar.DAY_OF_MONTH, 1)
                                    endCal.set(Calendar.MONTH, startMonth + 2)
                                    endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                                }
                                "YEARLY" -> {
                                    startCal.set(Calendar.DAY_OF_YEAR, 1)
                                    endCal.set(Calendar.DAY_OF_YEAR, startCal.getActualMaximum(Calendar.DAY_OF_YEAR))
                                }
                            }
                            // Normalize times
                            startCal.set(Calendar.HOUR_OF_DAY, 0)
                            startCal.set(Calendar.MINUTE, 0)
                            startCal.set(Calendar.SECOND, 0)
                            startCal.set(Calendar.MILLISECOND, 0)
                            endCal.set(Calendar.HOUR_OF_DAY, 23)
                            endCal.set(Calendar.MINUTE, 59)
                            endCal.set(Calendar.SECOND, 59)
                            endCal.set(Calendar.MILLISECOND, 999)

                            if (editingBudget == null) {
                                viewModel.addBudget(
                                    name = name,
                                    amount = amount,
                                    period = period,
                                    categoryId = categoryId,
                                    startDate = startCal.timeInMillis,
                                    endDate = endCal.timeInMillis,
                                    notes = notes.ifBlank { null }
                                )
                            } else {
                                viewModel.updateBudget(
                                    editingBudget!!.copy(
                                        name = name,
                                        amount = amount,
                                        period = period,
                                        categoryId = categoryId,
                                        startDate = startCal.timeInMillis,
                                        endDate = endCal.timeInMillis,
                                        notes = notes.ifBlank { null }
                                    )
                                )
                            }
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (editingBudget == null) "Add" else "Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
