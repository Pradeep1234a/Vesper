package com.vesper.ledger.ui.update

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vesper.ledger.BuildConfig
import com.vesper.ledger.data.update.AppUpdateInfo
import com.vesper.ledger.data.update.ChangeType
import com.vesper.ledger.data.update.ChangelogEntry
import com.vesper.ledger.data.update.DownloadProgress
import com.vesper.ledger.data.update.UpdateDownloadState
import com.vesper.ledger.data.update.UpdateType
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    progress: DownloadProgress,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onLaterClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!updateInfo.updateAvailable) {
                    // ── Up To Date Dialog ──
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogoBadge()
                        Text(
                            text = "Vesper Ledger",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "✓ You're up to date",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A) // Green
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetadataRow(label = "Current Version", value = "v${updateInfo.currentVersionName} (${updateInfo.currentVersionCode})")
                        MetadataRow(label = "Latest Version", value = "v${updateInfo.latestVersionName} (${updateInfo.latestVersionCode})")
                        MetadataRow(label = "Status", value = "Latest Version Installed")
                    }

                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // ── Update Available Dialog ──
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LogoBadge()
                        Text(
                            text = "Vesper Ledger",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Update Available",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Versions
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "v${updateInfo.currentVersionName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "v${updateInfo.latestVersionName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Update Type Badge
                        val badgeColor = when (updateInfo.updateType) {
                            UpdateType.MAJOR -> Color(0xFFDC2626) // Red
                            UpdateType.FEATURE -> Color(0xFF2563EB) // Blue
                            UpdateType.STABILITY -> Color(0xFF71717A) // Gray
                            UpdateType.SECURITY -> Color(0xFFD97706) // Amber
                            UpdateType.HOTFIX -> Color(0xFF16A34A) // Green
                        }
                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = updateInfo.updateType.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }

                    // What's New Section (Scrollable list if long)
                    if (updateInfo.changelog.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "What's New",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                    .padding(10.dp)
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val grouped = updateInfo.changelog.groupBy { it.type }
                                    ChangeType.values().forEach { type ->
                                        val list = grouped[type]
                                        if (!list.isNullOrEmpty()) {
                                            item {
                                                Text(
                                                    text = "${type.icon} ${type.label}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            items(list) { entry ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = entry.description,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        lineHeight = 15.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action Area
                    UpdateActionArea(
                        downloadState = downloadState,
                        progress = progress,
                        onDownloadClick = onDownloadClick,
                        onInstallClick = onInstallClick,
                        onLaterClick = onLaterClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    progress: DownloadProgress,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoBadge()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vesper Ledger v${updateInfo.latestVersionName}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = updateInfo.updateType.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Changelog Box
            if (updateInfo.changelog.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val grouped = updateInfo.changelog.groupBy { it.type }
                        ChangeType.values().forEach { type ->
                            val list = grouped[type]
                            if (!list.isNullOrEmpty()) {
                                item {
                                    Text(
                                        text = "${type.icon} ${type.label}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                items(list) { entry ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(entry.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action
            UpdateActionArea(
                downloadState = downloadState,
                progress = progress,
                onDownloadClick = onDownloadClick,
                onInstallClick = onInstallClick,
                onLaterClick = null
            )
        }
    }
}

@Composable
fun SettingsUpdatesScreen(
    viewModel: UpdateViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val dfTime = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var lastCheckedText by remember { mutableStateOf("Today, " + dfTime.format(Date())) }

    LaunchedEffect(uiState.downloadState) {
        if (uiState.downloadState != UpdateDownloadState.CHECKING) {
            lastCheckedText = "Today, ${dfTime.format(Date())}"
        }
    }

    // Status mapping
    val statusText: String
    val statusColor: Color
    val isUpdateAvailable = uiState.updateInfo != null && uiState.updateInfo!!.updateAvailable

    when {
        uiState.downloadState == UpdateDownloadState.CHECKING -> {
            statusText = "Checking..."
            statusColor = MaterialTheme.colorScheme.primary
        }
        isUpdateAvailable -> {
            statusText = when (uiState.downloadState) {
                UpdateDownloadState.DOWNLOADED -> "Update Ready To Install"
                UpdateDownloadState.DOWNLOADING -> "Downloading..."
                UpdateDownloadState.INSTALLING -> "Installing..."
                UpdateDownloadState.ERROR -> "Download Failed"
                else -> "Update Available"
            }
            statusColor = if (uiState.downloadState == UpdateDownloadState.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        }
        else -> {
            statusText = "✓ Up To Date"
            statusColor = Color(0xFF16A34A) // Green
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Status Header Card
        ShCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(statusColor.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, statusColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = if (statusText.contains("Up To Date") || statusText.contains("✓")) Icons.Outlined.Check else Icons.Outlined.Info
                    Icon(icon, null, tint = statusColor, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(
                        text = if (isUpdateAvailable) "Update Available" else "✓ Up To Date",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isUpdateAvailable) "A new version of Vesper Ledger is available." else "You are running the latest stable version.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Version Details Card
        ShCard {
            val latestName = uiState.updateInfo?.latestVersionName ?: BuildConfig.VERSION_NAME
            val latestCode = uiState.updateInfo?.latestVersionCode ?: BuildConfig.VERSION_CODE
            
            MetadataRow(label = "Version", value = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            if (isUpdateAvailable) {
                MetadataRow(label = "Latest", value = "v$latestName ($latestCode)")
            }
            MetadataRow(label = "Status", value = statusText)
            MetadataRow(label = "Last Checked", value = lastCheckedText)
        }

        // Changelog / What's New section inside SettingsUpdatesScreen if update available
        if (isUpdateAvailable && uiState.updateInfo!!.changelog.isNotEmpty()) {
            Text(
                text = "What's New",
                fontFamily = SpaceGroteskFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            ShCard {
                val grouped = uiState.updateInfo!!.changelog.groupBy { it.type }
                ChangeType.values().forEach { type ->
                    val list = grouped[type]
                    if (!list.isNullOrEmpty()) {
                        Text(
                            text = "${type.icon} ${type.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        list.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = entry.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Panel Card with single action button matching states
        ShCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttonText: String
                val isButtonEnabled: Boolean
                val buttonAction: () -> Unit
                
                when {
                    uiState.downloadState == UpdateDownloadState.CHECKING -> {
                        buttonText = "Checking..."
                        isButtonEnabled = false
                        buttonAction = {}
                    }
                    uiState.downloadState == UpdateDownloadState.INSTALLING -> {
                        buttonText = "Installing..."
                        isButtonEnabled = false
                        buttonAction = {}
                    }
                    uiState.downloadState == UpdateDownloadState.DOWNLOADING -> {
                        buttonText = "Downloading..."
                        isButtonEnabled = false
                        buttonAction = {}
                    }
                    uiState.downloadState == UpdateDownloadState.DOWNLOADED -> {
                        buttonText = "Install Update"
                        isButtonEnabled = true
                        buttonAction = { viewModel.installUpdate() }
                    }
                    uiState.updateInfo != null && uiState.updateInfo!!.updateAvailable -> {
                        buttonText = "Download Update"
                        isButtonEnabled = true
                        buttonAction = { viewModel.startDownload() }
                    }
                    uiState.updateInfo != null && !uiState.updateInfo!!.updateAvailable -> {
                        buttonText = "Up To Date"
                        isButtonEnabled = false
                        buttonAction = {}
                    }
                    else -> {
                        buttonText = "Check for Updates"
                        isButtonEnabled = true
                        buttonAction = { viewModel.checkForUpdates() }
                    }
                }
                
                if (uiState.downloadState == UpdateDownloadState.DOWNLOADING) {
                    DownloadingPillProgress(uiState.downloadProgress)
                } else {
                    Button(
                        onClick = buttonAction,
                        enabled = isButtonEnabled,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = buttonText,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                val statusTextHelper = when {
                    uiState.downloadState == UpdateDownloadState.CHECKING -> "Connecting to GitHub..."
                    uiState.downloadState == UpdateDownloadState.DOWNLOADING -> "Downloading update APK..."
                    uiState.downloadState == UpdateDownloadState.DOWNLOADED -> "Ready to install."
                    uiState.updateInfo != null && uiState.updateInfo!!.updateAvailable -> "An update is available for download."
                    uiState.updateInfo != null && !uiState.updateInfo!!.updateAvailable -> "Latest version is installed."
                    else -> "Check if a new version is available."
                }
                
                Text(
                    text = statusTextHelper,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = if (label.contains("Version") || label.contains("Number")) SpaceGroteskFamily else null,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ── Shared UI Helpers ──

@Composable
fun LogoBadge() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "V",
            fontFamily = SpaceGroteskFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UpdateActionArea(
    downloadState: UpdateDownloadState,
    progress: DownloadProgress,
    onDownloadClick: () -> Unit,
    onInstallClick: () -> Unit,
    onLaterClick: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (downloadState) {
            UpdateDownloadState.AVAILABLE -> {
                ShButton(
                    text = "Download Update",
                    onClick = onDownloadClick,
                    modifier = Modifier.fillMaxWidth()
                )
                if (onLaterClick != null) {
                    Text(
                        text = "Later",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { onLaterClick() }
                            .padding(vertical = 4.dp)
                    )
                }
            }
            UpdateDownloadState.DOWNLOADING -> {
                DownloadingPillProgress(progress)
            }
            UpdateDownloadState.DOWNLOADED -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Update Ready To Install",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    ShButton(
                        text = "Install Update",
                        onClick = onInstallClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            UpdateDownloadState.INSTALLING -> {
                InstallingAnimationState()
            }
            UpdateDownloadState.ERROR -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Download failed. Please try again.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    ShButton(
                        text = "Retry Update",
                        onClick = onDownloadClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
fun DownloadingPillProgress(progress: DownloadProgress) {
    val df = DecimalFormat("0.0")
    val speedMb = progress.speedBytesPerSecond / (1024f * 1024f)
    val downloadedMb = progress.bytesDownloaded / (1024f * 1024f)
    val totalMb = progress.totalBytes / (1024f * 1024f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloading Update",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${progress.progressPercent}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Pill Progress Bar
        val animatedProgress by animateFloatAsState(targetValue = progress.progressFraction, label = "")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${df.format(downloadedMb)} MB / ${df.format(totalMb)} MB",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${df.format(speedMb)} MB/s • ${progress.estimatedSecondsRemaining}s left",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InstallingAnimationState() {
    var stateIndex by remember { mutableStateOf(0) }
    val states = listOf("Preparing Update...", "Installing...", "Optimizing Application...")

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2500)
            stateIndex = (stateIndex + 1) % states.size
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = states[stateIndex],
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
