package com.vesper.ledger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

data class CurrencyInfo(
    val code: String,
    val name: String,
    val country: String,
    val flag: String
)

val popularCurrencies = listOf(
    CurrencyInfo("USD", "US Dollar", "United States", "🇺🇸"),
    CurrencyInfo("EUR", "Euro", "European Union", "🇪🇺"),
    CurrencyInfo("GBP", "British Pound", "United Kingdom", "🇬🇧"),
    CurrencyInfo("INR", "Indian Rupee", "India", "🇮🇳"),
    CurrencyInfo("AED", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
    CurrencyInfo("JPY", "Japanese Yen", "Japan", "🇯🇵")
)

val allCurrencies = listOf(
    CurrencyInfo("USD", "US Dollar", "United States", "🇺🇸"),
    CurrencyInfo("EUR", "Euro", "European Union", "🇪🇺"),
    CurrencyInfo("GBP", "British Pound", "United Kingdom", "🇬🇧"),
    CurrencyInfo("INR", "Indian Rupee", "India", "🇮🇳"),
    CurrencyInfo("AED", "UAE Dirham", "United Arab Emirates", "🇦🇪"),
    CurrencyInfo("JPY", "Japanese Yen", "Japan", "🇯🇵"),
    CurrencyInfo("CAD", "Canadian Dollar", "Canada", "🇨🇦"),
    CurrencyInfo("AUD", "Australian Dollar", "Australia", "🇦🇺"),
    CurrencyInfo("CHF", "Swiss Franc", "Switzerland", "🇨🇭"),
    CurrencyInfo("CNY", "Chinese Yuan", "China", "🇨🇳"),
    CurrencyInfo("SGD", "Singapore Dollar", "Singapore", "🇸🇬"),
    CurrencyInfo("NZD", "New Zealand Dollar", "New Zealand", "🇳🇿")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    selectedCode: String,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    val filteredPopular = popularCurrencies.filter {
        it.code.contains(searchQuery, ignoreCase = true) ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true)
    }

    val filteredAll = allCurrencies.filter {
        it.code.contains(searchQuery, ignoreCase = true) ||
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.country.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search currency, code or country...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = outlineColor
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredPopular.isNotEmpty() && searchQuery.isEmpty()) {
                item {
                    Text(
                        text = "Popular Currencies",
                        fontSize = 11.sp,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }

                items(filteredPopular) { currency ->
                    CurrencyRow(
                        currency = currency,
                        isSelected = selectedCode == currency.code,
                        onClick = { onCurrencySelected(currency.code) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                Text(
                    text = "All Currencies",
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }

            items(filteredAll) { currency ->
                CurrencyRow(
                    currency = currency,
                    isSelected = selectedCode == currency.code,
                    onClick = { onCurrencySelected(currency.code) }
                )
            }
        }
    }
}

@Composable
fun CurrencyRow(
    currency: CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else outlineColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(surfaceColor, RoundedCornerShape(6.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = currency.flag, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Name info
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = currency.code,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currency.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = currency.country,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
