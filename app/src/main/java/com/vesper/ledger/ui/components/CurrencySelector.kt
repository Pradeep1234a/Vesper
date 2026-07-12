package com.vesper.ledger.ui.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

enum class CurrencySelectorMode {
    PERSONALIZATION,
    SETTINGS
}

data class CurrencyInfo(
    val code: String,
    val name: String,
    val country: String,
    val symbol: String
)

val currencyList = listOf(
    CurrencyInfo("AED", "UAE Dirham", "United Arab Emirates", "د.إ"),
    CurrencyInfo("ARS", "Argentine Peso", "Argentina", "$"),
    CurrencyInfo("AUD", "Australian Dollar", "Australia", "$"),
    CurrencyInfo("BDT", "Bangladeshi Taka", "Bangladesh", "৳"),
    CurrencyInfo("BRL", "Brazilian Real", "Brazil", "R$"),
    CurrencyInfo("CAD", "Canadian Dollar", "Canada", "CA$"),
    CurrencyInfo("CHF", "Swiss Franc", "Switzerland", "CHF"),
    CurrencyInfo("CNY", "Chinese Yuan", "China", "¥"),
    CurrencyInfo("DKK", "Danish Krone", "Denmark", "kr"),
    CurrencyInfo("EUR", "Euro", "European Union", "€"),
    CurrencyInfo("GBP", "British Pound", "United Kingdom", "£"),
    CurrencyInfo("ILS", "Israeli Shekel", "Israel", "₪"),
    CurrencyInfo("INR", "Indian Rupee", "India", "₹"),
    CurrencyInfo("JPY", "Japanese Yen", "Japan", "¥"),
    CurrencyInfo("MXN", "Mexican Peso", "Mexico", "$"),
    CurrencyInfo("NZD", "New Zealand Dollar", "New Zealand", "$"),
    CurrencyInfo("RUB", "Russian Ruble", "Russia", "₽"),
    CurrencyInfo("SAR", "Saudi Riyal", "Saudi Arabia", "ر.س"),
    CurrencyInfo("SGD", "Singapore Dollar", "Singapore", "$"),
    CurrencyInfo("USD", "US Dollar", "United States", "$"),
    CurrencyInfo("ZAR", "South African Rand", "South Africa", "R")
).sortedBy { it.code }

val popularCodes = listOf("USD", "EUR", "GBP", "INR", "AED", "JPY")

fun getRecentCurrencies(context: Context): List<String> {
    val prefs = context.getSharedPreferences("vesper_currency_recents", Context.MODE_PRIVATE)
    val raw = prefs.getString("recents", "") ?: ""
    if (raw.isEmpty()) return emptyList()
    return raw.split(",")
}

fun addRecentCurrency(context: Context, code: String) {
    val prefs = context.getSharedPreferences("vesper_currency_recents", Context.MODE_PRIVATE)
    val current = getRecentCurrencies(context).toMutableList()
    current.remove(code)
    current.add(0, code)
    if (current.size > 5) {
        current.removeAt(current.size - 1)
    }
    prefs.edit().putString("recents", current.joinToString(",")).apply()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CurrencySelectorScreen(
    mode: CurrencySelectorMode,
    currentSelection: String?, // Nullable for personalization initially
    onCurrencySelected: (String) -> Unit, // Instant save/close inside settings
    onContinueClick: (String) -> Unit, // Step 1 Continue confirm
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") } // "All", "Popular", "Recent"
    var tempSelection by remember { mutableStateOf(currentSelection) }

    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Fetch lists
    val recents = remember { getRecentCurrencies(context) }

    // Filter currencies based on search & active filter tab
    val filteredList = currencyList.filter { currency ->
        // Search filter
        val matchesSearch = currency.code.contains(searchQuery, ignoreCase = true) ||
                currency.name.contains(searchQuery, ignoreCase = true) ||
                currency.country.contains(searchQuery, ignoreCase = true)

        // Tab filter
        val matchesTab = when (activeFilter) {
            "Popular" -> popularCodes.contains(currency.code)
            "Recent" -> recents.contains(currency.code)
            else -> true
        }

        matchesSearch && matchesTab
    }

    // Alphabetical Groupings for filtered list
    val groupedCurrencies = filteredList.groupBy { it.code.first().uppercase() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = onBgColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Currency",
                        fontSize = 20.sp,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        color = onBgColor
                    )
                }

                Text(
                    text = "Choose the currency used for transactions, balances, reports, exports, and analytics.",
                    fontSize = 13.sp,
                    color = secTextColor,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search currency or country...", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, tint = secTextColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Compact Toggle Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                        .background(surfaceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("All" to Icons.Outlined.AccountBalanceWallet, "Popular" to Icons.Outlined.StarOutline, "Recent" to Icons.Outlined.AccessTime).forEach { (tab, icon) ->
                        val isActive = activeFilter == tab
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { activeFilter = tab },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else secTextColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else secTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Scrollable Currency Area
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Pinned Selected Currency
                tempSelection?.let { selectedCode ->
                    val selectedCurrency = currencyList.firstOrNull { it.code == selectedCode }
                    if (selectedCurrency != null) {
                        item {
                            Text(
                                text = "Selected Currency",
                                fontSize = 11.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = secTextColor,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                            CurrencySelectionRow(
                                currency = selectedCurrency,
                                isSelected = true,
                                onClick = {
                                    if (mode == CurrencySelectorMode.SETTINGS) {
                                        addRecentCurrency(context, selectedCurrency.code)
                                        onCurrencySelected(selectedCurrency.code)
                                        onBackClick()
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // 2. Alphabetical Sticky List Groupings
                groupedCurrencies.forEach { (letter, currencies) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = letter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secTextColor,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(outlineColor)
                                )
                            }
                        }
                    }

                    items(currencies) { currency ->
                        val isSelected = tempSelection == currency.code
                        CurrencySelectionRow(
                            currency = currency,
                            isSelected = isSelected,
                            onClick = {
                                tempSelection = currency.code
                                if (mode == CurrencySelectorMode.SETTINGS) {
                                    addRecentCurrency(context, currency.code)
                                    onCurrencySelected(currency.code)
                                    onBackClick()
                                }
                            }
                        )
                    }
                }
            }

            // Bottom Footer (Only in Onboarding Mode 1)
            if (mode == CurrencySelectorMode.PERSONALIZATION) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp)
                ) {
                    Button(
                        onClick = {
                            tempSelection?.let {
                                addRecentCurrency(context, it)
                                onContinueClick(it)
                            }
                        },
                        enabled = tempSelection != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Continue",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencySelectionRow(
    currency: CurrencyInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else outlineColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading symbol box
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(surfaceColor, RoundedCornerShape(8.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currency.symbol,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center info Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
            Text(
                text = "${currency.code} • ${currency.country}",
                fontSize = 12.sp,
                color = secTextColor
            )
        }

        // Trailing status indicator circle
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else outlineColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
