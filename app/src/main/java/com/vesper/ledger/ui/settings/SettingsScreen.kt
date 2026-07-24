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
    MAIN, UPDATES
}

enum class SettingsDialogType {
    THEME, ABOUT_APP, PRIVACY_POLICY, OPEN_SOURCE, TERMS, EDIT_NAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    onMenuClick: () -> Unit,
    onBackClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onAccountsClick: (() -> Unit)? = null,
    onSignOutClick: () -> Unit
) {
    val updateUiState by updateViewModel.uiState.collectAsState()
    val isUpdateAvailable = updateUiState.updateInfo != null && updateUiState.updateInfo!!.updateAvailable

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdatesOnLaunch()
    }
    val theme by viewModel.theme.collectAsState()
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
        topBar = {
            if (subView != SettingsSubView.MAIN) {
                ChildHeader(
                    title = "Software Updates",
                    onBackClick = { subView = SettingsSubView.MAIN }
                )
            }
        },
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
                        }

                        // Transactions & Accounts Section
                        SettingsGroup(title = "Transactions & Accounts") {
                            SettingsRow(
                                icon = Icons.Outlined.Category,
                                title = "Categories",
                                subtitle = "Manage standard and customized transaction tags",
                                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { onCategoriesClick() }
                            )
                            if (onAccountsClick != null) {
                                SettingsRow(
                                    icon = Icons.Outlined.AccountBalanceWallet,
                                    title = "Accounts Management",
                                    subtitle = "Manage accounts, starting balances, and total net worth settings",
                                    trailing = { Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { onAccountsClick() }
                                )
                            }
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.vesper.ledger.ui.update.SettingsUpdatesScreen(updateViewModel)
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


