package com.vesper.ledger.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.model.NotificationHistory
import com.vesper.ledger.data.notification.VesperNotificationApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).notificationHistoryDao()

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)

    // Unread count badge flow
    val unreadCount: StateFlow<Int> = dao.getUnreadCount()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered notification list
    val notifications: StateFlow<List<NotificationHistory>> = combine(
        dao.getAllNotifications(),
        searchQuery,
        selectedCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = query.isBlank() || 
                    item.title.contains(query, ignoreCase = true) || 
                    item.message.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category == category
            matchesQuery && matchesCategory
        }
    }.flowOn(Dispatchers.IO)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markAsRead(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.markAllAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteNotificationById(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAllNotifications()
        }
    }

    fun trackOpened(id: Long) {
        VesperNotificationApi.trackOpened(getApplication(), id)
    }

    fun trackClicked(id: Long) {
        VesperNotificationApi.trackClicked(getApplication(), id)
    }

    fun triggerSync() {
        VesperNotificationApi.syncHistoryWithCloud(getApplication())
    }
}

class NotificationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            return NotificationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
