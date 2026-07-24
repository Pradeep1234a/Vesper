package com.vesper.ledger.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.components.safeParseColor
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

data class CategoryOption(
    val id: Long,
    val name: String,
    val iconName: String,
    val type: TransactionType,
    val colorHex: String = "#2563EB",
    val description: String = ""
)

val DEFAULT_EXPENSE_CATEGORIES = listOf(
    CategoryOption(1L, "Groceries", "shopping_cart", TransactionType.EXPENSE, "#10B981", "Food, produce, daily essentials"),
    CategoryOption(2L, "Food & Dining", "restaurant", TransactionType.EXPENSE, "#EF4444", "Restaurants, cafes, takeout"),
    CategoryOption(3L, "Electronics", "laptop", TransactionType.EXPENSE, "#3B82F6", "Gadgets, cables, hardware"),
    CategoryOption(4L, "Books & Stationery", "book", TransactionType.EXPENSE, "#F97316", "Books, notebooks, office supply"),
    CategoryOption(5L, "Clothing & Apparel", "checkroom", TransactionType.EXPENSE, "#EC4899", "Clothes, shoes, accessories"),
    CategoryOption(6L, "Beauty & Care", "spa", TransactionType.EXPENSE, "#8B5CF6", "Skincare, hair care, cosmetics"),
    CategoryOption(7L, "Health & Medical", "medical", TransactionType.EXPENSE, "#14B8A6", "Pharmacy, doctor, wellness"),
    CategoryOption(8L, "Home Supplies", "home", TransactionType.EXPENSE, "#64748B", "Furniture, cleaning, decor"),
    CategoryOption(9L, "Entertainment", "movie", TransactionType.EXPENSE, "#A855F7", "Movies, games, events"),
    CategoryOption(10L, "Transportation", "car", TransactionType.EXPENSE, "#F59E0B", "Fuel, taxi, transit, tolls"),
    CategoryOption(11L, "Pets", "pets", TransactionType.EXPENSE, "#06B6D4", "Pet food, vet, accessories"),
    CategoryOption(12L, "Utilities & Bills", "receipt_long", TransactionType.EXPENSE, "#EF4444", "Electricity, water, internet"),
    CategoryOption(13L, "Subscriptions", "card", TransactionType.EXPENSE, "#3B82F6", "Software, streaming services"),
    CategoryOption(14L, "General Expense", "category", TransactionType.EXPENSE, "#64748B", "Miscellaneous spending")
)

val DEFAULT_INCOME_CATEGORIES = listOf(
    CategoryOption(101L, "Salary", "work", TransactionType.INCOME, "#10B981", "Monthly payroll, wages"),
    CategoryOption(102L, "Freelance & Business", "laptop", TransactionType.INCOME, "#3B82F6", "Client work, consulting"),
    CategoryOption(103L, "Investments", "trending_up", TransactionType.INCOME, "#8B5CF6", "Dividends, stock returns"),
    CategoryOption(104L, "Bonus & Incentives", "gift", TransactionType.INCOME, "#F97316", "Performance bonus, prize"),
    CategoryOption(105L, "Interest Income", "bank", TransactionType.INCOME, "#06B6D4", "Bank savings interest"),
    CategoryOption(106L, "Gifts & Grants", "gift", TransactionType.INCOME, "#EC4899", "Cash gifts, allowances"),
    CategoryOption(107L, "Rental Income", "home", TransactionType.INCOME, "#F59E0B", "Property rent, leasing"),
    CategoryOption(108L, "Side Hustle", "bolt", TransactionType.INCOME, "#14B8A6", "Gig economy, secondary sales"),
    CategoryOption(109L, "Cashback & Refunds", "payments", TransactionType.INCOME, "#10B981", "Reimbursements, cashbacks"),
    CategoryOption(110L, "Other Income", "money", TransactionType.INCOME, "#64748B", "Miscellaneous earnings")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    categoriesList: List<Category> = emptyList(),
    initialType: TransactionType = TransactionType.EXPENSE,
    selectedCategoryName: String,
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit = {},
    onCategorySelected: (CategoryOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val categoryList = remember(categoriesList, initialType) {
        val dbFiltered = categoriesList.filter { it.type == initialType }.map { cat ->
            CategoryOption(
                id = cat.id,
                name = cat.name,
                iconName = cat.iconName,
                type = cat.type,
                colorHex = cat.colorHex,
                description = "${cat.type.name.lowercase().replaceFirstChar { it.uppercase() }} category"
            )
        }
        if (dbFiltered.isNotEmpty()) dbFiltered
        else if (initialType == TransactionType.INCOME) DEFAULT_INCOME_CATEGORIES
        else DEFAULT_EXPENSE_CATEGORIES
    }

    val initialSelected = remember(selectedCategoryName, categoryList) {
        categoryList.find { it.name.equals(selectedCategoryName, ignoreCase = true) } ?: categoryList.first()
    }

    var selectedCat by remember { mutableStateOf(initialSelected) }

    val categoriesToDisplay = remember(categoryList, searchQuery) {
        if (searchQuery.isBlank()) {
            categoryList
        } else {
            categoryList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Select ${if (initialType == TransactionType.INCOME) "Income" else "Expense"} Category",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = onAddCategoryClick) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Category")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ShButton(
                        text = "Select Category",
                        onClick = { onCategorySelected(selectedCat) },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Category Search Input Field (Shadcn UI style)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search category name or keyword...", fontFamily = SpaceGroteskFamily) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )

            // Category Grid Selection
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categoriesToDisplay) { cat ->
                    val isSelected = cat.id == selectedCat.id
                    val catColor = safeParseColor(cat.colorHex)

                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) catColor else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCat = cat },
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) catColor else catColor.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) Color.White else catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = cat.name,
                                        style = TextStyle(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = cat.description,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.size(12.dp)
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
