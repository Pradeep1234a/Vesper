package com.vesper.ledger.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
            .background(Color(0xFF090D16)) // slate-950 deep dark
    ) {
        // Tech Grid Background matching Onboarding exactly
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 48.dp.toPx()
            val strokeColor = Color(0x0A94A3B8)
            val width = size.width
            val height = size.height

            var x = 0f
            while (x < width) {
                drawLine(strokeColor, Offset(x, 0f), Offset(x, height), 1.dp.toPx())
                x += gridSpacing
            }

            var y = 0f
            while (y < height) {
                drawLine(strokeColor, Offset(0f, y), Offset(width, y), 1.dp.toPx())
                y += gridSpacing
            }
        }

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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 3.sp
                )
            }

            // Shadcn Portal Setup Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF1E293B), // slate-800 border
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x2B0F172A) // slate-900 transparent fill
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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Enter your settings parameters below. All data resides offline on your sqlite storage.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B), // slate-500
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
                                color = Color(0xFF94A3B8) // slate-400
                            )
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("e.g. Keshav", color = Color(0xFF475569)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White, // pure white highlight focus
                                    unfocusedBorderColor = Color(0xFF1E293B), // slate-800
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
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
                                color = Color(0xFF94A3B8)
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
                                            .height(46.dp)
                                            .background(
                                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedCurrency = symbol },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
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
                                color = Color(0xFF94A3B8)
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
                                            .height(46.dp)
                                            .background(
                                                color = if (isSelected) Color.White else Color(0xFF0F172A),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedTheme = theme },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = theme,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8)
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
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // pure white high contrast button
                        contentColor = Color(0xFF0F172A)
                    )
                ) {
                    Text(
                        text = "Complete Setup",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
