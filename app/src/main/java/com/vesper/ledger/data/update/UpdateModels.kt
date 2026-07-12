package com.vesper.ledger.data.update

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val currentVersionCode: Int,
    val currentVersionName: String,
    val updateAvailable: Boolean,
    val updateType: UpdateType,
    val downloadUrl: String,
    val fileSizeBytes: Long,
    val changelog: List<ChangelogEntry>
)

enum class UpdateType(val label: String, val badge: String) {
    MAJOR("MAJOR UPDATE", "Major Update"),
    FEATURE("FEATURE UPDATE", "Feature Update"),
    STABILITY("STABILITY UPDATE", "Stability Update"),
    SECURITY("SECURITY UPDATE", "Security Update"),
    HOTFIX("HOTFIX", "Hotfix")
}

data class ChangelogEntry(
    val type: ChangeType,
    val description: String
)

enum class ChangeType(val label: String, val icon: String) {
    ADDED("New Features", "✨"),
    CHANGED("UI Changes", "🎨"),
    FIXED("Bug Fixes", "🐞"),
    IMPROVED("Improvements", "⚡"),
    REMOVED("Removed", "🗑️")
}

enum class UpdateDownloadState {
    IDLE,
    CHECKING,
    AVAILABLE,
    DOWNLOADING,
    DOWNLOADED,
    INSTALLING,
    INSTALLED,
    ERROR
}

data class DownloadProgress(
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val speedBytesPerSecond: Long = 0,
    val estimatedSecondsRemaining: Int = 0
) {
    val progressFraction: Float get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
    val progressPercent: Int get() = (progressFraction * 100).toInt()
}
