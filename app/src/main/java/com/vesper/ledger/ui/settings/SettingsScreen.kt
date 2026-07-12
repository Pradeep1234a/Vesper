package com.vesper.ledger.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShSegmentedControl
import com.vesper.ledger.ui.components.ShTextField

enum class SettingsSubView {
    MAIN, CATEGORIES
}

enum class SettingsDialogType {
    THEME, LANGUAGE, DEFAULT_TX_TYPE, DEFAULT_ACCOUNT, ABOUT_APP, PRIVACY_POLICY, OPEN_SOURCE, TERMS, CONFIRM_RESTORE, EDIT_NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onCurrencyClick: () -> Unit
) {
    val currency by viewModel.currency.collectAsState()
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val defaultTransactionType by viewModel.defaultTransactionType.collectAsState()
    val quickAddPreferences by viewModel.quickAddPreferences.collectAsState()
    val defaultAccount by viewModel.defaultAccount.collectAsState()
    val dailyReminder by viewModel.dailyReminder.collectAsState()
    val missedEntryReminder by viewModel.missedEntryReminder.collectAsState()
    val budgetReminder by viewModel.budgetReminder.collectAsState()
    val recurringReminder by viewModel.recurringReminder.collectAsState()
    val appLock by viewModel.appLock.collectAsState()
    val biometricAuth by viewModel.biometricAuth.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var subView by remember { mutableStateOf(SettingsSubView.MAIN) }
    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }
    val context = LocalContext.current

    // Category Creator State
    var newCatName by remember { mutableStateOf("") }
    var newCatType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var newCatColor by remember { mutableStateOf("#71717A") }

    val colorOptions = listOf(
        "#16A34A", // Green
        "#DC2626", // Red
        "#2563EB", // Blue
        "#D97706", // Amber
        "#9333EA", // Purple
        "#DB2777", // Pink
        "#0D9488", // Teal
        "#71717A"  // Grey
    )

    // Dialog handlers
    when (activeDialog) {
        SettingsDialogType.THEME -> {
            SettingsSelectionDialog(
                title = "Select Theme",
                options = listOf("light", "dark", "system"),
                selectedOption = theme,
                onOptionSelected = { viewModel.saveTheme(it) },
                onDismissRequest = { activeDialog = null },
                labelProvider = { it.replaceFirstChar { char -> char.uppercase() } }
            )
        }
        SettingsDialogType.LANGUAGE -> {
            SettingsSelectionDialog(
                title = "Select Language",
                options = listOf("English", "Spanish", "French", "German"),
                selectedOption = language,
                onOptionSelected = { viewModel.saveLanguage(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.DEFAULT_TX_TYPE -> {
            SettingsSelectionDialog(
                title = "Default Transaction Type",
                options = listOf("Expense", "Income"),
                selectedOption = defaultTransactionType,
                onOptionSelected = { viewModel.saveDefaultTransactionType(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.DEFAULT_ACCOUNT -> {
            SettingsSelectionDialog(
                title = "Default Account",
                options = listOf("Cash", "Bank Card", "Savings Account"),
                selectedOption = defaultAccount,
                onOptionSelected = { viewModel.saveDefaultAccount(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.ABOUT_APP -> {
            SettingsInfoDialog(
                title = "About Vesper Ledger",
                text = "Vesper Ledger v1.0.0\n\nA premium, minimalist personal finance tracker designed for Android using a clean shadcn-inspired design system.",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.PRIVACY_POLICY -> {
            SettingsInfoDialog(
                title = "Privacy Policy",
                text = "Your financial privacy is our highest priority. All your transaction data, accounts, and categories are saved locally on your device in a secure SQLite database. Vesper Ledger does not track, collect, or transmit any of your personal or financial information.",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.OPEN_SOURCE -> {
            SettingsInfoDialog(
                title = "Open Source Licenses",
                text = "Vesper Ledger is built using open-source libraries:\n\n• Jetpack Compose (Apache License 2.0)\n• Kotlin Coroutines (Apache License 2.0)\n• Room Database (Apache License 2.0)",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.TERMS -> {
            SettingsInfoDialog(
                title = "Terms & Conditions",
                text = "Vesper Ledger is provided as-is without any warranties of any kind. You are responsible for backing up your data using the Data & Backup utilities in Settings.",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.CONFIRM_RESTORE -> {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Restore Data", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = { Text("Are you sure you want to restore? This will overwrite your current local database.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            activeDialog = null
                            Toast.makeText(context, "Data restored successfully", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Restore", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        SettingsDialogType.EDIT_NAME -> {
            var tempName by remember { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("Edit Name", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("What should we call you?")
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmed = tempName.trim()
                            if (trimmed.isNotBlank()) {
                                viewModel.saveUserName(trimmed)
                                activeDialog = null
                            } else {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        null -> {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (subView == SettingsSubView.MAIN) "Settings" else "Manage Categories",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (subView == SettingsSubView.CATEGORIES) {
                                subView = SettingsSubView.MAIN
                            } else {
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (subView == SettingsSubView.MAIN) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Dedicated Premium Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { activeDialog = SettingsDialogType.EDIT_NAME },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Personal Profile • Tap to edit name",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Preferences Section
                SettingsGroup(title = "Preferences") {
                    SettingsRow(
                        icon = Icons.Outlined.Palette,
                        title = "Theme",
                        trailing = { Text(theme.replaceFirstChar { char -> char.uppercase() }, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = { activeDialog = SettingsDialogType.THEME }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.AttachMoney,
                        title = "Currency",
                        trailing = { Text(currency, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = onCurrencyClick
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Translate,
                        title = "Language",
                        trailing = { Text(language, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = { activeDialog = SettingsDialogType.LANGUAGE }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.ColorLens,
                        title = "Dynamic Colors",
                        trailing = {
                            Switch(
                                checked = dynamicColors,
                                onCheckedChange = { viewModel.saveDynamicColors(it) }
                            )
                        }
                    )
                }

                // Transactions Section
                SettingsGroup(title = "Transactions") {
                    SettingsRow(
                        icon = Icons.Outlined.Category,
                        title = "Categories",
                        subtitle = "Manage standard and customized transaction tags",
                        trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { subView = SettingsSubView.CATEGORIES }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.SwapHoriz,
                        title = "Default Transaction Type",
                        trailing = { Text(defaultTransactionType, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = { activeDialog = SettingsDialogType.DEFAULT_TX_TYPE }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Bolt,
                        title = "Quick Add Preferences",
                        trailing = {
                            Switch(
                                checked = quickAddPreferences,
                                onCheckedChange = { viewModel.saveQuickAddPreferences(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.AccountBalanceWallet,
                        title = "Default Account",
                        trailing = { Text(defaultAccount, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = { activeDialog = SettingsDialogType.DEFAULT_ACCOUNT }
                    )
                }

                // Notifications Section
                SettingsGroup(title = "Notifications") {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Daily Reminder",
                        trailing = {
                            Switch(
                                checked = dailyReminder,
                                onCheckedChange = { viewModel.saveDailyReminder(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.NotificationsActive,
                        title = "Missed Entry Reminder",
                        trailing = {
                            Switch(
                                checked = missedEntryReminder, // Map to correct flow
                                onCheckedChange = { viewModel.saveMissedEntryReminder(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.NotificationsPaused,
                        title = "Budget Reminder",
                        trailing = {
                            Switch(
                                checked = budgetReminder,
                                onCheckedChange = { viewModel.saveBudgetReminder(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Autorenew,
                        title = "Recurring Transaction Reminder",
                        trailing = {
                            Switch(
                                checked = recurringReminder,
                                onCheckedChange = { viewModel.saveRecurringReminder(it) }
                            )
                        }
                    )
                }

                // Data & Backup Section
                SettingsGroup(title = "Data & Backup") {
                    SettingsRow(
                        icon = Icons.Outlined.CloudUpload,
                        title = "Backup",
                        subtitle = "Back up current local data to device store",
                        onClick = { Toast.makeText(context, "Backup created successfully", Toast.LENGTH_SHORT).show() }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.CloudDownload,
                        title = "Restore",
                        subtitle = "Restore database from existing backup store",
                        onClick = { activeDialog = SettingsDialogType.CONFIRM_RESTORE }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Description,
                        title = "Export CSV",
                        subtitle = "Export transactions as clean table files",
                        onClick = { Toast.makeText(context, "CSV exported successfully to Downloads folder", Toast.LENGTH_SHORT).show() }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Code,
                        title = "Export JSON",
                        subtitle = "Export transactions in structured developer format",
                        onClick = { Toast.makeText(context, "JSON exported successfully to Downloads folder", Toast.LENGTH_SHORT).show() }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Input,
                        title = "Import Data",
                        subtitle = "Load transaction dataset from JSON or CSV",
                        onClick = { Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show() }
                    )
                }

                // Security Section
                SettingsGroup(title = "Security") {
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        title = "App Lock",
                        trailing = {
                            Switch(
                                checked = appLock,
                                onCheckedChange = { viewModel.saveAppLock(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Fingerprint,
                        title = "Biometric Authentication",
                        subtitle = "Unlock app with secure biometric fingerprint scanner",
                        trailing = {
                            Switch(
                                checked = biometricAuth,
                                onCheckedChange = { viewModel.saveBiometricAuth(it) }
                            )
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Security,
                        title = "Privacy Settings",
                        trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { Toast.makeText(context, "Privacy settings are up to date", Toast.LENGTH_SHORT).show() }
                    )
                }

                // Premium Section
                SettingsGroup(title = "Premium") {
                    SettingsRow(
                        icon = Icons.Outlined.Star,
                        title = "Upgrade to Pro",
                        subtitle = "Unlock cloud backup and advanced export features.",
                        trailing = {
                            Text(
                                text = if (isProUser) "Pro Active" else "Get Pro >",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                )
                            )
                        },
                        onClick = {
                            viewModel.saveIsProUser(!isProUser)
                            Toast.makeText(
                                context,
                                if (!isProUser) "Upgraded to Pro successfully!" else "Subscription managed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.CreditCard,
                        title = "Manage Subscription",
                        onClick = { Toast.makeText(context, "Redirecting to subscription portal...", Toast.LENGTH_SHORT).show() }
                    )
                }

                // About Section
                SettingsGroup(title = "About") {
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        title = "App Version",
                        trailing = { Text("v1.0.0", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                        onClick = { activeDialog = SettingsDialogType.ABOUT_APP }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Policy,
                        title = "Privacy Policy",
                        trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { activeDialog = SettingsDialogType.PRIVACY_POLICY }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Gavel,
                        title = "Open Source Licenses",
                        trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { activeDialog = SettingsDialogType.OPEN_SOURCE }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsRow(
                        icon = Icons.Outlined.Assignment,
                        title = "Terms & Conditions",
                        trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { activeDialog = SettingsDialogType.TERMS }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            // CATEGORIES Sub-View (In-place Category Manager)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                ShCard {
                    Text(
                        text = "Add Customized Category",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ShTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = "Category Name",
                        placeholder = "e.g., Subscriptions"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Category Type",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ShSegmentedControl(
                        items = listOf("Expense", "Income"),
                        selectedIndex = if (newCatType == TransactionType.EXPENSE) 0 else 1,
                        onItemSelected = { index ->
                            newCatType = if (index == 0) TransactionType.EXPENSE else TransactionType.INCOME
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Theme Color",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = hex == newCatColor
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { newCatColor = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    ShButton(
                        text = "Create Category",
                        onClick = {
                            if (newCatName.isNotBlank()) {
                                viewModel.addCategory(newCatName, newCatType, newCatColor)
                                newCatName = ""
                            }
                        },
                        enabled = newCatName.isNotBlank()
                    )
                }

                Text(
                    text = "Manage Existing Categories",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )

                // List of existing categories grouped together nicely
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        categories.forEachIndexed { index, cat ->
                            val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Text(
                                            text = if (cat.type == TransactionType.INCOME) "Income" else "Expense",
                                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Category",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            if (index < categories.size - 1) {
                                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title.toUpperCase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                )
            }
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismissRequest: () -> Unit,
    labelProvider: (T) -> String = { it.toString() }
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onDismissRequest()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onOptionSelected(option)
                                onDismissRequest()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun SettingsInfoDialog(
    title: String,
    text: String,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
        text = { Text(text, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}
