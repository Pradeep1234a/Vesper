package com.vesper.ledger.ui.savings

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.SavingsGoal
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val SAVINGS_ICONS = mapOf(
    "savings" to Icons.Outlined.Savings,
    "flight" to Icons.Outlined.Flight,
    "directions_car" to Icons.Outlined.DirectionsCar,
    "home" to Icons.Outlined.Home,
    "laptop" to Icons.Outlined.Laptop,
    "school" to Icons.Outlined.School,
    "medical_services" to Icons.Outlined.MedicalServices,
    "trending_up" to Icons.Outlined.TrendingUp
)

fun getSavingsIcon(name: String): ImageVector {
    return SAVINGS_ICONS[name] ?: Icons.Outlined.Savings
}

val SAVINGS_COLORS = listOf(
    "#38BDF8", // Cyan
    "#22C55E", // Emerald
    "#A855F7", // Purple
    "#F59E0B", // Amber
    "#F43F5E"  // Rose
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSavingsGoalScreen(
    goalToEdit: SavingsGoal? = null,
    currencySymbol: String = "$",
    onBackClick: () -> Unit,
    onSaveGoal: (name: String, targetAmount: Double, currentAmount: Double, targetDateEpochMillis: Long, iconName: String, colorHex: String, goalIdToUpdate: Long?) -> Unit,
    onDeleteGoal: ((SavingsGoal) -> Unit)? = null
) {
    val context = LocalContext.current
    val isEditMode = goalToEdit != null
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var nameText by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmountText by remember { mutableStateOf(goalToEdit?.targetAmount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var currentAmountText by remember { mutableStateOf(goalToEdit?.currentAmount?.toString() ?: "0.00") }
    var targetDateMillis by remember { mutableStateOf(goalToEdit?.targetDateEpochMillis ?: (System.currentTimeMillis() + 30L * 86400000L)) }
    var selectedIcon by remember { mutableStateOf(goalToEdit?.let { "savings" } ?: "savings") }
    var selectedColorHex by remember { mutableStateOf("#38BDF8") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Delete Goal Confirmation Dialog
    if (showDeleteConfirmDialog && goalToEdit != null && onDeleteGoal != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Savings Goal",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${goalToEdit.name}'? Accumulated savings data for this goal will be removed.",
                    fontFamily = PlusJakartaSansFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteGoal(goalToEdit)
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "Savings goal deleted", Toast.LENGTH_SHORT).show()
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

    // Date Picker Dialog
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = targetDateMillis
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, day)
            targetDateMillis = newCal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
                        text = if (isEditMode) "SAVE GOAL CHANGES" else "CREATE SAVINGS GOAL",
                        onClick = {
                            val target = targetAmountText.toDoubleOrNull() ?: 0.0
                            val current = currentAmountText.toDoubleOrNull() ?: 0.0

                            if (nameText.isBlank()) {
                                Toast.makeText(context, "Please enter a goal name", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }
                            if (target <= 0) {
                                Toast.makeText(context, "Please enter a valid target amount", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }

                            onSaveGoal(
                                nameText.trim(),
                                target,
                                current,
                                targetDateMillis,
                                selectedIcon,
                                selectedColorHex,
                                goalToEdit?.id
                            )
                            Toast.makeText(
                                context,
                                if (isEditMode) "Goal updated" else "Goal created successfully",
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
            // Form Card Container
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Header Subtitle
                    Text(
                        text = if (isEditMode) "EDIT GOAL DETAILS" else "GOAL PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 1. Goal Name
                    ShTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = "Goal Name",
                        placeholder = "e.g., Summer Vacation, Emergency Fund"
                    )

                    // 2. Target Amount
                    ShTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it },
                        label = "Target Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 3. Initial / Current Saved Amount
                    ShTextField(
                        value = currentAmountText,
                        onValueChange = { currentAmountText = it },
                        label = "Initial Saved Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 4. Target Completion Date Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Target Completion Date",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
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
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = dateFormat.format(Date(targetDateMillis)),
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Change",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 5. Goal Category Icon Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Goal Icon",
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SAVINGS_ICONS.forEach { (iconKey, iconVector) ->
                                val isSelected = selectedIcon == iconKey
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedIcon = iconKey },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVector,
                                        contentDescription = iconKey,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 6. Accent Color Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SAVINGS_COLORS.forEach { hex ->
                                val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color(0xFF38BDF8) }
                                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Delete Goal Button (Only visible in edit mode)
            if (isEditMode && onDeleteGoal != null) {
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
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DELETE GOAL",
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
