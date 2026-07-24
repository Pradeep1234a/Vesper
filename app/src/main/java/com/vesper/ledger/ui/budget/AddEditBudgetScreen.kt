package com.vesper.ledger.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    budgetToEdit: Budget? = null,
    categories: List<Category>,
    currencySymbol: String = "$",
    onBackClick: () -> Unit,
    onSaveBudget: (
        name: String,
        amount: Double,
        period: String,
        categoryId: Long,
        startDate: Long,
        endDate: Long,
        notes: String?,
        idToUpdate: Long?
    ) -> Unit,
    onDeleteBudget: ((Budget) -> Unit)? = null
) {
    var name by remember { mutableStateOf(budgetToEdit?.name ?: "") }
    var amountStr by remember { mutableStateOf(budgetToEdit?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var period by remember { mutableStateOf(budgetToEdit?.period ?: "MONTHLY") }
    var selectedCategoryId by remember { mutableStateOf(budgetToEdit?.categoryId ?: (categories.firstOrNull()?.id ?: 0L)) }
    var notes by remember { mutableStateOf(budgetToEdit?.notes ?: "") }

    val df = DecimalFormat("#,##0.00")
    val isEditMode = budgetToEdit != null
    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()

    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0

    Scaffold(
        topBar = {
            ChildHeader(
                title = if (isEditMode) "Edit Budget" else "New Budget",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Card
            Text(
                text = "PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            val catColor = safeParseColor(selectedCategory?.colorHex ?: "#71717A")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
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
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(selectedCategory?.iconName ?: "category"),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = name.ifBlank { "Budget Name" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "${selectedCategory?.name ?: "Category"} • $period",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = period,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Limit Amount",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "$currencySymbol${df.format(parsedAmount)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = 0.25f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = catColor,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }

            // Budget Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Budget Name", fontFamily = SpaceGroteskFamily) },
                placeholder = { Text("e.g. Monthly Groceries, Fuel Limit") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Limit Amount Input
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Limit Amount ($currencySymbol)", fontFamily = SpaceGroteskFamily) },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            // Budget Period Segmented Control
            Text(
                text = "BUDGET PERIOD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("WEEKLY", "MONTHLY", "QUARTERLY", "YEARLY").forEach { itemPeriod ->
                    val selected = period == itemPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { period = itemPeriod },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = itemPeriod,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Category Selection Chips
            Text(
                text = "LINK TO CATEGORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val color = safeParseColor(cat.colorHex)

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategoryId = cat.id },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getIconByName(cat.iconName),
                                contentDescription = null,
                                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // Notes Input
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)", fontFamily = SpaceGroteskFamily) },
                placeholder = { Text("Set reminders, goals, or remarks...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (name.isNotBlank() && parsedAmount > 0) {
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
                        startCal.set(Calendar.HOUR_OF_DAY, 0)
                        startCal.set(Calendar.MINUTE, 0)
                        startCal.set(Calendar.SECOND, 0)
                        startCal.set(Calendar.MILLISECOND, 0)
                        endCal.set(Calendar.HOUR_OF_DAY, 23)
                        endCal.set(Calendar.MINUTE, 59)
                        endCal.set(Calendar.SECOND, 59)
                        endCal.set(Calendar.MILLISECOND, 999)

                        onSaveBudget(
                            name.trim(),
                            parsedAmount,
                            period,
                            selectedCategoryId,
                            startCal.timeInMillis,
                            endCal.timeInMillis,
                            notes.ifBlank { null },
                            budgetToEdit?.id
                        )
                        onBackClick()
                    }
                },
                enabled = name.isNotBlank() && parsedAmount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = if (isEditMode) "Save Changes" else "Create Budget",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (isEditMode && budgetToEdit != null && onDeleteBudget != null) {
                OutlinedButton(
                    onClick = {
                        onDeleteBudget(budgetToEdit)
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete Budget",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Budget",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
