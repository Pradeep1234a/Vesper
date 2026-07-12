package com.vesper.ledger.data.update

import android.content.Context
import com.vesper.ledger.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

class UpdateRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("vesper_update_prefs", Context.MODE_PRIVATE)
    private val updatesDir = File(context.getExternalFilesDir(null), "updates")

    init {
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }
        performInstalledUpdateCleanup()
    }

    fun performInstalledUpdateCleanup() {
        val downloadedCode = getDownloadedVersionCode()
        if (downloadedCode != -1 && BuildConfig.VERSION_CODE >= downloadedCode) {
            clearDownloadedVersion()
        }
    }

    // ── Preference Management ──
    fun shouldShowPopup(): Boolean {
        val lastShown = prefs.getLong("lastUpdatePopupShownAt", 0L)
        val now = System.currentTimeMillis()
        val ignoredCode = prefs.getInt("ignoredVersionCode", -1)
        
        // Don't show if shown in the last 24 hours (86400000 ms)
        if (now - lastShown < 86400000L) {
            return false
        }
        return true
    }

    fun markPopupShown() {
        prefs.edit().putLong("lastUpdatePopupShownAt", System.currentTimeMillis()).apply()
    }

    fun ignoreVersion(versionCode: Int) {
        prefs.edit().putInt("ignoredVersionCode", versionCode).apply()
    }

    fun setDownloadedVersion(versionCode: Int) {
        prefs.edit()
            .putInt("downloadedVersionCode", versionCode)
            .putLong("downloadCompletedTimestamp", System.currentTimeMillis())
            .apply()
    }

    fun getDownloadedVersionCode(): Int {
        return prefs.getInt("downloadedVersionCode", -1)
    }

    fun getDownloadCompletedTimestamp(): Long {
        return prefs.getLong("downloadCompletedTimestamp", 0L)
    }

    fun clearDownloadedVersion() {
        prefs.edit()
            .remove("downloadedVersionCode")
            .remove("downloadCompletedTimestamp")
            .apply()
        cleanupOldApks()
    }

    // ── APK File Management ──
    fun cleanupOldApks() {
        try {
            if (updatesDir.exists()) {
                updatesDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".apk")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDownloadedApkFile(): File? {
        val downloadedCode = getDownloadedVersionCode()
        if (downloadedCode == -1) return null

        // Check if 24 hours have passed since download completed
        val downloadTime = getDownloadCompletedTimestamp()
        if (System.currentTimeMillis() - downloadTime > 86400000L) {
            clearDownloadedVersion()
            return null
        }

        val apkFile = File(updatesDir, "update_$downloadedCode.apk")
        return if (apkFile.exists()) apkFile else null
    }

    // ── Check for Updates ──
    fun checkForUpdate(): AppUpdateInfo? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.github.com/repos/Pradeep1234a/Vesper/releases/latest")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "VesperLedgerApp")

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                return parseUpdateResponse(responseText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun parseUpdateResponse(json: String): AppUpdateInfo {
        val tagName = extractJsonString(json, "tag_name") ?: "1.0.0"
        val body = extractJsonString(json, "body") ?: ""
        
        // Clean release name tag ("v1.1.0" -> "1.1.0")
        val latestName = tagName.removePrefix("v").trim()
        val currentName = BuildConfig.VERSION_NAME

        // Extract versionCode from body or fallback to latest tag calculation or asset size
        // We look for a pattern like "versionCode = 285" or "Code: 285" in the body, otherwise fallback
        val latestCode = extractVersionCodeFromBody(body) ?: (BuildConfig.VERSION_CODE + 1) // default to current + 1 if check succeeded

        // Find browser_download_url for VesperLedger.apk
        val downloadUrl = extractBrowserDownloadUrl(json) ?: ""
        val fileSizeBytes = extractAssetSize(json)

        val updateAvailable = latestCode > BuildConfig.VERSION_CODE

        // Automatic update type detection
        val updateType = determineUpdateType(currentName, latestName, body)

        // Generate changelog
        val changelog = parseChangelog(body)

        // Automatically clean up old APKs if update is already installed
        if (!updateAvailable) {
            cleanupOldApks()
        }

        return AppUpdateInfo(
            latestVersionCode = latestCode,
            latestVersionName = latestName,
            currentVersionCode = BuildConfig.VERSION_CODE,
            currentVersionName = currentName,
            updateAvailable = updateAvailable,
            updateType = updateType,
            downloadUrl = downloadUrl,
            fileSizeBytes = fileSizeBytes,
            changelog = changelog
        )
    }

    private fun determineUpdateType(current: String, latest: String, body: String): UpdateType {
        if (body.contains("SECURITY", ignoreCase = true)) return UpdateType.SECURITY
        if (body.contains("STABILITY", ignoreCase = true)) return UpdateType.STABILITY

        val curParts = current.split(".")
        val latParts = latest.split(".")
        if (curParts.size >= 3 && latParts.size >= 3) {
            try {
                val curMajor = curParts[0].toInt()
                val latMajor = latParts[0].toInt()
                if (latMajor > curMajor) return UpdateType.MAJOR

                val curMinor = curParts[1].toInt()
                val latMinor = latParts[1].toInt()
                if (latMinor > curMinor) return UpdateType.FEATURE
            } catch (e: Exception) {
                // fallback
            }
        }
        return UpdateType.HOTFIX
    }

    private fun parseChangelog(body: String): List<ChangelogEntry> {
        val entries = mutableListOf<ChangelogEntry>()
        val lines = body.split("\n")
        
        var currentSection: ChangeType? = null

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Detect section headers
            val lower = trimmed.lowercase()
            when {
                lower.startsWith("added:") || lower.contains("added") && trimmed.startsWith("##") -> {
                    currentSection = ChangeType.ADDED
                    continue
                }
                lower.startsWith("changed:") || lower.contains("changed") && trimmed.startsWith("##") -> {
                    currentSection = ChangeType.CHANGED
                    continue
                }
                lower.startsWith("fixed:") || lower.contains("fixed") && trimmed.startsWith("##") -> {
                    currentSection = ChangeType.FIXED
                    continue
                }
                lower.startsWith("improved:") || lower.contains("improved") && trimmed.startsWith("##") -> {
                    currentSection = ChangeType.IMPROVED
                    continue
                }
                lower.startsWith("removed:") || lower.contains("removed") && trimmed.startsWith("##") -> {
                    currentSection = ChangeType.REMOVED
                    continue
                }
            }

            // Extract bullet point lists or parse labeled lines
            if (trimmed.startsWith("•") || trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val desc = trimmed.substring(1).trim()
                val section = currentSection ?: ChangeType.ADDED
                if (desc.isNotEmpty()) {
                    entries.add(ChangelogEntry(section, desc))
                }
            } else if (trimmed.startsWith("✨")) {
                entries.add(ChangelogEntry(ChangeType.ADDED, trimmed.substring(1).trim()))
            } else if (trimmed.startsWith("🎨")) {
                entries.add(ChangelogEntry(ChangeType.CHANGED, trimmed.substring(1).trim()))
            } else if (trimmed.startsWith("🐞")) {
                entries.add(ChangelogEntry(ChangeType.FIXED, trimmed.substring(1).trim()))
            } else if (trimmed.startsWith("⚡")) {
                entries.add(ChangelogEntry(ChangeType.IMPROVED, trimmed.substring(1).trim()))
            } else if (trimmed.startsWith("🗑️")) {
                entries.add(ChangelogEntry(ChangeType.REMOVED, trimmed.substring(1).trim()))
            } else {
                // If it's a single word/sentence under a section
                if (currentSection != null) {
                    entries.add(ChangelogEntry(currentSection, trimmed))
                }
            }
        }

        // If no sections detected, return a default list from body
        if (entries.isEmpty() && body.trim().isNotEmpty()) {
            body.split("\n").take(5).forEach { l ->
                val t = l.trim()
                if (t.isNotEmpty() && !t.startsWith("#") && !t.contains("|")) {
                    entries.add(ChangelogEntry(ChangeType.ADDED, t.removePrefix("-").removePrefix("•").trim()))
                }
            }
        }
        return entries
    }

    private fun extractVersionCodeFromBody(body: String): Int? {
        val patterns = listOf(
            Pattern.compile("versionCode\\s*=\\s*(\\d+)"),
            Pattern.compile("code\\s*:\\s*(\\d+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\((\\d+)\\)")
        )
        for (pattern in patterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                try {
                    return matcher.group(1)?.toInt()
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        return null
    }

    private fun extractJsonString(json: String, key: String): String? {
        val pattern = Pattern.compile("\"$key\"\\s*:\\s*\"([^\"]*)\"")
        val matcher = pattern.matcher(json)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun extractBrowserDownloadUrl(json: String): String? {
        // Look for browser_download_url that ends with VesperLedger.apk or has .apk
        val pattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]*\\.apk)\"")
        val matcher = pattern.matcher(json)
        if (matcher.find()) {
            return matcher.group(1)
        }
        
        // Fallback to any browser_download_url
        val fallbackPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]*)\"")
        val fallbackMatcher = fallbackPattern.matcher(json)
        if (fallbackMatcher.find()) {
            return fallbackMatcher.group(1)
        }
        return null
    }

    private fun extractAssetSize(json: String): Long {
        val pattern = Pattern.compile("\"size\"\\s*:\\s*(\\d+)")
        val matcher = pattern.matcher(json)
        if (matcher.find()) {
            try {
                return matcher.group(1)?.toLong() ?: 0L
            } catch (e: Exception) {
                // ignore
            }
        }
        return 0L
    }

    // ── Download APK ──
    fun downloadApk(url: String, versionCode: Int, onProgress: (DownloadProgress) -> Unit) {
        val apkFile = File(updatesDir, "update_$versionCode.apk")
        var connection: HttpURLConnection? = null
        var outputStream: FileOutputStream? = null
        try {
            val connectionUrl = URL(url)
            connection = connectionUrl.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned HTTP ${connection.responseCode} ${connection.responseMessage}")
            }

            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream
            outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalBytesRead = 0L
            val startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastUpdateTime >= 200 || totalBytesRead == totalBytes) {
                    val durationSeconds = (now - startTime) / 1000.0
                    val speed = if (durationSeconds > 0) (totalBytesRead / durationSeconds).toLong() else 0L
                    
                    val remainingBytes = totalBytes - totalBytesRead
                    val estimatedSeconds = if (speed > 0) (remainingBytes / speed).toInt() else 0

                    onProgress(
                        DownloadProgress(
                            bytesDownloaded = totalBytesRead,
                            totalBytes = totalBytes,
                            speedBytesPerSecond = speed,
                            estimatedSecondsRemaining = estimatedSeconds
                        )
                    )
                    lastUpdateTime = now
                }
            }

            // Successfully downloaded
            setDownloadedVersion(versionCode)

        } catch (e: Exception) {
            if (apkFile.exists()) apkFile.delete()
            throw e
        } finally {
            outputStream?.close()
            connection?.disconnect()
        }
    }
}
