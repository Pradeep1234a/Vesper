package com.vesper.ledger.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShSegmentedControl
import com.vesper.ledger.ui.components.ShTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val currency by viewModel.currency.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var newCatName by remember { mutableStateOf("") }
    var newCatType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var newCatColor by remember { mutableStateOf("#71717A") }

    val colorOptions = listOf(
        "#16A34A", // Green
        "#DC2626", // Red
        "#2563EB", // Blue
        "#D97706", // Amber
        "#9333EA", // Purple
        "#DB2777", // Pink
        "#0D9488", // Teal
        "#71717A"  // Grey
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                ShCard {
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShSegmentedControl(
                        items = listOf("$", "€", "£", "¥"),
                        selectedIndex = when (currency) {
                            "$" -> 0
                            "€" -> 1
                            "£" -> 2
                            "¥" -> 3
                            else -> 0
                        },
                        onItemSelected = { index ->
                            val symbol = when (index) {
                                0 -> "$"
                                1 -> "€"
                                2 -> "£"
                                3 -> "¥"
                                else -> "$"
                            }
                            viewModel.saveCurrency(symbol)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Application Theme",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShSegmentedControl(
                        items = listOf("Light", "Dark", "System"),
                        selectedIndex = when (theme) {
                            "light" -> 0
                            "dark" -> 1
                            "system" -> 2
                            else -> 2
                        },
                        onItemSelected = { index ->
                            val themeStr = when (index) {
                                0 -> "light"
                                1 -> "dark"
                                2 -> "system"
                                else -> "system"
                            }
                            viewModel.saveTheme(themeStr)
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                ShCard {
                    Text(
                        text = "Add Customized Category",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ShTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = "Category Name",
                        placeholder = "e.g., Subscriptions"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Category Type",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShSegmentedControl(
                        items = listOf("Expense", "Income"),
                        selectedIndex = if (newCatType == TransactionType.EXPENSE) 0 else 1,
                        onItemSelected = { index ->
                            newCatType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = hex == newCatColor
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { newCatColor = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    ShButton(
                        text = "Create Category",
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                viewModel.addCategory(newCatName, newCatType, newCatColor)
                                newCatName = ""
                            }
                        },
                        enabled = newCatName.isNotBlank()
                    )
                }
            }

            item {
                Text(
                    text = "Manage Existing Categories",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(categories) { cat ->
                val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = if (cat.type == TransactionType.INCOME) "Income" else "Expense",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Category",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
