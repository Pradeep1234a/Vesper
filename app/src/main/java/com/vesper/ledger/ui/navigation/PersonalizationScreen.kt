package com.vesper.ledger.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    viewModel: SettingsViewModel,
    onSetupComplete: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("$") }
    var selectedTheme by remember { mutableStateOf("System") }
    val context = LocalContext.current

    val currencyOptions = listOf("$", "€", "£", "¥")
    val themeOptions = listOf("Light", "Dark", "System")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // Zinc-950 (Shadcn Dark background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VESPER",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAFAFA), // Zinc-50
                    letterSpacing = 3.sp
                )
            }

            // Shadcn Strict Card Container
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF27272A), // Zinc-800 border
                        shape = RoundedCornerShape(8.dp) // Strict 8dp corner radius
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF09090B) // Zinc-950 content fill
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Access Portal",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA), // Zinc-50
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Configure your parameters below. All data resides offline on local sqlite database.",
                            fontSize = 13.sp,
                            color = Color(0xFFA1A1AA), // Zinc-400
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }

                    // Form Fields Group
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Name Input Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Name / Alias",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE4E4E7) // Zinc-200
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("e.g. Keshav", color = Color(0xFF71717A)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp), // Strict 8dp
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFAFAFA), // Zinc-50 focus highlight
                                    unfocusedBorderColor = Color(0xFF27272A), // Zinc-800
                                    focusedContainerColor = Color(0xFF09090B),
                                    unfocusedContainerColor = Color(0xFF09090B)
                                )
                            )
                        }

                        // Currency Selector Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Base Currency",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE4E4E7)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                currencyOptions.forEach { symbol ->
                                    val isSelected = selectedCurrency == symbol
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .background(
                                                color = if (isSelected) Color(0xFFFAFAFA) else Color(0xFF09090B),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFFFAFAFA) else Color(0xFF27272A),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedCurrency = symbol },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF09090B) else Color(0xFFA1A1AA)
                                        )
                                    }
                                }
                            }
                        }

                        // Theme Selector Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Default Mode",
                                fontSize = 12.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE4E4E7)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                themeOptions.forEach { theme ->
                                    val isSelected = selectedTheme == theme
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .background(
                                                color = if (isSelected) Color(0xFFFAFAFA) else Color(0xFF09090B),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFFFAFAFA) else Color(0xFF27272A),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedTheme = theme },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = theme,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF09090B) else Color(0xFFA1A1AA)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Setup Trigger Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        val finalName = nameInput.trim()
                        if (finalName.isBlank()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.saveUserName(finalName)
                            viewModel.saveCurrency(selectedCurrency)
                            viewModel.saveTheme(selectedTheme.lowercase())
                            viewModel.saveFirstLaunch(false)
                            onSetupComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF27272A),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp), // Strict 8dp corner radius
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFAFAFA), // Solid white background
                        contentColor = Color(0xFF09090B) // Pure black text
                    )
                ) {
                    Text(
                        text = "Complete Setup",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
