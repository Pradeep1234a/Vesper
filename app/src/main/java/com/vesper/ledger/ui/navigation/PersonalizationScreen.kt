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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    viewModel: SettingsViewModel,
    onSetupComplete: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("$") }
    val context = LocalContext.current

    val currencyOptions = listOf(
        "$" to "USD ($)",
        "€" to "EUR (€)",
        "£" to "GBP (£)",
        "¥" to "JPY (¥)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // slate-900
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Personalize Your Ledger",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "A few details to tailor your experience. All data stays offline.",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8), // slate-400
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            // Inputs Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Name Field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0) // slate-200
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        placeholder = { Text("e.g., Keshav", color = Color(0xFF64748B)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981), // Emerald
                            unfocusedBorderColor = Color(0xFF334155), // slate-700
                            focusedPlaceholderColor = Color(0xFF64748B),
                            unfocusedPlaceholderColor = Color(0xFF64748B),
                            focusedContainerColor = Color(0xFF1E293B), // slate-800
                            unfocusedContainerColor = Color(0xFF1E293B)
                        )
                    )
                }

                // Currency Field Selector Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Primary Currency",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E8F0)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyOptions.forEach { (symbol, label) ->
                            val isSelected = selectedCurrency == symbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .background(
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF334155),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCurrency = symbol },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = symbol,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Setup button
            Button(
                onClick = {
                    val finalName = nameInput.trim()
                    if (finalName.isBlank()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveUserName(finalName)
                        viewModel.saveCurrency(selectedCurrency)
                        viewModel.saveFirstLaunch(false)
                        onSetupComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981) // Emerald CTA
                )
            ) {
                Text(
                    text = "Complete Setup",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
