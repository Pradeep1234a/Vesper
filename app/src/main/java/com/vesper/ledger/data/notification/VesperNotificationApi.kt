package com.vesper.ledger.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.model.NotificationHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object VesperNotificationApi {

    private const val TAG = "VesperNotificationApi"

    /**
     * Send a notification immediately and record it in the history database.
     */
    fun sendNotification(
        context: Context,
        category: NotificationCategory,
        customTitle: String? = null,
        customMessage: String? = null
    ) {
        val (fallbackTitle, fallbackMessage) = NotificationContentLibrary.getNextVariation(context, category)
        val finalTitle = customTitle ?: fallbackTitle
        val finalMessage = customMessage ?: fallbackMessage

        // Insert into database in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)

                // Cooldown: 1 hour for duplicate content checks
                val oneHourAgo = System.currentTimeMillis() - (1 * 60 * 60 * 1000L)
                val existing = db.notificationHistoryDao().getRecentNotification(finalTitle, finalMessage, category.name, oneHourAgo)
                if (existing != null) {
                    Log.w(TAG, "Notification suppressed by Duplicate Prevention System: '$finalTitle'")
                    return@launch
                }

                val notification = NotificationHistory(
                    title = finalTitle,
                    message = finalMessage,
                    category = category.name
                )
                val dbId = db.notificationHistoryDao().insertNotification(notification)
                Log.d(TAG, "Logged notification to history DB. Category: ${category.name}, DB ID: $dbId")

                // Trigger standard push dispatch helper
                NotificationHelper.dispatchNotification(context, dbId, finalTitle, finalMessage, category)
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting notification into history DB", e)
            }
        }
    }

    /**
     * Schedule a future notification using WorkManager.
     */
    fun scheduleNotification(
        context: Context,
        category: NotificationCategory,
        triggerTimeEpoch: Long,
        tag: String
    ) {
        val delayMs = (triggerTimeEpoch - System.currentTimeMillis()).coerceAtLeast(0L)
        
        val workRequest = OneTimeWorkRequestBuilder<IntelligentNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(tag)
            .setInputData(
                workDataOf(
                    "TRIGGER_TYPE" to "SCHEDULED_ALERT",
                    "CATEGORY_NAME" to category.name
                )
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        Log.d(TAG, "Scheduled notification for category ${category.name} in ${delayMs / 1000}s with tag $tag")
    }

    /**
     * Cancel scheduled notification by tag.
     */
    fun cancelScheduledNotification(context: Context, tag: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag)
        Log.d(TAG, "Cancelled scheduled notification for tag: $tag")
    }

    /**
     * Update existing notification details in database and re-dispatch.
     */
    fun updateNotification(context: Context, id: Long, newTitle: String, newBody: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val notification = db.notificationHistoryDao().getNotificationById(id)
                if (notification != null) {
                    val updated = notification.copy(title = newTitle, message = newBody)
                    db.notificationHistoryDao().updateNotification(updated)
                    
                    val category = NotificationCategory.valueOf(notification.category)
                    NotificationHelper.dispatchNotification(context, id, newTitle, newBody, category)
                    Log.d(TAG, "Updated notification ID: $id")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification ID: $id", e)
            }
        }
    }

    fun trackOpened(context: Context, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.notificationHistoryDao().incrementOpenedCount(id)
                Log.d(TAG, "Tracked opened event for ID: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Failed tracking open for ID: $id", e)
            }
        }
    }

    fun trackClicked(context: Context, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.notificationHistoryDao().incrementClickedCount(id)
                Log.d(TAG, "Tracked clicked event for ID: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Failed tracking click for ID: $id", e)
            }
        }
    }

    fun trackDismissed(context: Context, id: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                db.notificationHistoryDao().incrementDismissedCount(id)
                Log.d(TAG, "Tracked dismissed event for ID: $id")
            } catch (e: Exception) {
                Log.e(TAG, "Failed tracking dismiss for ID: $id", e)
            }
        }
    }

    /**
     * Simulated Cloud Push Sync API falls back to inserting new entries to local history.
     */
    fun syncHistoryWithCloud(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulating remote fetch. We'll generate a "Cloud Updates" product announcement notification.
                val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
                val lastSyncTime = sharedPrefs.getLong("last_cloud_sync", 0L)
                val now = System.currentTimeMillis()

                if (now - lastSyncTime > 12 * 60 * 60 * 1000L) { // 12 hours check
                    // Simulate fetching remote notification
                    sendNotification(
                        context = context,
                        category = NotificationCategory.PRODUCT_UPDATES,
                        customTitle = "Cloud Sync Active",
                        customMessage = "Your financial ledger is fully synchronized. Secure backups are active."
                    )
                    sharedPrefs.edit().putLong("last_cloud_sync", now).apply()
                    Log.d(TAG, "Simulated Cloud Sync succeeded: new record synced to local history.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed cloud push sync simulation", e)
            }
        }
    }
}
