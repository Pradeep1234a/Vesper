package com.vesper.ledger.ui.update

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.BuildConfig
import com.vesper.ledger.data.update.AppUpdateInfo
import com.vesper.ledger.data.update.ChangelogEntry
import com.vesper.ledger.data.update.DownloadProgress
import com.vesper.ledger.data.update.UpdateDownloadState
import com.vesper.ledger.data.update.UpdateRepository
import com.vesper.ledger.data.update.UpdateType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class UpdateUiState(
    val downloadState: UpdateDownloadState = UpdateDownloadState.IDLE,
    val updateInfo: AppUpdateInfo? = null,
    val downloadProgress: DownloadProgress = DownloadProgress(),
    val errorMessage: String? = null,
    val showUpdateDialog: Boolean = false,
    val showUpdateBottomSheet: Boolean = false
)

class UpdateViewModel(
    application: Application,
    private val updateRepository: UpdateRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    init {
        checkForUpdatesOnLaunch()
    }

    private fun checkForUpdatesOnLaunch() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(downloadState = UpdateDownloadState.CHECKING)

                val updateInfo = updateRepository.checkForUpdate()
                if (updateInfo != null && updateInfo.updateAvailable) {
                    // Check if we already have this version downloaded
                    val downloadedApk = updateRepository.getDownloadedApkFile()
                    val state = if (downloadedApk != null) {
                        UpdateDownloadState.DOWNLOADED
                    } else {
                        UpdateDownloadState.AVAILABLE
                    }

                    _uiState.value = _uiState.value.copy(
                        downloadState = state,
                        updateInfo = updateInfo,
                        showUpdateDialog = updateRepository.shouldShowPopup()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        downloadState = UpdateDownloadState.IDLE,
                        updateInfo = updateInfo
                    )
                    // Clean up any old APKs if we're already on latest
                    updateRepository.cleanupOldApks()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.IDLE,
                    errorMessage = e.message
                )
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.CHECKING,
                    errorMessage = null
                )

                val updateInfo = updateRepository.checkForUpdate()
                if (updateInfo != null && updateInfo.updateAvailable) {
                    val downloadedApk = updateRepository.getDownloadedApkFile()
                    val state = if (downloadedApk != null) {
                        UpdateDownloadState.DOWNLOADED
                    } else {
                        UpdateDownloadState.AVAILABLE
                    }
                    _uiState.value = _uiState.value.copy(
                        downloadState = state,
                        updateInfo = updateInfo
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        downloadState = UpdateDownloadState.IDLE,
                        updateInfo = updateInfo
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.ERROR,
                    errorMessage = "Failed to check for updates: ${e.message}"
                )
            }
        }
    }

    fun startDownload() {
        val info = _uiState.value.updateInfo ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.DOWNLOADING,
                    downloadProgress = DownloadProgress()
                )

                // Clean up old APKs before downloading
                updateRepository.cleanupOldApks()

                updateRepository.downloadApk(
                    url = info.downloadUrl,
                    versionCode = info.latestVersionCode,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(downloadProgress = progress)
                    }
                )

                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.DOWNLOADED
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    downloadState = UpdateDownloadState.ERROR,
                    errorMessage = "Download failed: ${e.message}"
                )
            }
        }
    }

    fun installUpdate() {
        val apkFile = updateRepository.getDownloadedApkFile() ?: return
        try {
            _uiState.value = _uiState.value.copy(downloadState = UpdateDownloadState.INSTALLING)

            val context = getApplication<Application>()
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                downloadState = UpdateDownloadState.ERROR,
                errorMessage = "Installation failed: ${e.message}"
            )
        }
    }

    fun dismissUpdateDialog() {
        updateRepository.markPopupShown()
        _uiState.value = _uiState.value.copy(showUpdateDialog = false)
    }

    fun pressLater() {
        updateRepository.markPopupShown()
        _uiState.value = _uiState.value.copy(showUpdateDialog = false)
    }

    fun showUpdateSheet() {
        _uiState.value = _uiState.value.copy(showUpdateBottomSheet = true)
    }

    fun dismissUpdateSheet() {
        _uiState.value = _uiState.value.copy(showUpdateBottomSheet = false)
    }

    fun showDialogManually() {
        if (_uiState.value.updateInfo?.updateAvailable == true) {
            _uiState.value = _uiState.value.copy(showUpdateDialog = true)
        }
    }
}

class UpdateViewModelFactory(
    private val application: Application,
    private val updateRepository: UpdateRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateViewModel::class.java)) {
            return UpdateViewModel(application, updateRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
