package com.vesper.ledger.ui.settings

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.BuildConfig
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShTextField
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

enum class SettingsSubView {
    MAIN, PROFILE, UPDATES, PRIVACY, TERMS, OPEN_SOURCE, APP_VERSION, ABOUT
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
    var showThemeDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Redesigned M3 Theme Selector Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text(
                    text = "App Theme",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "dark" to "Dark Mode (Vesper Pure Dark)",
                        "light" to "Light Mode",
                        "system" to "System Default"
                    ).forEach { (key, label) ->
                        val isSelected = theme == key
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF38BDF8).copy(alpha = 0.18f) else Color(0xFF242429))
                                .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF3F3F46), RoundedCornerShape(6.dp))
                                .clickable {
                                    viewModel.saveTheme(key)
                                    showThemeDialog = false
                                }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", fontFamily = SpaceGroteskFamily, color = Color(0xFFA1A1AA))
                }
            },
            containerColor = Color(0xFF121215),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
    ) {
        // Sub-View Header with Back Navigation
        if (subView != SettingsSubView.MAIN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { subView = SettingsSubView.MAIN }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = when (subView) {
                        SettingsSubView.PROFILE -> "User Profile Studio"
                        SettingsSubView.UPDATES -> "Application Updates"
                        SettingsSubView.PRIVACY -> "Privacy Policy"
                        SettingsSubView.TERMS -> "Terms & Conditions"
                        SettingsSubView.OPEN_SOURCE -> "Open Source Licenses"
                        SettingsSubView.APP_VERSION -> "App Version & What's New"
                        SettingsSubView.ABOUT -> "About Vesper Ledger"
                        else -> "Settings"
                    },
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // EXACT 8DP TOP MARGIN SPACING BETWEEN TOP BAR AND FIRST CARD
            Spacer(modifier = Modifier.height(8.dp))

            when (subView) {
                SettingsSubView.MAIN -> {
                    // 1. PERSONAL PROFILE TILE (NAVIGATES TO DEDICATED PROFILE SCREEN)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF18181B))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                            .clickable { subView = SettingsSubView.PROFILE }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase().ifBlank { "U" },
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userName.ifBlank { "Vesper User" },
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (userEmail.isNotBlank()) userEmail else "Tap to manage user profile & details",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFA1A1AA)
                            )
                        }
                    }

                    // 2. PREFERENCES SECTION
                    SettingsGroupCard(title = "PREFERENCES") {
                        SettingsRowItem(
                            icon = Icons.Outlined.AttachMoney,
                            title = "Primary Currency",
                            subtitle = "Currency symbol for balances and transactions",
                            trailingText = "$currencyCode ($currencySymbol)",
                            onClick = { onCurrencyClick() }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.Palette,
                            title = "App Theme",
                            subtitle = "Visual dark mode appearance",
                            trailingText = theme.replaceFirstChar { it.uppercase() },
                            onClick = { showThemeDialog = true }
                        )
                    }

                    // 3. LEDGER & DATA MANAGEMENT SECTION
                    SettingsGroupCard(title = "LEDGER & MANAGEMENT") {
                        SettingsRowItem(
                            icon = Icons.Outlined.Category,
                            title = "Categories",
                            subtitle = "Manage expense & income category tags",
                            onClick = { onCategoriesClick() }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.AccountBalance,
                            title = "Accounts",
                            subtitle = "Manage cash, bank & wallet accounts",
                            onClick = { onAccountsClick() }
                        )
                    }

                    // 4. ABOUT & APPLICATION SECTION
                    SettingsGroupCard(title = "APPLICATION & LEGAL") {
                        SettingsRowItem(
                            icon = Icons.Outlined.Info,
                            title = "App Version & What's New",
                            subtitle = "Current version v${BuildConfig.VERSION_NAME}",
                            trailingText = "v${BuildConfig.VERSION_NAME}",
                            onClick = { subView = SettingsSubView.APP_VERSION }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = if (isUpdateAvailable) Icons.Outlined.FileDownload else Icons.Outlined.Check,
                            title = "Application Updates",
                            subtitle = if (isUpdateAvailable && updateUiState.updateInfo != null) {
                                "v${updateUiState.updateInfo?.latestVersionName} available"
                            } else {
                                "Up To Date"
                            },
                            badgeText = if (isUpdateAvailable) "NEW UPDATE" else null,
                            onClick = { subView = SettingsSubView.UPDATES }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.Policy,
                            title = "Privacy Policy",
                            subtitle = "100% local on-device privacy guarantee",
                            onClick = { subView = SettingsSubView.PRIVACY }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.Gavel,
                            title = "Open Source Licenses",
                            subtitle = "Credits & libraries used in Vesper Ledger",
                            onClick = { subView = SettingsSubView.OPEN_SOURCE }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.Assignment,
                            title = "Terms & Conditions",
                            subtitle = "Software terms of service & disclaimers",
                            onClick = { subView = SettingsSubView.TERMS }
                        )
                        Divider(color = Color(0xFF27272A))
                        SettingsRowItem(
                            icon = Icons.Outlined.HelpOutline,
                            title = "About Vesper Ledger",
                            subtitle = "Story, mission & developer credits",
                            onClick = { subView = SettingsSubView.ABOUT }
                        )
                    }

                    // 5. ACCOUNT SECTION (SIGN OUT)
                    SettingsGroupCard(title = "ACCOUNT LOCK") {
                        SettingsRowItem(
                            icon = Icons.Outlined.ExitToApp,
                            title = "Sign Out / Reset Profile",
                            subtitle = "Disconnect active profile session",
                            titleColor = Color(0xFFEF4444),
                            onClick = { onSignOutClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // DEDICATED PROFILE SCREEN
                SettingsSubView.PROFILE -> {
                    var editName by remember { mutableStateOf(userName) }
                    var editEmail by remember { mutableStateOf(userEmail) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF18181B))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Avatar Monogram Display
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(CircleShape)
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                    .border(2.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = editName.take(1).uppercase().ifBlank { "U" },
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            Text(
                                text = "USER PROFILE DETAILS",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFFA1A1AA)
                            )

                            ShTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = "Display Name",
                                placeholder = "e.g., Alex Vance"
                            )

                            ShTextField(
                                value = editEmail,
                                onValueChange = { editEmail = it },
                                label = "Email Address (Optional)",
                                placeholder = "e.g., alex@example.com"
                            )

                            ShButton(
                                text = "Save Profile Changes",
                                onClick = {
                                    val trimmed = editName.trim()
                                    if (trimmed.isNotBlank()) {
                                        viewModel.saveUserName(trimmed)
                                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                        subView = SettingsSubView.MAIN
                                    } else {
                                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }

                // DEDICATED APPLICATION UPDATES SCREEN
                SettingsSubView.UPDATES -> {
                    com.vesper.ledger.ui.update.SettingsUpdatesScreen(updateViewModel)
                }

                // DEDICATED OPEN SOURCE LICENSES SCREEN
                SettingsSubView.OPEN_SOURCE -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "LIBRARIES & OPEN SOURCE CREDITS",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA1A1AA)
                        )

                        listOf(
                            Triple("Jetpack Compose", "Google LLC • Apache License 2.0", "https://developer.android.com/jetpack/compose"),
                            Triple("Material 3 Design System", "Google LLC • Apache License 2.0", "https://m3.material.io"),
                            Triple("fl_chart", "imaNNeoFighT • MIT License", "https://github.com/imaNNeoFighT/fl_chart"),
                            Triple("Kotlin Coroutines & Flow", "JetBrains s.r.o. • Apache License 2.0", "https://github.com/Kotlin/kotlinx.coroutines"),
                            Triple("Room SQLite Database", "Google LLC • Apache License 2.0", "https://developer.android.com/training/data-storage/room"),
                            Triple("CameraX", "Google LLC • Apache License 2.0", "https://developer.android.com/training/camerax"),
                            Triple("Space Grotesk & Plus Jakarta Sans", "Google Fonts • Open Font License", "https://fonts.google.com")
                        ).forEach { (name, desc, url) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF18181B))
                                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                                    .clickable { uriHandler.openUri(url) }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = desc,
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Launch,
                                        contentDescription = "Open URL",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // DEDICATED PRIVACY POLICY SCREEN (PLAY STORE COMPLIANT)
                SettingsSubView.PRIVACY -> {
                    LegalDocumentBox(
                        title = "PRIVACY POLICY",
                        lastUpdated = "Effective Date: July 2026",
                        sections = listOf(
                            "1. 100% On-Device Local Data Storage" to "Vesper Ledger operates entirely offline. All transactions, accounts, categories, and personal balance entries are saved exclusively on your local device SQLite database. We do not harvest, track, or upload your data to external servers.",
                            "2. Zero Telemetry & Zero Analytics" to "We respect your absolute privacy. Vesper Ledger contains zero analytics SDKs, zero remote tracking scripts, and zero advertising networks.",
                            "3. Device Permissions Disclosure" to "Optional device permissions (e.g. Camera access) are requested strictly for local receipt scanning features. Images are processed locally on your phone and never transmitted over the internet.",
                            "4. User Control & Data Retention" to "You have 100% control over your data. You can edit, export, or permanently wipe your ledger database at any time directly through the app settings."
                        )
                    )
                }

                // DEDICATED TERMS & CONDITIONS SCREEN (PLAY STORE COMPLIANT)
                SettingsSubView.TERMS -> {
                    LegalDocumentBox(
                        title = "TERMS AND CONDITIONS",
                        lastUpdated = "Effective Date: July 2026",
                        sections = listOf(
                            "1. Software Usage License" to "Vesper Ledger is provided as a personal financial ledger application for your personal use. You retain full ownership of all data generated using the software.",
                            "2. Financial Disclaimer" to "Vesper Ledger is a tool for logging expenses and tracking budgets. It does not provide professional financial, accounting, tax, or investment advice.",
                            "3. Local Backup Responsibility" to "Because all data is stored locally on your device, you are responsible for maintaining your own device backups to prevent data loss due to device damage or factory resets.",
                            "4. Limitation of Liability" to "The software is provided 'as is' without warranties of any kind. Under no circumstances shall the developer be liable for any inaccuracies in user-entered data."
                        )
                    )
                }

                // DEDICATED APP VERSION & WHAT'S NEW HISTORY SCREEN
                SettingsSubView.APP_VERSION -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF18181B))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "CURRENT VERSION: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Vesper Ledger Pure Dark Edition",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "CHANGELOG & RELEASE HISTORY",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA1A1AA)
                        )

                        listOf(
                            "v2.85.0" to listOf("Dedicated Profile, Open Source, Privacy & Legal screens", "Material 3 FAB exact 16dp edge alignment & 8dp gap", "Redesigned M3 Theme Selector Dialog"),
                            "v2.83.0" to listOf("Complete fl_chart Analytics Suite (Line, Bar, Donut, Radar, Scatter)", "Interactive chart tap tooltips & daily spending heatmaps"),
                            "v2.80.0" to listOf("Material 3 FAB & FAB Menu implementation", "Bouncy spring motion & press feedback animations"),
                            "v2.74.0" to listOf("Bento Grid Dashboard Architecture", "4-Tier Non-Blending Dark Surface contrast scale"),
                            "v2.70.0" to listOf("100% Room SQLite local database migration", "Shadcn-inspired minimal black theme")
                        ).forEach { (version, highlights) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF18181B))
                                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = version,
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    highlights.forEach { item ->
                                        Text(
                                            text = "• $item",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // DEDICATED ABOUT APP & DEVELOPER STORY SCREEN
                SettingsSubView.ABOUT -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF18181B))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "VESPER LEDGER STORY",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFF38BDF8)
                            )

                            Text(
                                text = "Vesper Ledger was created with a clear mission: to build an ultra-fast, 100% private, and visually breathtaking personal finance application for Android.\n\nDesigned with strict adherence to Material 3 guidelines and a sleek 4-tier non-blending dark surface contrast system, Vesper Ledger gives you complete sovereignty over your financial records without cloud telemetry or data harvesting.",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )

                            Divider(color = Color(0xFF27272A))

                            Text(
                                text = "QUICK NAVIGATION LINKS",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color(0xFFA1A1AA)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onCategoriesClick() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242429)),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46))
                                ) {
                                    Text("Categories", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Color.White)
                                }

                                Button(
                                    onClick = { onAccountsClick() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242429)),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3F3F46))
                                ) {
                                    Text("Accounts", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegalDocumentBox(
    title: String,
    lastUpdated: String,
    sections: List<Pair<String, String>>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF18181B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = title,
                fontFamily = SpaceGroteskFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = lastUpdated,
                fontFamily = SpaceGroteskFamily,
                fontSize = 11.sp,
                color = Color(0xFF38BDF8)
            )

            Divider(color = Color(0xFF27272A))

            sections.forEach { (heading, body) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = heading,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = body,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFFA1A1AA),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF18181B))
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    badgeText: String? = null,
    titleColor: Color = Color.White,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF242429)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (titleColor != Color.White) titleColor else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SpaceGroteskFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFA1A1AA),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
