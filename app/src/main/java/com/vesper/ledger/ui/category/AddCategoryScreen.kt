package com.vesper.ledger.ui.category

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.*
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

// 14 luxury preset colors from the specifications
val LUXURY_COLORS = listOf(
    "#E5484D", // Red
    "#F57C00", // Orange
    "#E8A317", // Amber
    "#D4B000", // Yellow
    "#22A559", // Green
    "#00A676", // Emerald
    "#0FA3B1", // Teal
    "#3B82F6", // Blue
    "#5C6BC0", // Indigo
    "#8B5CF6", // Purple
    "#E84A9B", // Pink
    "#E85D75", // Rose
    "#8D6E63", // Brown
    "#607D8B"  // Slate
)

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
    
    // Track if user has manually chosen an icon, otherwise use intelligent fallback
    var userSelectedIcon by remember { mutableStateOf<String?>(null) }
    var selectedColorHex by remember { mutableStateOf("#DC2626") }
    var showIconSheet by remember { mutableStateOf(false) }

    // Load category if categoryId is provided (Edit Mode)
    LaunchedEffect(categoryId) {
        if (categoryId != null && !isInitialized) {
            viewModel.getCategory(categoryId) { cat ->
                if (cat != null) {
                    categoryToEdit = cat
                    selectedType = cat.type
                    nameText = cat.name
                    userSelectedIcon = cat.iconName
                    selectedColorHex = cat.colorHex
                }
                isInitialized = true
            }
        } else {
            isInitialized = true
        }
    }

    // Determine current icon (user selected icon or name-based fallback)
    val activeIconName = remember(nameText, userSelectedIcon) {
        userSelectedIcon ?: if (nameText.isBlank()) "category" else getFallbackIcon(nameText)
    }

    val selectedColor = safeParseColor(selectedColorHex)

    Scaffold(
        topBar = {
            ChildHeader(
                title = if (categoryId == null) "Add Category" else "Edit Category",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (!isInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 100.dp), // Clearance for pinned Save button
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. LIVE PREVIEW CARD
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Live Preview",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ShCard(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icon Container with chosen color tint accent
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, selectedColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(activeIconName),
                                        contentDescription = null,
                                        tint = selectedColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Name & Type Subtitle
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (nameText.isNotBlank()) nameText else "Category Name",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (nameText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (selectedType == TransactionType.EXPENSE) "Expense Category" else "Income Category",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }

                                // Chevron matching list structure
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // 2. CATEGORY NAME INPUT
                    ShTextField(
                        value = nameText,
                        onValueChange = { if (it.length <= 30) nameText = it },
                        label = "Category Name",
                        placeholder = "Enter category name"
                    )

                    // 3. TYPE SELECTOR
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Type",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ShSegmentedControl(
                            items = listOf("Expense", "Income"),
                            selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
                            onItemSelected = { index ->
                                selectedType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                            }
                        )
                    }

                    // 4. COLOR PICKER
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Color",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(LUXURY_COLORS) { hex ->
                                val color = safeParseColor(hex)
                                val isSelected = hex.equals(selectedColorHex, ignoreCase = true)
                                
                                val scale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "swatchScale"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .scale(scale)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }

                    // 5. ICON SELECTION TRIGGER
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Icon",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ShCard(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.clickable { showIconSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getIconByName(activeIconName),
                                            contentDescription = null,
                                            tint = selectedColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Select Icon",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // PINNED BOTTOM SAVE BUTTON
                val saveInteractionSource = remember { MutableInteractionSource() }
                val isSavePressed by saveInteractionSource.collectIsPressedAsState()
                val saveScale by animateFloatAsState(
                    targetValue = if (isSavePressed) 0.96f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "saveScale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    ShButton(
                        text = "Save Category",
                        onClick = {
                            if (nameText.isNotBlank()) {
                                if (categoryToEdit == null) {
                                    viewModel.addCategory(
                                        name = nameText,
                                        iconName = activeIconName,
                                        type = selectedType,
                                        colorHex = selectedColorHex
                                    )
                                } else {
                                    viewModel.updateCategory(
                                        categoryToEdit!!.copy(
                                            name = nameText,
                                            iconName = activeIconName,
                                            type = selectedType,
                                            colorHex = selectedColorHex
                                        )
                                    )
                                }
                                onBackClick()
                            }
                        },
                        enabled = nameText.isNotBlank(),
                        modifier = Modifier.scale(saveScale)
                    )
                }
            }
        }
    }

    // ICON SELECTION SYSTEM BOTTOM SHEET
    if (showIconSheet) {
        val iconsList = remember { ICON_CATEGORIES.values.flatten().distinct() }
        
        var iconSearchQuery by remember { mutableStateOf("") }
        val filteredIcons = remember(iconsList, iconSearchQuery) {
            if (iconSearchQuery.isBlank()) {
                iconsList
            } else {
                iconsList.filter { it.contains(iconSearchQuery.lowercase().trim()) }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showIconSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Select Icon",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Search Field
                OutlinedTextField(
                    value = iconSearchQuery,
                    onValueChange = { iconSearchQuery = it },
                    placeholder = { Text("Search icons...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                )

                // Scrollable 4 column icon grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredIcons) { iconName ->
                        val isSelected = iconName.equals(activeIconName, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) selectedColor.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) selectedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    userSelectedIcon = iconName
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconByName(iconName),
                                contentDescription = null,
                                tint = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Done Button (fixed at bottom)
                ShButton(
                    text = "Done",
                    onClick = { showIconSheet = false }
                )
            }
        }
    }
}

// Intelligent name-based fallback icons lookup
fun getFallbackIcon(name: String): String {
    val clean = name.lowercase().trim()
    return when {
        clean.contains("shop") || clean.contains("buy") || clean.contains("purchas") || clean.contains("store") || clean.contains("mall") -> "shopping_bag"
        clean.contains("food") || clean.contains("din") || clean.contains("eat") || clean.contains("restaur") || clean.contains("caf") || clean.contains("pizza") || clean.contains("coffee") -> "restaurant"
        clean.contains("fuel") || clean.contains("gas") || clean.contains("oil") || clean.contains("petrol") -> "gas_station"
        clean.contains("travel") || clean.contains("flight") || clean.contains("trip") || clean.contains("vacation") || clean.contains("plan") || clean.contains("tour") -> "flight"
        clean.contains("gift") || clean.contains("present") || clean.contains("donat") || clean.contains("charity") -> "gift"
        clean.contains("salary") || clean.contains("wage") || clean.contains("payday") || clean.contains("earn") -> "wallet"
        clean.contains("bill") || clean.contains("utilit") || clean.contains("electr") || clean.contains("water") || clean.contains("phone") || clean.contains("intern") || clean.contains("tax") -> "card"
        clean.contains("educat") || clean.contains("school") || clean.contains("uni") || clean.contains("colleg") || clean.contains("stud") || clean.contains("learn") || clean.contains("book") -> "school"
        clean.contains("health") || clean.contains("medic") || clean.contains("doctor") || clean.contains("fit") || clean.contains("gym") || clean.contains("care") || clean.contains("dentist") -> "heart"
        clean.contains("rent") || clean.contains("house") || clean.contains("home") || clean.contains("mortgag") || clean.contains("apart") -> "home"
        else -> "tag"
    }
}
