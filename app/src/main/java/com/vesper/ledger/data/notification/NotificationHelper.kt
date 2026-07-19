package com.vesper.ledger.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vesper.ledger.MainActivity
import com.vesper.ledger.R
import com.vesper.ledger.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NotificationHelper {

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    "vesper_daily_reminders",
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminds you to log daily transactions and build healthy habit streaks."
                    enableVibration(true)
                },
                NotificationChannel(
                    "vesper_smart_suggestions",
                    "Smart Suggestions",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Delivers smart financial suggestions based on spending patterns."
                    enableVibration(true)
                },
                NotificationChannel(
                    "vesper_financial_insights",
                    "Financial Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Weekly summaries and monthly spending insights."
                    enableVibration(false)
                },
                NotificationChannel(
                    "vesper_goals_achievements",
                    "Goals & Achievements",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Celebrates goal milestones and streak achievements."
                    enableVibration(true)
                },
                NotificationChannel(
                    "vesper_security",
                    "Security",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts about local data backups and account safety warnings."
                    enableVibration(true)
                },
                NotificationChannel(
                    "vesper_system",
                    "System Events",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "System logs and database status messages."
                    enableVibration(false)
                },
                NotificationChannel(
                    "vesper_updates",
                    "Product Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifies about new feature releases and update announcements."
                    enableVibration(false)
                }
            )

            manager.createNotificationChannels(channels)
            Log.d("NotificationHelper", "All 7 notification channels initialized successfully.")
        }
    }

    /**
     * Dispatch standard Android notification and hook click/delete actions for engagement tracking.
     */
    fun dispatchNotification(
        context: Context,
        dbId: Long,
        title: String,
        message: String,
        category: NotificationCategory
    ) {
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_ID", dbId)
            putExtra("NOTIFICATION_ACTION", "CLICK")
            
            // Contextual routing extras based on notification type
            if (category == NotificationCategory.DAILY_REMINDER || category == NotificationCategory.FRIENDLY_REMINDER) {
                putExtra("EXTRA_ROUTE", "add_transaction")
            } else if (category == NotificationCategory.SMART_SUGGESTIONS || category == NotificationCategory.WEEKLY_SUMMARY) {
                putExtra("EXTRA_ROUTE", "reports")
            }
        }
        val clickPendingIntent = PendingIntent.getActivity(
            context,
            dbId.toInt(),
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "DISMISS_NOTIFICATION"
            putExtra("NOTIFICATION_ID", dbId)
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            dbId.toInt() + 20000,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, category.channelId)
            // Use unified premium shadcn logo icon for all notifications
            .setSmallIcon(com.vesper.ledger.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setGroup("com.vesper.ledger.NOTIFICATIONS")
            .setAutoCancel(true)

        // Choose Expandable vs non-expandable style based on length of message
        if (message.length > 50) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }

        // Heads-up / Silent / Standard priority mapping based on category/specs
        if (category == NotificationCategory.WARNING || category == NotificationCategory.BACKUP_REMINDER || category.channelId == "vesper_security") {
            // Heads-up: High priority popup banner
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        } else if (category.channelId == "vesper_system" || category.channelId == "vesper_updates" || category == NotificationCategory.WEEKLY_SUMMARY || category == NotificationCategory.MONTHLY_INSIGHT) {
            // Silent: Low priority, no vibration or sound
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
        } else {
            // Standard / Default priority
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        // Contextual action buttons based on notification type
        when (category) {
            NotificationCategory.DAILY_REMINDER, NotificationCategory.FRIENDLY_REMINDER -> {
                // Log Transaction Action
                val logIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("NOTIFICATION_ID", dbId)
                    putExtra("NOTIFICATION_ACTION", "CLICK")
                    putExtra("EXTRA_ROUTE", "add_transaction")
                }
                val logPendingIntent = PendingIntent.getActivity(
                    context,
                    dbId.toInt() + 1000,
                    logIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Snooze Action
                val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "SNOOZE"
                    putExtra("NOTIFICATION_ID", dbId)
                    putExtra("TX_ID", -1L)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    dbId.toInt() + 2000,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                builder.addAction(android.R.drawable.ic_menu_add, "Record Expense", logPendingIntent)
                builder.addAction(android.R.drawable.ic_lock_silent_mode, "Snooze", snoozePendingIntent)
            }

            NotificationCategory.BACKUP_REMINDER -> {
                // Trigger Secure Backup in background
                val backupIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                    action = "BACKUP_NOW"
                    putExtra("NOTIFICATION_ID", dbId)
                }
                val backupPendingIntent = PendingIntent.getBroadcast(
                    context,
                    dbId.toInt() + 3000,
                    backupIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                builder.addAction(android.R.drawable.ic_menu_save, "Backup Now", backupPendingIntent)
            }

            NotificationCategory.SMART_SUGGESTIONS -> {
                // View Reports/Analytics
                val viewIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("NOTIFICATION_ID", dbId)
                    putExtra("NOTIFICATION_ACTION", "CLICK")
                    putExtra("EXTRA_ROUTE", "reports")
                }
                val viewPendingIntent = PendingIntent.getActivity(
                    context,
                    dbId.toInt() + 4000,
                    viewIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(android.R.drawable.ic_menu_view, "View Analytics", viewPendingIntent)
            }
            else -> {}
        }

        try {
            NotificationManagerCompat.from(context).notify(dbId.toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to dispatch notification: Permission denied", e)
        }

        // Dynamically fetch unread list and dispatch an Inbox-style Group Summary notification if multiple unread exist
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val unreadCount = db.notificationHistoryDao().getUnreadCountSync()
                if (unreadCount > 1) {
                    val unreadList = db.notificationHistoryDao().getUnreadNotificationsSync()
                    
                    val inboxStyle = NotificationCompat.InboxStyle()
                        .setBigContentTitle("Vesper Ledger ($unreadCount)")
                        .setSummaryText("New alerts")
                    
                    unreadList.take(5).forEach { item ->
                        inboxStyle.addLine("• ${item.title}")
                    }

                    val summaryNotification = NotificationCompat.Builder(context, "vesper_system")
                        .setSmallIcon(com.vesper.ledger.R.mipmap.ic_launcher)
                        .setContentTitle("Vesper Ledger ($unreadCount)")
                        .setContentText("You have $unreadCount new updates")
                        .setStyle(inboxStyle)
                        .setGroup("com.vesper.ledger.NOTIFICATIONS")
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .build()

                    NotificationManagerCompat.from(context).notify(9999, summaryNotification)
                }
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Failed to build grouped notification summary", e)
            }
        }
    }

    /**
     * Dispatch progress-bar notifications for background long-running tasks.
     */
    fun dispatchProgressNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        progress: Int, // 0 to 100, or -1 for indeterminate
        isFinished: Boolean = false
    ) {
        val builder = NotificationCompat.Builder(context, "vesper_system")
            .setSmallIcon(com.vesper.ledger.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setOngoing(!isFinished)
            .setAutoCancel(isFinished)

        if (isFinished) {
            builder.setProgress(0, 0, false)
        } else {
            if (progress >= 0) {
                builder.setProgress(100, progress, false)
            } else {
                builder.setProgress(0, 0, true) // Indeterminate loading
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to dispatch progress notification", e)
        }
    }

    // Direct budget warn helpers
    fun showBudgetAlert(context: Context, categoryName: String, spent: Double, limit: Double, currencySymbol: String) {
        val remaining = limit - spent
        val ratio = spent / limit
        val percent = (ratio * 100).toInt()
        val filledBlocks = (percent / 10).coerceIn(0, 10)
        val bar = "█".repeat(filledBlocks) + "░".repeat(10 - filledBlocks)

        val text = if (remaining >= 0) {
            "Budget Alert: $categoryName: $bar $percent%\nSpent: $currencySymbol${spent.toInt()} / Limit: $currencySymbol${limit.toInt()}.\n$currencySymbol${remaining.toInt()} remaining."
        } else {
            "Budget Alert: $categoryName: $bar $percent%\nSpent: $currencySymbol${spent.toInt()} / Limit: $currencySymbol${limit.toInt()}.\nLimit exceeded by $currencySymbol${(-remaining).toInt()}!"
        }

        VesperNotificationApi.sendNotification(
            title = "Budget Warning: $categoryName",
            body = text,
            category = NotificationCategory.SMART_SUGGESTIONS
        )
    }

    fun showSavingsGoalAlert(context: Context, goalId: Long, goalName: String, current: Double, target: Double, currencySymbol: String) {
        val percent = (((current / target) * 100).toInt()).coerceIn(0, 100)
        val text = "Savings Milestone for '$goalName': $percent% achieved ($currencySymbol${current.toInt()} of $currencySymbol${target.toInt()})."
        
        VesperNotificationApi.sendNotification(
            title = "Savings Milestone!",
            body = text,
            category = NotificationCategory.ACHIEVEMENT
        )
    }

    fun showRecurringTransactionAlert(context: Context, txId: Long, txName: String, amount: Double, currencySymbol: String) {
        val text = "Your subscription '$txName' of $currencySymbol${amount.toInt()} is expected tomorrow."
        
        VesperNotificationApi.sendNotification(
            title = "Upcoming Payment Tomorrow",
            body = text,
            category = NotificationCategory.SMART_SUGGESTIONS
        )
    }

    fun showMissedEntryAlert(context: Context, title: String, text: String) {
        VesperNotificationApi.sendNotification(
            title = title,
            body = text,
            category = NotificationCategory.DAILY_REMINDER
        )
    }

    fun showEngagementAlert(context: Context, title: String, text: String) {
        VesperNotificationApi.sendNotification(
            title = title,
            body = text,
            category = NotificationCategory.MOTIVATION
        )
    }

    fun cancelAllBudgetAlerts(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
