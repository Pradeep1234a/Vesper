package com.vesper.ledger.ui.addtransaction

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShSegmentedControl
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.getIconByName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    currencySymbol: String,
    onBackClick: () -> Unit
) {
    val title by viewModel.title.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val type by viewModel.type.collectAsState()
    val categoryId by viewModel.categoryId.collectAsState()
    val dateEpochMillis by viewModel.dateEpochMillis.collectAsState()
    val note by viewModel.note.collectAsState()
    val filteredCategories by viewModel.filteredCategories.collectAsState()

    val context = LocalContext.current
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    val calendar = remember(dateEpochMillis) {
        Calendar.getInstance().apply { timeInMillis = dateEpochMillis }
    }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, day)
                viewModel.dateEpochMillis.value = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Transaction" else "Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Large Amount Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currencySymbol,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 36.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    BasicTextField(
                        value = amount,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                viewModel.amount.value = input
                            }
                        },
                        textStyle = MaterialTheme.typography.displayLarge.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 44.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(IntrinsicSize.Min)
                            .widthIn(min = 80.dp)
                    )
                }
            }

            // Segmented Control: Expense vs Income
            ShSegmentedControl(
                items = listOf("Expense", "Income"),
                selectedIndex = if (type == TransactionType.EXPENSE) 0 else 1,
                onItemSelected = { index ->
                    viewModel.type.value = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                }
            )

            // Title Field
            ShTextField(
                value = title,
                onValueChange = { viewModel.title.value = it },
                label = "Title",
                placeholder = "e.g., Grocery Store"
            )

            // Category Picker Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                
                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "No categories available.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { cat ->
                            val isSelected = categoryId == cat.id
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        if (isSelected) catColor.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        if (isSelected) BorderStroke(1.5.dp, catColor)
                                        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        MaterialTheme.shapes.small
                                    )
                                    .clickable { viewModel.categoryId.value = cat.id },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = getIconByName(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transaction Date Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() }
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transaction Date",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Text(
                    text = sdf.format(Date(dateEpochMillis)),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Note field
            ShTextField(
                value = note,
                onValueChange = { viewModel.note.value = it },
                label = "Notes (Optional)",
                placeholder = "Add details..."
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            ShButton(
                text = if (viewModel.isEditMode) "Save Changes" else "Save Transaction",
                onClick = {
                    viewModel.saveTransaction {
                        onBackClick()
                    }
                },
                enabled = amount.isNotBlank() && amount.toDoubleOrNull() ?: 0.0 > 0.0
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
