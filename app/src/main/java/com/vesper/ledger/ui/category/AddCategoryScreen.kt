package com.vesper.ledger.ui.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
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
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    viewModel: CategoryViewModel,
    categoryId: Long?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var nameText by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("restaurant") }
    var selectedColorHex by remember { mutableStateOf("#EF4444") }

    // Load category if categoryId is provided (Edit Mode)
    LaunchedEffect(categoryId) {
        if (categoryId != null && !isInitialized) {
            viewModel.getCategory(categoryId) { cat ->
                if (cat != null) {
                    categoryToEdit = cat
                    selectedType = cat.type
                    nameText = cat.name
                    selectedIcon = cat.iconName
                    selectedColorHex = cat.colorHex
                }
                isInitialized = true
            }
        } else {
            isInitialized = true
        }
    }

    // Color picker states
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var hexInput by remember { mutableStateOf(selectedColorHex) }
    var rInput by remember { mutableStateOf("") }
    var gInput by remember { mutableStateOf("") }
    var bInput by remember { mutableStateOf("") }

    // Synchronize hexInput when selectedColorHex changes
    LaunchedEffect(selectedColorHex) {
        hexInput = selectedColorHex
        val rgb = hexToRgb(selectedColorHex)
        if (rgb != null) {
            rInput = rgb.first.toString()
            gInput = rgb.second.toString()
            bInput = rgb.third.toString()
        }
    }

    // Icon search and categories
    var iconSearchQuery by remember { mutableStateOf("") }
    var selectedIconCategory by remember { mutableStateOf("Food") }

    val scrollState = rememberScrollState()

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ChildHeader(
                title = if (categoryId == null) "Add Category" else "Edit Category",
                onBackClick = onBackClick,
                actions = {
                    TextButton(
                        onClick = {
                            if (nameText.isNotBlank()) {
                                if (categoryToEdit == null) {
                                    viewModel.addCategory(
                                        name = nameText,
                                        iconName = selectedIcon,
                                        type = selectedType,
                                        colorHex = selectedColorHex
                                    )
                                } else {
                                    val updated = categoryToEdit!!.copy(
                                        name = nameText,
                                        iconName = selectedIcon,
                                        type = selectedType,
                                        colorHex = selectedColorHex
                                    )
                                    viewModel.updateCategory(updated)
                                }
                                onBackClick()
                            }
                        },
                        enabled = nameText.isNotBlank()
                    ) {
                        Text(
                            text = "Save",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (nameText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            )

            if (!isInitialized) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                // 1. Category Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Category Type",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Expense Tab
                        val isExpense = selectedType == TransactionType.EXPENSE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isExpense) Color(0xFFEF4444) else Color.Transparent
                               )
                                .clickable { selectedType = TransactionType.EXPENSE },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Expense",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Income Tab
                        val isIncome = selectedType == TransactionType.INCOME
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    if (isIncome) Color(0xFF10B981) else Color.Transparent
                                )
                                .clickable { selectedType = TransactionType.INCOME },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Income",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Category Name Input (Acts as preview)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. Category Name",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { if (it.length <= 50) nameText = it },
                        placeholder = { Text("e.g., Food & Dining", fontFamily = PlusJakartaSansFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(safeParseColor(selectedColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconByName(selectedIcon),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        trailingIcon = {
                            Text(
                                text = "${nameText.length}/50",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // 3. Choose Icon Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "3. Choose Icon",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    // Icon Search Bar
                    OutlinedTextField(
                        value = iconSearchQuery,
                        onValueChange = { iconSearchQuery = it },
                        placeholder = { Text("Search 500+ icons...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )

                    if (iconSearchQuery.isEmpty()) {
                        // Category Groups Row (Tabs)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(ICON_CATEGORIES.keys.toList()) { group ->
                                val isSelected = group == selectedIconCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedIconCategory = group },
                                    label = { Text(group, fontFamily = SpaceGroteskFamily) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    // Grid of icons matching group or search query
                    val filteredIcons = remember(iconSearchQuery, selectedIconCategory) {
                        if (iconSearchQuery.isNotEmpty()) {
                            // Find all icons across all groups matching search query
                            ICON_CATEGORIES.values.flatten().distinct().filter {
                                it.contains(iconSearchQuery, ignoreCase = true)
                            }
                        } else {
                            ICON_CATEGORIES[selectedIconCategory] ?: emptyList()
                        }
                    }

                    // Static height container for Grid to prevent infinite nested scrolling crash
                    Box(modifier = Modifier.height(180.dp)) {
                        if (filteredIcons.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No icons found", fontFamily = PlusJakartaSansFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredIcons) { iconName ->
                                    val isSelected = iconName == selectedIcon
                                    Box(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedIcon = iconName },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(iconName),
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Choose Color Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "4. Choose Color",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    // Color presets row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PRESET_COLORS.take(8).forEach { hex ->
                            val color = safeParseColor(hex)
                            val isSelected = hex == selectedColorHex && !showCustomColorPicker
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColorHex = hex
                                        showCustomColorPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Add Custom color dot (+)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (showCustomColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                    .clickable { showCustomColorPicker = !showCustomColorPicker },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Custom Color",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Custom Color Picker (HEX & RGB Inputs)
                    if (showCustomColorPicker) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Custom Color Selector",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Live Color Box
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(safeParseColor(selectedColorHex))
                                    )

                                    // HEX input field
                                    OutlinedTextField(
                                        value = hexInput,
                                        onValueChange = {
                                            hexInput = it
                                            val validHex = if (it.startsWith("#")) it else "#$it"
                                            if (it.length == 7 || (it.length == 6 && !it.startsWith("#"))) {
                                                val rgb = hexToRgb(validHex)
                                                if (rgb != null) {
                                                    selectedColorHex = validHex
                                                    rInput = rgb.first.toString()
                                                    gInput = rgb.second.toString()
                                                    bInput = rgb.third.toString()
                                                }
                                            }
                                        },
                                        label = { Text("HEX", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1.2f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }

                                // RGB Input fields
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Red channel input
                                    OutlinedTextField(
                                        value = rInput,
                                        onValueChange = {
                                            rInput = it
                                            val rVal = it.toIntOrNull()
                                            val gVal = gInput.toIntOrNull() ?: 0
                                            val bVal = bInput.toIntOrNull() ?: 0
                                            if (rVal != null && rVal in 0..255) {
                                                selectedColorHex = rgbToHex(rVal, gVal, bVal)
                                            }
                                        },
                                        label = { Text("R", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    // Green channel input
                                    OutlinedTextField(
                                        value = gInput,
                                        onValueChange = {
                                            gInput = it
                                            val rVal = rInput.toIntOrNull() ?: 0
                                            val gVal = it.toIntOrNull()
                                            val bVal = bInput.toIntOrNull() ?: 0
                                            if (gVal != null && gVal in 0..255) {
                                                selectedColorHex = rgbToHex(rVal, gVal, bVal)
                                            }
                                        },
                                        label = { Text("G", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    // Blue channel input
                                    OutlinedTextField(
                                        value = bInput,
                                        onValueChange = {
                                            bInput = it
                                            val rVal = rInput.toIntOrNull() ?: 0
                                            val gVal = gInput.toIntOrNull() ?: 0
                                            val bVal = it.toIntOrNull()
                                            if (bVal != null && bVal in 0..255) {
                                                selectedColorHex = rgbToHex(rVal, gVal, bVal)
                                            }
                                        },
                                        label = { Text("B", fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
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

// Helper functions for HEX/RGB translation
fun rgbToHex(r: Int, g: Int, b: Int): String {
    return String.format(Locale.US, "#%02X%02X%02X", r, g, b)
}

fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
    val cleanHex = hex.replace("#", "")
    if (cleanHex.length != 6) return null
    return try {
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)
        Triple(r, g, b)
    } catch (e: Exception) {
        null
    }
}
