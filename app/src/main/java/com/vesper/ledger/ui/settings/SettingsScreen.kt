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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.BuildConfig
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

enum class SettingsSubView {
    MAIN, PROFILE, UPDATES, PRIVACY_POLICY, OPEN_SOURCE, TERMS
}

enum class SettingsDialogType {
    THEME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    updateViewModel: com.vesper.ledger.ui.update.UpdateViewModel,
    onMenuClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val updateUiState by updateViewModel.uiState.collectAsState()
    val isUpdateAvailable = updateUiState.updateInfo != null && updateUiState.updateInfo!!.updateAvailable

    LaunchedEffect(Unit) {
        updateViewModel.checkForUpdatesOnLaunch()
    }
    val theme by viewModel.theme.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val currencyCode by viewModel.currencyCode.collectAsState()

    var subView by remember { mutableStateOf(SettingsSubView.MAIN) }
    var activeDialog by remember { mutableStateOf<SettingsDialogType?>(null) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Theme Dialog Handler
    if (activeDialog == SettingsDialogType.THEME) {
        SettingsSelectionDialog(
            title = "Select Theme",
            options = listOf("light", "dark", "system"),
            selectedOption = theme,
            onOptionSelected = { viewModel.saveTheme(it) },
            onDismissRequest = { activeDialog = null },
            labelProvider = { it.replaceFirstChar { char -> char.uppercase() } }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (subView) {
            SettingsSubView.MAIN -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Personal Profile Card (Navigates to dedicated Profile Screen, not a modal)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { subView = SettingsSubView.PROFILE },
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
                                    text = userName.take(1).uppercase().ifBlank { "U" },
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName.ifBlank { "User Profile" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (userEmail.isNotEmpty()) userEmail else "Personal Profile • Tap to edit details",
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
                            icon = Icons.Outlined.AttachMoney,
                            title = "Currency",
                            subtitle = "Primary currency for balances and budgets",
                            trailing = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "$currencyCode ($currencySymbol)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Icon(
                                        Icons.Outlined.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { onCurrencyClick() }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Outlined.Palette,
                            title = "Theme",
                            trailing = {
                                Text(
                                    theme.replaceFirstChar { char -> char.uppercase() },
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            },
                            onClick = { activeDialog = SettingsDialogType.THEME }
                        )
                    }

                    // Ledger & Transactions Section
                    SettingsGroup(title = "Transactions") {
                        SettingsRow(
                            icon = Icons.Outlined.Category,
                            title = "Categories",
                            subtitle = "Manage standard and customized transaction tags",
                            trailing = {
                                Icon(
                                    Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { onCategoriesClick() }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Outlined.AccountBalance,
                            title = "Accounts",
                            subtitle = "Manage financial bank & wallet accounts",
                            trailing = {
                                Icon(
                                    Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { onAccountsClick() }
                        )
                    }

                    // About Section
                    SettingsGroup(title = "About") {
                        SettingsRow(
                            icon = if (isUpdateAvailable) Icons.Outlined.FileDownload else Icons.Outlined.Check,
                            title = "Application Updates",
                            subtitle = if (isUpdateAvailable && updateUiState.updateInfo != null) {
                                "v${BuildConfig.VERSION_NAME} → v${updateUiState.updateInfo?.latestVersionName} available"
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
                            subtitle = "100% local on-device privacy guarantee",
                            trailing = {
                                Icon(
                                    Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { subView = SettingsSubView.PRIVACY_POLICY }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Outlined.Gavel,
                            title = "Open Source Licenses",
                            subtitle = "Open source libraries and developer credits",
                            trailing = {
                                Icon(
                                    Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { subView = SettingsSubView.OPEN_SOURCE }
                        )
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsRow(
                            icon = Icons.Outlined.Assignment,
                            title = "Terms & Conditions",
                            subtitle = "Terms of service and software disclaimers",
                            trailing = {
                                Icon(
                                    Icons.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { subView = SettingsSubView.TERMS }
                        )
                    }

                    // Account Section (Sign Out)
                    SettingsGroup(title = "Account") {
                        SettingsRow(
                            icon = Icons.Outlined.ExitToApp,
                            title = "Sign Out",
                            subtitle = "Disconnect current profile and database",
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = { onSignOutClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // DEDICATED PROFILE SCREEN (NOT A MODAL DIALOG)
            SettingsSubView.PROFILE -> {
                var editName by remember { mutableStateOf(userName) }
                var editEmail by remember { mutableStateOf(userEmail) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SubViewHeader(
                        title = "Profile Management",
                        onBack = { subView = SettingsSubView.MAIN }
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = editName.take(1).uppercase().ifBlank { "U" },
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Text(
                                text = "USER DETAILS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            ShTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = "Display Name",
                                placeholder = "Your Name"
                            )

                            ShTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = "Email Address (Optional)",
                                placeholder = "email@example.com"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { subView = SettingsSubView.MAIN },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Cancel")
                                }

                                Button(
                                    onClick = {
                                        val trimmed = editName.trim()
                                        if (trimmed.isNotBlank()) {
                                            viewModel.saveUserName(trimmed)
                                            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                            subView = SettingsSubView.MAIN
                                        } else {
                                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }

            // DEDICATED APPLICATION UPDATES SCREEN (FIXES SCROLL NESTING CRASH)
            SettingsSubView.UPDATES -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { subView = SettingsSubView.MAIN }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        Text(
                            text = "Application Updates",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        com.vesper.ledger.ui.update.SettingsUpdatesScreen(updateViewModel)
                    }
                }
            }

            // DEDICATED PRIVACY POLICY SCREEN (FULL SCREEN, NOT A MODAL)
            SettingsSubView.PRIVACY_POLICY -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SubViewHeader(
                        title = "Privacy Policy",
                        onBack = { subView = SettingsSubView.MAIN }
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "PRIVACY GUARANTEE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Effective Date: July 2026",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            listOf(
                                "1. 100% Local On-Device Storage" to "Vesper Ledger operates entirely offline. All of your financial entries, transaction history, accounts, and budgets are saved exclusively on your local device's encrypted SQLite database. No data is ever transmitted, uploaded, or synced to remote cloud servers.",
                                "2. Zero Analytics & Tracking" to "We respect your digital sovereignty. Vesper Ledger contains zero tracking SDKs, zero analytics counters, and zero advertising services. Your personal spending habits belong solely to you.",
                                "3. Hardware Permissions Disclosure" to "Optional device permissions (such as camera access) are used strictly for local receipt scanning features. Captured photos are processed locally on your phone's processor and are never uploaded anywhere.",
                                "4. Complete Data Retention & Control" to "You maintain total control over your financial records. You can modify, export, or erase your entire database at any time directly through the application settings."
                            ).forEach { (heading, body) ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = heading,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DEDICATED TERMS & CONDITIONS SCREEN (FULL SCREEN, NOT A MODAL)
            SettingsSubView.TERMS -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SubViewHeader(
                        title = "Terms & Conditions",
                        onBack = { subView = SettingsSubView.MAIN }
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "TERMS OF SERVICE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Effective Date: July 2026",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            listOf(
                                "1. Software Usage Rights" to "Vesper Ledger is provided for personal financial logging and budget planning. You are granted a personal, non-exclusive license to use the application on your devices.",
                                "2. Financial Advice Disclaimer" to "Vesper Ledger is an organizational software tool. It does not constitute certified financial, tax, legal, or investment advice. You remain responsible for verifying your financial decisions.",
                                "3. User Backup Responsibilities" to "Because all data is stored strictly on your local device, maintaining device backups (e.g. Android system backups or manual database exports) is your responsibility in the event of device damage or loss.",
                                "4. Limitation of Warranty" to "The software is provided 'as-is' without warranties of any kind, express or implied. The developer is not liable for data entry errors or device-specific hardware failures."
                            ).forEach { (heading, body) ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = heading,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DEDICATED OPEN SOURCE LICENSES SCREEN (FULL SCREEN WITH CREDITS)
            SettingsSubView.OPEN_SOURCE -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SubViewHeader(
                        title = "Open Source Licenses",
                        onBack = { subView = SettingsSubView.MAIN }
                    )

                    Text(
                        text = "Vesper Ledger is built using world-class open-source software libraries and frameworks:",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    listOf(
                        Triple("Jetpack Compose", "Google LLC • Apache License 2.0", "https://developer.android.com/jetpack/compose"),
                        Triple("Material 3 Design System", "Google LLC • Apache License 2.0", "https://m3.material.io"),
                        Triple("fl_chart Analytics Library", "imaNNeoFighT • MIT License", "https://github.com/imaNNeoFighT/fl_chart"),
                        Triple("Kotlin Coroutines & Flow", "JetBrains s.r.o. • Apache License 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
                        Triple("Room SQLite Database", "Google LLC • Apache License 2.0", "https://developer.android.com/training/data-storage/room"),
                        Triple("CameraX API", "Google LLC • Apache License 2.0", "https://developer.android.com/training/camerax"),
                        Triple("Space Grotesk Font Family", "Florian Karsten • Open Font License", "https://fonts.google.com/specimen/Space+Grotesk")
                    ).forEach { (name, desc, url) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                                .clickable { uriHandler.openUri(url) },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Visit project page",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubViewHeader(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
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
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun SettingsSelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    labelProvider: (String) -> String = { it }
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                onOptionSelected(option)
                                onDismissRequest()
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
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
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
    )
}
