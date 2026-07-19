package com.vesper.ledger.data.notification

import com.vesper.ledger.data.model.NotificationHistory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object InAppNotificationController {
    private val _notificationFlow = MutableSharedFlow<NotificationHistory>(extraBufferCapacity = 10)
    val notificationFlow: SharedFlow<NotificationHistory> = _notificationFlow

    fun postNotification(notification: NotificationHistory) {
        _notificationFlow.tryEmit(notification)
    }
}
