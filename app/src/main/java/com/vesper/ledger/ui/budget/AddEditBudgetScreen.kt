package com.vesper.ledger.ui.budget

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.util.Calendar

val BUDGET_PERIODS = listOf("MONTHLY", "WEEKLY", "YEARLY")

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
    val context = LocalContext.current
    val isEditMode = budgetToEdit != null

    var nameText by remember { mutableStateOf(budgetToEdit?.name ?: "") }
    var amountText by remember { mutableStateOf(budgetToEdit?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var selectedPeriod by remember { mutableStateOf(budgetToEdit?.period ?: "MONTHLY") }
    var selectedCategoryId by remember { mutableStateOf(budgetToEdit?.categoryId ?: (categories.firstOrNull()?.id ?: 0L)) }
    var notesText by remember { mutableStateOf(budgetToEdit?.notes ?: "") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && budgetToEdit != null && onDeleteBudget != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Budget",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${budgetToEdit.name.ifBlank { selectedCategory?.name ?: "this budget" }}'? Spent records will remain intact.",
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBudget(budgetToEdit)
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Budget deleted", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    ShButton(
                        text = if (isEditMode) "SAVE BUDGET CHANGES" else "CREATE BUDGET",
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0

                            if (amount <= 0) {
                                Toast.makeText(context, "Please enter a valid limit amount", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }

                            val cal = Calendar.getInstance()
                            val now = cal.timeInMillis
                            cal.add(Calendar.DAY_OF_MONTH, 30)
                            val endDate = cal.timeInMillis

                            val finalName = nameText.trim().ifBlank { selectedCategory?.name ?: "Budget" }

                            onSaveBudget(
                                finalName,
                                amount,
                                selectedPeriod,
                                selectedCategoryId,
                                now,
                                endDate,
                                notesText.ifBlank { null },
                                budgetToEdit?.id
                            )
                            Toast.makeText(
                                context,
                                if (isEditMode) "Budget updated" else "Budget created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form Card Container (Reference to AddEditAccountScreen)
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = if (isEditMode) "EDIT BUDGET DETAILS" else "BUDGET PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 1. Category Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedCategoryId = cat.id }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(cat.iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = cat.name,
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Budget Limit Amount Input
                    ShTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = "Limit Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 3. Optional Custom Budget Name
                    ShTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = "Budget Name (Optional)",
                        placeholder = selectedCategory?.name ?: "e.g., Dining Out"
                    )

                    // 4. Budget Period Selection
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Budget Period",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BUDGET_PERIODS.forEach { p ->
                                val isSelected = selectedPeriod.equals(p, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedPeriod = p }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p,
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 5. Notes / Description Input
                    ShTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = "Notes / Restrictions (Optional)",
                        placeholder = "e.g., Exclude weekend spending"
                    )
                }
            }

            // Delete Budget Button (Only in Edit Mode)
            if (isEditMode && onDeleteBudget != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Budget",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DELETE BUDGET",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
