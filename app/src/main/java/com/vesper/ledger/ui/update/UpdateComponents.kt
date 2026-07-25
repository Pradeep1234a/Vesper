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
                .background(Color.Black.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF18181B))
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
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
                            color = Color.White
                        )
                        Text(
                            text = "✓ You're on the latest release",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF09090B))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetadataRow(label = "Current Version", value = "v${updateInfo.currentVersionName} (${updateInfo.currentVersionCode})")
                        MetadataRow(label = "Latest Version", value = "v${updateInfo.latestVersionName} (${updateInfo.latestVersionCode})")
                        MetadataRow(label = "Status", value = "Latest Version Installed")
                    }

                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "Close",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
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
                            color = Color.White
                        )

                        // Version Transition Pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF09090B))
                                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "v${updateInfo.currentVersionName}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1A1AA)
                            )
                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "v${updateInfo.latestVersionName}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        // Update Type Badge
                        val (badgeLabel, badgeBg, badgeTextColor) = when (updateInfo.updateType) {
                            UpdateType.MAJOR -> Triple("⚡ MAJOR RELEASE", Color(0xFF6366F1).copy(alpha = 0.2f), Color(0xFF818CF8))
                            UpdateType.FEATURE -> Triple("✨ FEATURE UPDATE", Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399))
                            UpdateType.STABILITY -> Triple("🛠️ STABILITY & FIXES", Color(0xFF38BDF8).copy(alpha = 0.2f), Color(0xFF38BDF8))
                            UpdateType.SECURITY -> Triple("🔒 SECURITY UPDATE", Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFFBBF24))
                            UpdateType.HOTFIX -> Triple("🔥 HOTFIX", Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFF87171))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .border(1.dp, badgeTextColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                    }

                    // What's New Section (Formatted Release Notes)
                    if (updateInfo.changelog.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "What's New in this Update",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFA1A1AA)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF09090B))
                                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
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
                                                    fontFamily = SpaceGroteskFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF38BDF8)
                                                )
                                            }
                                            items(list) { entry ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFA1A1AA)
                                                    )
                                                    Text(
                                                        text = entry.description,
                                                        fontFamily = SpaceGroteskFamily,
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        lineHeight = 16.sp
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
        containerColor = Color(0xFF18181B),
        shape = RoundedCornerShape(6.dp),
        tonalElevation = 0.dp,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFF27272A)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
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
                        color = Color.White
                    )
                    Text(
                        text = updateInfo.updateType.label,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
            }

            // Changelog Box
            if (updateInfo.changelog.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.35f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF09090B))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
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
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                items(list) { entry ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("•", fontSize = 12.sp, color = Color(0xFFA1A1AA))
                                        Text(entry.description, fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
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
    val isUpdateAvailable = uiState.updateInfo != null && uiState.updateInfo?.updateAvailable == true
    val lastCheckedText = viewModel.getLastCheckedTimeFormatted()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when {
            // Checking For Updates State
            uiState.downloadState == UpdateDownloadState.CHECKING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(42.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "Checking for updates...",
                        fontFamily = SpaceGroteskFamily,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFA1A1AA))
                    )
                }
            }

            // Error State
            uiState.downloadState == UpdateDownloadState.ERROR -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Unable to Check For Updates",
                        fontFamily = SpaceGroteskFamily,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = uiState.errorMessage ?: "Please check your internet connection and try again.",
                        fontFamily = SpaceGroteskFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFA1A1AA),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(0.88f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.checkForUpdates() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Retry", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Downloading State
            uiState.downloadState == UpdateDownloadState.DOWNLOADING -> {
                val progress = uiState.downloadProgress
                val df = DecimalFormat("0.0")
                val downloadedMb = progress.bytesDownloaded / (1024f * 1024f)
                val totalMb = progress.totalBytes / (1024f * 1024f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Downloading Update Package",
                        fontFamily = SpaceGroteskFamily,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    DownloadingPillProgress(progress)
                }
            }

            // Ready To Install State
            uiState.downloadState == UpdateDownloadState.DOWNLOADED || uiState.downloadState == UpdateDownloadState.INSTALLING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SystemUpdate,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Update Ready to Install",
                        fontFamily = SpaceGroteskFamily,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "The latest update package is downloaded and ready.",
                        fontFamily = SpaceGroteskFamily,
                        color = Color(0xFFA1A1AA),
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = { viewModel.installUpdate() },
                        enabled = uiState.downloadState != UpdateDownloadState.INSTALLING,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black)
                    ) {
                        Text(
                            text = if (uiState.downloadState == UpdateDownloadState.INSTALLING) "Installing..." else "Install Update Now",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Update Available State
            isUpdateAvailable -> {
                val info = uiState.updateInfo!!
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "New Version Available",
                                fontFamily = SpaceGroteskFamily,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Version v${info.latestVersionName} (${info.latestVersionCode}) is available for download.",
                                fontFamily = SpaceGroteskFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA1A1AA),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MetadataRow(label = "Current Version", value = "${info.currentVersionName} (${info.currentVersionCode})")
                            Divider(color = Color(0xFF27272A), modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(label = "Latest Version", value = "${info.latestVersionName} (${info.latestVersionCode})")
                        }
                    }

                    Button(
                        onClick = { viewModel.startDownload() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Download Update", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Up To Date State
            else -> {
                val currentVersion = BuildConfig.VERSION_NAME
                val currentCode = BuildConfig.VERSION_CODE
                val latestVersion = uiState.updateInfo?.latestVersionName ?: currentVersion
                val latestCode = uiState.updateInfo?.latestVersionCode ?: currentCode

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                tint = Color(0xFF10B981),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Vesper Ledger is Up To Date",
                                fontFamily = SpaceGroteskFamily,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "You are running the latest version with all recent performance and security updates.",
                                fontFamily = SpaceGroteskFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA1A1AA),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp)),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MetadataRow(label = "Current Version", value = "$currentVersion ($currentCode)")
                            Divider(color = Color(0xFF27272A), modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(label = "Latest Version", value = "$latestVersion ($latestCode)")
                            Divider(color = Color(0xFF27272A), modifier = Modifier.padding(vertical = 4.dp))
                            MetadataRow(label = "Last Checked", value = lastCheckedText)
                        }
                    }

                    Button(
                        onClick = { viewModel.checkForUpdates() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Check Again", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            color = Color(0xFFA1A1AA)
        )
        Text(
            text = value,
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun LogoBadge() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF09090B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
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
                Button(
                    onClick = onDownloadClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(
                        text = "Download Update",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                if (onLaterClick != null) {
                    Text(
                        text = "Remind Me Later",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFA1A1AA),
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
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onInstallClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.Black)
                    ) {
                        Text(
                            text = "Install Update Now",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
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
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text(
                            text = "Retry Download",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
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
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF09090B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloading Update...",
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${progress.progressPercent}%",
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
        }

        val animatedProgress by animateFloatAsState(targetValue = progress.progressFraction, label = "")
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = Color(0xFF38BDF8),
            trackColor = Color(0xFF27272A)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${df.format(downloadedMb)} MB / ${df.format(totalMb)} MB",
                fontFamily = SpaceGroteskFamily,
                fontSize = 10.sp,
                color = Color(0xFFA1A1AA)
            )
            Text(
                text = "${df.format(speedMb)} MB/s • ${progress.estimatedSecondsRemaining}s left",
                fontFamily = SpaceGroteskFamily,
                fontSize = 10.sp,
                color = Color(0xFFA1A1AA)
            )
        }
    }
}

@Composable
fun InstallingAnimationState() {
    var stateIndex by remember { mutableStateOf(0) }
    val states = listOf("Preparing Update Package...", "Verifying Signature...", "Launching Installer...")

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            stateIndex = (stateIndex + 1) % states.size
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF09090B))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(6.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.5.dp,
            color = Color(0xFF10B981)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = states[stateIndex],
            fontFamily = SpaceGroteskFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
