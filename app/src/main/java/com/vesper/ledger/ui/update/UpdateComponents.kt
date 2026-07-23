package com.vesper.ledger.ui.update

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
                            color = MaterialTheme.colorScheme.onBackground
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

                    ShButton(
                        text = "Close",
                        onClick = onDismissRequest,
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
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
                            UpdateType.MAJOR -> MaterialTheme.colorScheme.onBackground
                            UpdateType.FEATURE -> MaterialTheme.colorScheme.onSurface
                            UpdateType.STABILITY -> MaterialTheme.colorScheme.onSurfaceVariant
                            UpdateType.SECURITY -> MaterialTheme.colorScheme.onSurface
                            UpdateType.HOTFIX -> MaterialTheme.colorScheme.onSurfaceVariant
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
    val isUpdateAvailable = uiState.updateInfo != null && uiState.updateInfo!!.updateAvailable
    val lastCheckedText = viewModel.getLastCheckedTimeFormatted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        when {
            // STATE 3: Checking For Updates
            uiState.downloadState == UpdateDownloadState.CHECKING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Checking for updates...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // STATE 6: Error State
            uiState.downloadState == UpdateDownloadState.ERROR -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Unable To Check For Updates",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = uiState.errorMessage ?: "Please check your internet connection and try again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShButton(
                        text = "Retry",
                        onClick = { viewModel.checkForUpdates() }
                    )
                }
            }

            // STATE 4: Downloading
            uiState.downloadState == UpdateDownloadState.DOWNLOADING -> {
                val progress = uiState.downloadProgress
                val df = java.text.DecimalFormat("0.0")
                val downloadedMb = progress.bytesDownloaded / (1024f * 1024f)
                val totalMb = progress.totalBytes / (1024f * 1024f)

                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Downloading Update",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val animatedProgress by animateFloatAsState(targetValue = progress.progressFraction, label = "")
                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${df.format(downloadedMb)} MB / ${df.format(totalMb)} MB",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "${progress.progressPercent}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    if (progress.estimatedSecondsRemaining > 0) {
                        Text(
                            text = "Estimated time remaining: ${progress.estimatedSecondsRemaining}s",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // STATE 5: Ready To Install
            uiState.downloadState == UpdateDownloadState.DOWNLOADED || uiState.downloadState == UpdateDownloadState.INSTALLING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Update Ready",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "The update has finished downloading and is ready to install.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShButton(
                        text = if (uiState.downloadState == UpdateDownloadState.INSTALLING) "Installing..." else "Install Update",
                        onClick = { viewModel.installUpdate() },
                        enabled = uiState.downloadState != UpdateDownloadState.INSTALLING
                    )
                }
            }

            // STATE 1: Update Available
            isUpdateAvailable -> {
                val info = uiState.updateInfo!!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Update Available",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Version v${info.latestVersionName} (${info.latestVersionCode}) is available for download.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            MetadataRow(
                                label = "Current Version",
                                value = "${info.currentVersionName} (${info.currentVersionCode})"
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(
                                label = "Latest Version",
                                value = "${info.latestVersionName} (${info.latestVersionCode})"
                            )
                        }
                    }

                    if (info.changelog.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "What's New",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    info.changelog.forEach { entry ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = entry.type.icon,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = entry.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ShButton(
                        text = "Download Update",
                        onClick = { viewModel.startDownload() }
                    )
                }
            }

            // STATE 2: App Up To Date / Developer Preview
            else -> {
                val currentVersion = BuildConfig.VERSION_NAME
                val currentCode = BuildConfig.VERSION_CODE
                val latestVersion = uiState.updateInfo?.latestVersionName ?: currentVersion
                val latestCode = uiState.updateInfo?.latestVersionCode ?: currentCode

                val isNewerBuild = currentCode > latestCode
                val titleText = if (isNewerBuild) "Developer Preview" else "You're Up To Date"
                val descText = if (isNewerBuild) {
                    "Developer Preview Installed: Running newer build than release channel."
                } else {
                    "You're running the latest version of Vesper Ledger."
                }
                val iconColor = MaterialTheme.colorScheme.primary

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                tint = iconColor,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = descText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            MetadataRow(
                                label = "Current Version",
                                value = "$currentVersion ($currentCode)"
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(
                                label = "Latest Version",
                                value = "$latestVersion ($latestCode)"
                            )
                            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(
                                label = "Last Checked",
                                value = lastCheckedText
                            )
                        }
                    }

                    ShButton(
                        text = "Check Again",
                        onClick = { viewModel.checkForUpdates() }
                    )
                }
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
            fontFamily = if (label.contains("Version") || label.contains("Number") || label.contains("Current") || label.contains("Latest")) SpaceGroteskFamily else null,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun LogoBadge() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0D0E11))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.5.dp.toPx()

            // Left branch of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.18f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Right branch of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.18f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Stem of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Horizontal baseline
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.82f),
                end = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // White dot
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f)
            )
        }
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
    val df = java.text.DecimalFormat("0.0")
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
