package com.vesper.ledger.data.local

import androidx.room.*
import com.vesper.ledger.data.model.NotificationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {

    @Query("SELECT * FROM notification_history WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationHistory>>

    @Query("SELECT COUNT(*) FROM notification_history WHERE isRead = 0 AND isDeleted = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notification_history WHERE id = :id")
    suspend fun getNotificationById(id: Long): NotificationHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationHistory): Long

    @Update
    suspend fun updateNotification(notification: NotificationHistory)

    @Query("UPDATE notification_history SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notification_history SET isRead = 1 WHERE isRead = 0 AND isDeleted = 0")
    suspend fun markAllAsRead()

    @Query("UPDATE notification_history SET isDeleted = 1 WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("UPDATE notification_history SET isDeleted = 1")
    suspend fun clearAllNotifications()

    @Query("UPDATE notification_history SET openedCount = openedCount + 1, isRead = 1 WHERE id = :id")
    suspend fun incrementOpenedCount(id: Long)

    @Query("UPDATE notification_history SET clickedCount = clickedCount + 1 WHERE id = :id")
    suspend fun incrementClickedCount(id: Long)

    @Query("UPDATE notification_history SET dismissedCount = dismissedCount + 1 WHERE id = :id")
    suspend fun incrementDismissedCount(id: Long)

    @Query("SELECT * FROM notification_history WHERE title = :title AND message = :message AND category = :category AND timestamp >= :sinceTime LIMIT 1")
    suspend fun getRecentNotification(title: String, message: String, category: String, sinceTime: Long): NotificationHistory?

    @Query("SELECT COUNT(*) FROM notification_history WHERE isRead = 0 AND isDeleted = 0")
    suspend fun getUnreadCountSync(): Int

    @Query("SELECT * FROM notification_history WHERE isRead = 0 AND isDeleted = 0 ORDER BY timestamp DESC")
    suspend fun getUnreadNotificationsSync(): List<NotificationHistory>
}
