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
import androidx.compose.material.icons.filled.Check
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
import com.vesper.ledger.BuildConfig
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShSegmentedControl
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.DynamicLogo
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.ChildHeader
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class SettingsSubView {
    MAIN, UPDATES, APP_ICON
}

enum class SettingsDialogType {
    THEME, CURRENCY, LANGUAGE, DEFAULT_TX_TYPE, DEFAULT_ACCOUNT, DEFAULT_PAYMENT_METHOD, ABOUT_APP, PRIVACY_POLICY, OPEN_SOURCE, TERMS, EDIT_NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    onBackClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val currency by viewModel.currency.collectAsState()
    val updateUiState by updateViewModel.uiState.collectAsState()
    val isUpdateAvailable = updateUiState.updateInfo != null && updateUiState.updateInfo!!.updateAvailable

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdatesOnLaunch()
    }
    val theme by viewModel.theme.collectAsState()
    val language by viewModel.language.collectAsState()
    val defaultTransactionType by viewModel.defaultTransactionType.collectAsState()
    val quickAddPreferences by viewModel.quickAddPreferences.collectAsState()
    val defaultAccount by viewModel.defaultAccount.collectAsState()
    val defaultPaymentMethod by viewModel.defaultPaymentMethod.collectAsState()
    val accountsList by viewModel.accounts.collectAsState()
    val paymentMethodsList by viewModel.paymentMethods.collectAsState()
    val appIcon by viewModel.appIcon.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    var subView by remember { mutableStateOf(SettingsSubView.MAIN) }
    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

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
        SettingsDialogType.CURRENCY -> {
            SettingsSelectionDialog(
                title = "Select Currency",
                options = listOf("USD", "EUR", "GBP", "INR", "JPY", "CAD", "AUD", "CHF", "CNY", "BRL", "MXN", "ZAR", "RUB", "SAR", "AED", "SGD", "AUD", "NZD", "CHF", "ILS", "BDT"),
                selectedOption = currency,
                onOptionSelected = { viewModel.saveCurrency(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.LANGUAGE -> {
            SettingsSelectionDialog(
                title = "Select Language",
                options = listOf("English", "Hindi", "Español", "Français", "Deutsch", "Italiano", "Português", "Русский", "日本語", "한국어", "中文"),
                selectedOption = language,
                onOptionSelected = { viewModel.saveLanguage(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.DEFAULT_TX_TYPE -> {
            SettingsSelectionDialog(
                title = "Default Transaction Type",
                options = listOf("Expense", "Income", "Transfer"),
                selectedOption = defaultTransactionType,
                onOptionSelected = { viewModel.saveDefaultTransactionType(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.DEFAULT_ACCOUNT -> {
            val accountNames = accountsList.map { it.name }
            SettingsSelectionDialog(
                title = "Default Account",
                options = accountNames,
                selectedOption = defaultAccount,
                onOptionSelected = { viewModel.saveDefaultAccount(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.DEFAULT_PAYMENT_METHOD -> {
            val pmNames = paymentMethodsList.map { it.name }
            SettingsSelectionDialog(
                title = "Default Payment Method",
                options = pmNames,
                selectedOption = defaultPaymentMethod,
                onOptionSelected = { viewModel.saveDefaultPaymentMethod(it) },
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.ABOUT_APP -> {
            SettingsInfoDialog(
                title = "About Vesper Ledger",
                text = "Vesper Ledger is a premium, minimalist personal finance tracker designed for Android using a clean shadcn-inspired design system.",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.PRIVACY_POLICY -> {
            SettingsInfoDialog(
                title = "Privacy Policy",
                text = "Vesper Ledger operates completely offline. Your financial data is securely stored on your local device storage and is never uploaded to any remote servers.",
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
                text = "Vesper Ledger is provided as-is without any warranties of any kind. You are responsible for managing your financial logs and data.",
                onDismissRequest = { activeDialog = null }
            )
        }
        SettingsDialogType.EDIT_NAME -> {
            var tempName by remember { mutableStateOf(userName) }
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = {
                    Text(
                        text = "Edit Name",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = "What should we call you?",
                            placeholder = "Your name"
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
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeDialog = null }) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
            )
        }
        null -> {}
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (subView) {
                SettingsSubView.MAIN -> {
                    RootHeader(
                        title = "Settings"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {

                        // Personal Profile Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clickable { activeDialog = SettingsDialogType.EDIT_NAME },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
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
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = userName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
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
                                        text = if (userEmail.isNotEmpty()) userEmail else "Personal Profile • Tap to edit name",
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
                                onClick = { activeDialog = SettingsDialogType.CURRENCY }
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
                                icon = Icons.Outlined.Layers,
                                title = "App Icon",
                                trailing = {
                                    Text(
                                        text = appIcon.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                },
                                onClick = { subView = SettingsSubView.APP_ICON }
                            )
                        }

                        // Transactions Section
                        SettingsGroup(title = "Transactions") {
                            SettingsRow(
                                icon = Icons.Outlined.Category,
                                title = "Categories",
                                subtitle = "Manage standard and customized transaction tags",
                                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { onCategoriesClick() }
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
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            SettingsRow(
                                icon = Icons.Outlined.Payment,
                                title = "Default Payment Method",
                                trailing = { Text(defaultPaymentMethod, style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                                onClick = { activeDialog = SettingsDialogType.DEFAULT_PAYMENT_METHOD }
                            )
                        }

                        // About Section
                        SettingsGroup(title = "About") {
                            SettingsRow(
                                icon = Icons.Outlined.Info,
                                title = "App Version",
                                trailing = { Text("v${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)) },
                                onClick = { activeDialog = SettingsDialogType.ABOUT_APP }
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            SettingsRow(
                                icon = if (isUpdateAvailable) Icons.Outlined.FileDownload else Icons.Outlined.Check,
                                title = "Application Updates",
                                subtitle = if (isUpdateAvailable) {
                                    "v${BuildConfig.VERSION_NAME} → v${updateUiState.updateInfo!!.latestVersionName} available"
                                } else {
                                    "v${BuildConfig.VERSION_NAME} • Up To Date"
                                },
                                trailing = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isUpdateAvailable) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "NEW",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Outlined.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = { subView = SettingsSubView.UPDATES }
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

                        // Account Section (Sign Out)
                        SettingsGroup(title = "Account") {
                            SettingsRow(
                                icon = Icons.Outlined.ExitToApp,
                                title = "Sign Out",
                                subtitle = "Disconnect current profile and database",
                                titleColor = MaterialTheme.colorScheme.error,
                                onClick = {
                                    onSignOutClick()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                SettingsSubView.UPDATES -> {
                    LaunchedEffect(Unit) {
                        updateViewModel.checkForUpdatesOnLaunch()
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        ChildHeader(
                            title = "Updates",
                            onBackClick = { subView = SettingsSubView.MAIN }
                        )
                        Box(modifier = Modifier.fillMaxSize()) {
                            com.vesper.ledger.ui.update.SettingsUpdatesScreen(updateViewModel)
                        }
                    }
                }
                SettingsSubView.APP_ICON -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ChildHeader(
                            title = "App Icon",
                            onBackClick = { subView = SettingsSubView.MAIN }
                        )
                        Box(modifier = Modifier.fillMaxSize()) {
                            AppIconSelectionScreen(viewModel = viewModel)
                        }
                    }
                }
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
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium
                ),
            shape = MaterialTheme.shapes.medium,
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
    titleColor: Color = Color.Unspecified,
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (titleColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else titleColor,
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
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
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
        title = { 
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                ) 
            }
        },
        text = { 
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (title == "About Vesper Ledger") {
                    DynamicLogo(size = 56.dp, cornerRadius = 12.dp)
                }
                Text(
                    text = text, 
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = if (title == "About Vesper Ledger") TextAlign.Center else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismissRequest) {
                    Text(
                        text = "Close",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
    )
}

@Composable
fun AppIconSelectionScreen(
    viewModel: SettingsViewModel
) {
    val activeIcon by viewModel.appIcon.collectAsState()
    val context = LocalContext.current
    
    data class IconVariantItem(
        val key: String,
        val name: String,
        val desc: String,
        val bgRes: Int,
        val fgRes: Int
    )

    val variants = remember {
        listOf(
            IconVariantItem("default", "Official", "Default installation balance scale logo", com.vesper.ledger.R.drawable.ic_launcher_background, com.vesper.ledger.R.drawable.ic_launcher_foreground),
            IconVariantItem("flux", "Flux", "Smooth violet-pink gradient cashflow wave", com.vesper.ledger.R.drawable.ic_flux_background, com.vesper.ledger.R.drawable.ic_flux_foreground),
            IconVariantItem("material", "Material 3", "Teal adaptive geometric wallet outline", com.vesper.ledger.R.drawable.ic_material_background, com.vesper.ledger.R.drawable.ic_material_foreground),
            IconVariantItem("glass", "iOS Glass", "Translucent glass diamond prism highlights", com.vesper.ledger.R.drawable.ic_glass_background, com.vesper.ledger.R.drawable.ic_glass_foreground),
            IconVariantItem("coin", "Coin", "Premium gold rupee coin branding", com.vesper.ledger.R.drawable.ic_coin_background, com.vesper.ledger.R.drawable.ic_coin_foreground),
            IconVariantItem("analytics", "Analytics", "Deep royal blue trending growth chart", com.vesper.ledger.R.drawable.ic_analytics_background, com.vesper.ledger.R.drawable.ic_analytics_foreground),
            IconVariantItem("vault", "Vault", "Charcoal secure lock & privacy shield", com.vesper.ledger.R.drawable.ic_vault_background, com.vesper.ledger.R.drawable.ic_vault_foreground),
            IconVariantItem("ledger", "Ledger", "Forest green open book bookkeeping mark", com.vesper.ledger.R.drawable.ic_ledger_background, com.vesper.ledger.R.drawable.ic_ledger_foreground)
        )
    }

    var previewKey by remember(activeIcon) { mutableStateOf(activeIcon) }
    val previewVariant = variants.find { it.key == previewKey } ?: variants[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOME SCREEN PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "10:42",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                    )
                    Text(
                        text = "Monday, July 13",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color.LightGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MockAppIcon(name = "Phone", icon = Icons.Outlined.Phone, color = Color(0xFF10B981))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = previewVariant.bgRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                            Image(
                                painter = painterResource(id = previewVariant.fgRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "Vesper",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }

                    MockAppIcon(name = "Messages", icon = Icons.Outlined.ChatBubbleOutline, color = Color(0xFF3B82F6))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val isActive = previewKey == activeIcon
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isActive) "● Active Identity" else "Previewing Selection",
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Text(
            text = "Choose App Identity",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val rows = variants.chunked(2)
            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        val isSelected = item.key == previewKey
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .clickable { previewKey = item.key },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(11.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = item.bgRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Image(
                                        painter = painterResource(id = item.fgRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center
                                )
                                
                                Text(
                                    text = item.desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    minLines = 2
                                )
                            }
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShButton(
                text = "Apply Identity",
                onClick = {
                    viewModel.saveAppIcon(previewKey)
                    Toast.makeText(context, "App icon updated successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = previewKey != activeIcon
            )

            TextButton(
                onClick = {
                    viewModel.saveAppIcon("default")
                    previewKey = "default"
                    Toast.makeText(context, "Official Vesper icon restored", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Restore Default")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MockAppIcon(
    name: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(color = Color.LightGray)
        )
    }
}
