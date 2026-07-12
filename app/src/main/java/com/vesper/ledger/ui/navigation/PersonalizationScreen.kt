package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.components.CurrencySelector
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    viewModel: SettingsViewModel,
    onSetupComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    // Temporary variables matching steps to update view model on finish
    var selectedCurrency by remember { mutableStateOf("$") }
    var selectedTheme by remember { mutableStateOf("system") }
    var selectedAccent by remember { mutableStateOf("rose") }
    var selectedStyle by remember { mutableStateOf("comfortable") }
    var selectedStartScreen by remember { mutableStateOf("dashboard") }

    // Notifications
    var dailyRem by remember { mutableStateOf(false) }
    var missedEntryRem by remember { mutableStateOf(false) }
    var weeklySummaryRem by remember { mutableStateOf(false) }
    var monthlySummaryRem by remember { mutableStateOf(false) }

    // Security
    var biometricUnlock by remember { mutableStateOf(false) }

    val totalSteps = 7
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with steps
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VESPER",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Step $currentStep of $totalSteps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(surfaceColor, RoundedCornerShape(1.5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(currentStep.toFloat() / totalSteps)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
                    )
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when (currentStep) {
                    1 -> StepCurrency(selectedCurrency) { selectedCurrency = it }
                    2 -> StepTheme(selectedTheme) { selectedTheme = it }
                    3 -> StepAccentColor(selectedAccent) { selectedAccent = it }
                    4 -> StepStyle(selectedStyle) { selectedStyle = it }
                    5 -> StepStartScreen(selectedStartScreen) { selectedStartScreen = it }
                    6 -> StepNotifications(
                        dailyRem, { dailyRem = it },
                        missedEntryRem, { missedEntryRem = it },
                        weeklySummaryRem, { weeklySummaryRem = it },
                        monthlySummaryRem, { monthlySummaryRem = it }
                    )
                    7 -> StepSecurity(biometricUnlock) { biometricUnlock = it }
                }
            }

            // Bottom Actions (Navigation)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("Back", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            // Persist all choices to settings view model
                            viewModel.saveCurrency(selectedCurrency)
                            viewModel.saveTheme(selectedTheme)
                            viewModel.saveAccentColor(selectedAccent)
                            viewModel.saveAppStyle(selectedStyle)
                            viewModel.saveStartScreen(selectedStartScreen)
                            viewModel.saveDailyReminder(dailyRem)
                            viewModel.saveMissedEntryReminder(missedEntryRem)
                            viewModel.saveWeeklySummaryReminder(weeklySummaryRem)
                            viewModel.saveMonthlySummaryReminder(monthlySummaryRem)
                            viewModel.saveBiometricAuth(biometricUnlock)
                            viewModel.saveFirstLaunch(false)

                            onSetupComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (currentStep == totalSteps) "Complete Setup" else "Continue",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StepCurrency(selected: String, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Choose Currency", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Set the primary base monetary symbol for ledger balance valuations.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        CurrencySelector(selectedCode = selected, onCurrencySelected = onSelect)
    }
}

@Composable
fun StepTheme(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("light" to "Light Mode", "dark" to "Dark Mode", "system" to "System Preference")
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Choose Theme", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Adjust Vesper's user interface color contrast theme.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (key, label) ->
                val isActive = selected == key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(
                            width = 1.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else outlineColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(key) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (isActive) {
                        Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StepAccentColor(selected: String, onSelect: (String) -> Unit) {
    val accents = listOf(
        "emerald" to "Emerald Green",
        "blue" to "Vibrant Blue",
        "purple" to "Deep Purple",
        "orange" to "Warm Orange",
        "rose" to "Rose Pink"
    )
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Accent Color", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Select an accent color palette to customize controls and actions.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Live preview widget
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Live Preview Widget", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("$2,450.00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                }
                // Custom accent preview progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(3.dp))
                ) {
                    val previewColor = when (selected) {
                        "emerald" -> Color(0xFF10B981)
                        "blue" -> Color(0xFF3B82F6)
                        "purple" -> Color(0xFF8B5CF6)
                        "orange" -> Color(0xFFF97316)
                        else -> Color(0xFFF43F5E) // rose
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight()
                            .background(previewColor, RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Accents Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            accents.forEach { (key, name) ->
                val isActive = selected == key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            width = 1.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else outlineColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(key) }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val colorCircle = when (key) {
                            "emerald" -> Color(0xFF10B981)
                            "blue" -> Color(0xFF3B82F6)
                            "purple" -> Color(0xFF8B5CF6)
                            "orange" -> Color(0xFFF97316)
                            else -> Color(0xFFF43F5E)
                        }
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(colorCircle, RoundedCornerShape(4.dp))
                        )
                        Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    if (isActive) {
                        Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StepStyle(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "minimal" to ("Minimal" to "Maximum space, minimal gaps, compact cards."),
        "comfortable" to ("Comfortable" to "Standard spacing and padding grids."),
        "compact" to ("Compact" to "Tighter vertical rows for high-density lists.")
    )
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("App Density Style", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Choose visual density spacing rules for lists and paddings.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        options.forEach { (key, pair) ->
            val isActive = selected == key
            val (title, desc) = pair
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else outlineColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(key) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isActive) {
                    Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StepStartScreen(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "dashboard" to "Dashboard Overview",
        "transactions" to "Transaction Logs",
        "analysis" to "Deep Reports Analysis"
    )
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Start Screen Path", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Choose which portal displays automatically upon opening the application.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        options.forEach { (key, label) ->
            val isActive = selected == key
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else outlineColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .background(
                        color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(key) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                if (isActive) {
                    Icon(Icons.Outlined.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StepNotifications(
    daily: Boolean, onDailyChange: (Boolean) -> Unit,
    missed: Boolean, onMissedChange: (Boolean) -> Unit,
    weekly: Boolean, onWeeklyChange: (Boolean) -> Unit,
    monthly: Boolean, onMonthlyChange: (Boolean) -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Notifications", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Set up alerts and reminders to keep your ledgers updated.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NotificationSwitchRow("Daily Reminder", "Prompt to add entry every evening.", daily, onDailyChange)
            NotificationSwitchRow("Missed Entry Alert", "Alert if no entry logged for 24 hours.", missed, onMissedChange)
            NotificationSwitchRow("Weekly Summary", "Get weekly spending breakdown reports.", weekly, onWeeklyChange)
            NotificationSwitchRow("Monthly summary", "Get monthly budget reviews.", monthly, onMonthlyChange)
        }
    }
}

@Composable
fun NotificationSwitchRow(title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun StepSecurity(biometric: Boolean, onBiometricChange: (Boolean) -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Security & Privacy", fontFamily = SpaceGroteskFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Configure local screen lock locks.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Biometric Unlock", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Enable Fingerprint / Face ID unlock upon opening Vesper.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = biometric, onCheckedChange = onBiometricChange)
        }
    }
}
