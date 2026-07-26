package com.vesper.ledger.ui.savings

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.SavingsGoal
import com.vesper.ledger.ui.accounts.ElasticBounceContainer
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.safeParseColor
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
                        Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show()
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
                        text = if (isEditMode) "SAVE GOAL CHANGES" else "CREATE SAVINGS GOAL",
                        onClick = {
                            val targetVal = targetAmountText.toDoubleOrNull() ?: 0.0
                            val currentVal = currentAmountText.toDoubleOrNull() ?: 0.0

                            if (nameText.isBlank()) {
                                Toast.makeText(context, "Please enter a savings goal name", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }

                            if (targetVal <= 0.0) {
                                Toast.makeText(context, "Please enter a valid target amount", Toast.LENGTH_SHORT).show()
                                return@ShButton
                            }

                            onSaveGoal(
                                nameText.trim(),
                                targetVal,
                                currentVal,
                                targetDateMillis,
                                selectedIcon,
                                selectedColorHex,
                                goalToEdit?.id
                            )
                            Toast.makeText(
                                context,
                                if (isEditMode) "Savings goal updated" else "Savings goal created successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            onBackClick()
                        },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                }
            }
        }
    ) { innerPadding ->
        ElasticBounceContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 1. LIVE SAVINGS GOAL PREVIEW CARD (Sectioned Bento Card)
                val parsedTarget = targetAmountText.toDoubleOrNull() ?: 0.0
                val parsedCurrent = currentAmountText.toDoubleOrNull() ?: 0.0
                val progress = if (parsedTarget > 0) (parsedCurrent / parsedTarget).toFloat().coerceIn(0f, 1f) else 0f
                val parsedAccent = safeParseColor(selectedColorHex)

                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(parsedAccent.copy(alpha = 0.15f))
                                    .border(1.dp, parsedAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getSavingsIcon(selectedIcon),
                                    contentDescription = null,
                                    tint = parsedAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nameText.ifBlank { "Savings Target" },
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Target Date: ${dateFormat.format(Date(targetDateMillis))}",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format("%.2f", parsedTarget)}",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}% SAVED",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = parsedAccent
                                )
                            }
                        }

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = parsedAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // 2. GOAL NAME INPUT CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    ShTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = "Savings Goal Name",
                        placeholder = "e.g., Emergency Fund, Vacation, New Laptop"
                    )
                }

                // 3. TARGET AMOUNT INPUT CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    ShTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it },
                        label = "Target Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                // 4. INITIAL SAVED AMOUNT CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    ShTextField(
                        value = currentAmountText,
                        onValueChange = { currentAmountText = it },
                        label = "Currently Saved Amount ($currencySymbol)",
                        placeholder = "0.00",
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
                    )
                }

                // 5. TARGET DATE PICKER CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Target Date",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        val cal = Calendar.getInstance()
                        cal.timeInMillis = targetDateMillis

                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val selectedCal = Calendar.getInstance()
                                selectedCal.set(y, m, d)
                                targetDateMillis = selectedCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = dateFormat.format(Date(targetDateMillis)),
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // 6. ICON SELECTION CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Goal Icon",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SAVINGS_ICONS.keys.forEach { iconName ->
                                val isSelected = selectedIcon == iconName
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelected) parsedAccent.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) parsedAccent else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedIcon = iconName },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getSavingsIcon(iconName),
                                        contentDescription = iconName,
                                        tint = if (isSelected) parsedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. ACCENT COLOR CARD
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Accent Color",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SAVINGS_COLORS.forEach { hex ->
                                val parsedColor = safeParseColor(hex)
                                val isSelected = selectedColorHex.equals(hex, ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
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

                // 8. DELETE GOAL CARD (If Edit Mode)
                if (isEditMode && onDeleteGoal != null) {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete Goal",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DELETE SAVINGS GOAL",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
