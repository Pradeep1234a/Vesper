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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.getIconByName
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

data class CategoryOption(
    val id: Long,
    val name: String,
    val iconName: String,
    val type: TransactionType,
    val description: String = ""
)

val DEFAULT_EXPENSE_CATEGORIES = listOf(
    CategoryOption(1L, "Groceries", "shopping_cart", TransactionType.EXPENSE, "Food, produce, daily essentials"),
    CategoryOption(2L, "Food & Dining", "restaurant", TransactionType.EXPENSE, "Restaurants, cafes, takeout"),
    CategoryOption(3L, "Electronics", "laptop", TransactionType.EXPENSE, "Gadgets, cables, hardware"),
    CategoryOption(4L, "Books & Stationery", "book", TransactionType.EXPENSE, "Books, notebooks, office supply"),
    CategoryOption(5L, "Clothing & Apparel", "checkroom", TransactionType.EXPENSE, "Clothes, shoes, accessories"),
    CategoryOption(6L, "Beauty & Care", "spa", TransactionType.EXPENSE, "Skincare, hair care, cosmetics"),
    CategoryOption(7L, "Health & Medical", "medical", TransactionType.EXPENSE, "Pharmacy, doctor, wellness"),
    CategoryOption(8L, "Home Supplies", "home", TransactionType.EXPENSE, "Furniture, cleaning, decor"),
    CategoryOption(9L, "Entertainment", "movie", TransactionType.EXPENSE, "Movies, games, events"),
    CategoryOption(10L, "Transportation", "car", TransactionType.EXPENSE, "Fuel, taxi, transit, tolls"),
    CategoryOption(11L, "Pets", "pets", TransactionType.EXPENSE, "Pet food, vet, accessories"),
    CategoryOption(12L, "Utilities & Bills", "receipt_long", TransactionType.EXPENSE, "Electricity, water, internet"),
    CategoryOption(13L, "Subscriptions", "card", TransactionType.EXPENSE, "Software, streaming services"),
    CategoryOption(14L, "General Expense", "category", TransactionType.EXPENSE, "Miscellaneous spending")
)

val DEFAULT_INCOME_CATEGORIES = listOf(
    CategoryOption(101L, "Salary", "work", TransactionType.INCOME, "Monthly payroll, wages"),
    CategoryOption(102L, "Freelance & Business", "laptop", TransactionType.INCOME, "Client work, consulting"),
    CategoryOption(103L, "Investments", "trending_up", TransactionType.INCOME, "Dividends, stock returns"),
    CategoryOption(104L, "Bonus & Incentives", "gift", TransactionType.INCOME, "Performance bonus, prize"),
    CategoryOption(105L, "Interest Income", "bank", TransactionType.INCOME, "Bank savings interest"),
    CategoryOption(106L, "Gifts & Grants", "gift", TransactionType.INCOME, "Cash gifts, allowances"),
    CategoryOption(107L, "Rental Income", "home", TransactionType.INCOME, "Property rent, leasing"),
    CategoryOption(108L, "Side Hustle", "bolt", TransactionType.INCOME, "Gig economy, secondary sales"),
    CategoryOption(109L, "Cashback & Refunds", "payments", TransactionType.INCOME, "Reimbursements, cashbacks"),
    CategoryOption(110L, "Other Income", "money", TransactionType.INCOME, "Miscellaneous earnings")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    initialType: TransactionType = TransactionType.EXPENSE,
    selectedCategoryName: String,
    onBackClick: () -> Unit,
    onCategorySelected: (CategoryOption) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val categoriesToDisplay = remember(initialType, searchQuery) {
        val list = if (initialType == TransactionType.INCOME) DEFAULT_INCOME_CATEGORIES else DEFAULT_EXPENSE_CATEGORIES
        if (searchQuery.isBlank()) {
            list
        } else {
            list.filter { it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Select ${if (initialType == TransactionType.INCOME) "Income" else "Expense"} Category",
                onBackClick = onBackClick
            )
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
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(categoriesToDisplay) { cat ->
                    val isSelected = cat.name.equals(selectedCategoryName, ignoreCase = true)

                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onCategorySelected(cat) },
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
                                            if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(cat.iconName),
                                        contentDescription = cat.name,
                                        tint = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
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
