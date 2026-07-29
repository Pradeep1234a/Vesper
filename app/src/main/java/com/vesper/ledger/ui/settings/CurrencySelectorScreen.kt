package com.vesper.ledger.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.CurrencyData
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

enum class CurrencyFlowMode {
    ONBOARDING,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorScreen(
    viewModel: SettingsViewModel,
    flowMode: CurrencyFlowMode = CurrencyFlowMode.SETTINGS,
    onBackClick: () -> Unit = {},
    onCompleteOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentSymbol by viewModel.currencySymbol.collectAsState()
    val currentCode by viewModel.currencyCode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCurrency by remember {
        mutableStateOf(
            CurrencyData.currencies.find { it.code == currentCode || it.symbol == currentSymbol }
                ?: CurrencyData.defaultCurrency
        )
    }

    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CurrencyData.currencies
        } else {
            val query = searchQuery.trim().lowercase()
            CurrencyData.currencies.filter {
                it.country.lowercase().contains(query) ||
                        it.name.lowercase().contains(query) ||
                        it.code.lowercase().contains(query) ||
                        it.symbol.lowercase().contains(query)
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            com.vesper.ledger.ui.components.ChildHeader(
                title = if (flowMode == CurrencyFlowMode.ONBOARDING) "Select Currency" else "Currency",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .navigationBarsPadding(),
                color = backgroundColor,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.saveCurrency(selectedCurrency.symbol, selectedCurrency.code)
                            Toast.makeText(
                                context,
                                "Currency set to ${selectedCurrency.code} (${selectedCurrency.symbol})",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (flowMode == CurrencyFlowMode.ONBOARDING) {
                                onCompleteOnboarding()
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textColorPrimary,
                            contentColor = backgroundColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Save",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (flowMode == CurrencyFlowMode.ONBOARDING) "SAVE & CONTINUE" else "SAVE CURRENCY",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(backgroundColor)
                .padding(top = 8.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        text = "Search country, currency or symbol...",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 13.sp,
                        color = textColorSecondary.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = textColorSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = textColorSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = textColorPrimary,
                    unfocusedBorderColor = outlineColor,
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedTextColor = textColorPrimary,
                    unfocusedTextColor = textColorPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Active Selected Currency Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(surfaceColor)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(textColorPrimary.copy(alpha = 0.08f))
                                .border(1.dp, outlineColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedCurrency.flagEmoji,
                                fontSize = 20.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedCurrency.country,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColorPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "(${selectedCurrency.code})",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "${selectedCurrency.name} • Symbol: ${selectedCurrency.symbol}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                color = textColorSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SELECTED",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AVAILABLE CURRENCIES",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = textColorSecondary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Currency List (Occupies remaining vertical space cleanly)
            if (filteredCurrencies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = textColorSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No currencies match \"$searchQuery\"",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 13.sp,
                            color = textColorSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCurrencies, key = { it.code + it.country }) { item ->
                        val isSelected = item.code == selectedCurrency.code

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else outlineColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedCurrency = item
                                    if (flowMode == CurrencyFlowMode.SETTINGS) {
                                        viewModel.saveCurrency(item.symbol, item.code)
                                        Toast.makeText(
                                            context,
                                            "Currency set to ${item.country} (${item.code} ${item.symbol})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) surfaceColor else surfaceColor.copy(alpha = 0.6f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(textColorPrimary.copy(alpha = 0.08f))
                                            .border(1.dp, outlineColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.flagEmoji,
                                            fontSize = 18.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = item.country,
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = textColorPrimary
                                        )
                                        Text(
                                            text = "${item.name} (${item.code})",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 11.sp,
                                            color = textColorSecondary
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(textColorPrimary.copy(alpha = 0.08f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = item.symbol,
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = textColorPrimary
                                        )
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
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
}
