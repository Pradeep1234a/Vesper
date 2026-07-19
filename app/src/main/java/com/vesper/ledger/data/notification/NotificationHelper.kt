package com.vesper.ledger.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
     * Dispatch dynamic Android notifications using parsed category configuration.
     * RawCategory parameter maps to format: "CATEGORY_NAME;style=STYLE_NAME;actions=ACTION1,ACTION2"
     */
    fun dispatchNotification(
        context: Context,
        dbId: Long,
        title: String,
        message: String,
        rawCategory: String
    ) {
        // Parse rawCategory info
        val parts = rawCategory.split(";")
        val categoryName = parts.getOrNull(0) ?: "DAILY_REMINDER"
        val category = try {
            NotificationCategory.valueOf(categoryName)
        } catch (e: Exception) {
            NotificationCategory.DAILY_REMINDER
        }

        var styleStr = "STANDARD"
        val actions = mutableListOf<String>()

        parts.drop(1).forEach { part ->
            if (part.startsWith("style=")) {
                styleStr = part.substringAfter("style=")
            } else if (part.startsWith("actions=")) {
                val list = part.substringAfter("actions=")
                if (list.isNotEmpty()) {
                    actions.addAll(list.split(","))
                }
            }
        }

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("NOTIFICATION_ID", dbId)
            putExtra("NOTIFICATION_ACTION", "CLICK")
            
            // Default deep-links routing
            if (category == NotificationCategory.DAILY_REMINDER || category == NotificationCategory.FRIENDLY_REMINDER) {
                putExtra("EXTRA_ROUTE", "add_transaction")
            } else if (category == NotificationCategory.SMART_SUGGESTIONS || category == NotificationCategory.WEEKLY_SUMMARY || category == NotificationCategory.MONTHLY_INSIGHT) {
                putExtra("EXTRA_ROUTE", "reports")
            } else if (category == NotificationCategory.BACKUP_REMINDER) {
                putExtra("EXTRA_ROUTE", "settings")
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
            .setSmallIcon(com.vesper.ledger.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setGroup("com.vesper.ledger.NOTIFICATIONS")
            .setAutoCancel(true)

        // 1. Render style dynamically
        when (styleStr) {
            "BIG_TEXT" -> {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
            "INBOX" -> {
                val inboxStyle = NotificationCompat.InboxStyle().setBigContentTitle(title)
                // Split body lines or construct bulleted list
                val lines = message.split("\n").filter { it.trim().isNotEmpty() }
                if (lines.size > 1) {
                    lines.forEach { inboxStyle.addLine(it) }
                } else {
                    inboxStyle.addLine("• $message")
                    inboxStyle.addLine("• Track your goals daily")
                    inboxStyle.addLine("• Check insights for savings")
                }
                builder.setStyle(inboxStyle)
            }
            "RICH" -> {
                // Large text + summary format
                builder.setStyle(NotificationCompat.BigTextStyle()
                    .setBigContentTitle("⭐ Premium Insight: $title")
                    .bigText(message)
                    .setSummaryText("Vesper Premium Smart Engine")
                )
            }
        }

        // 2. Set Priority dynamically based on channel
        if (category == NotificationCategory.WARNING || category == NotificationCategory.BACKUP_REMINDER || category.channelId == "vesper_security") {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        } else if (category.channelId == "vesper_system" || category.channelId == "vesper_updates" || category == NotificationCategory.WEEKLY_SUMMARY || category == NotificationCategory.MONTHLY_INSIGHT) {
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        // 3. Attach actions dynamically
        actions.forEachIndexed { index, actionKey ->
            val actionIntent = when (actionKey) {
                "RECORD_EXPENSE" -> {
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("NOTIFICATION_ID", dbId)
                        putExtra("NOTIFICATION_ACTION", "CLICK")
                        putExtra("EXTRA_ROUTE", "add_transaction")
                    }
                }
                "VIEW_ANALYTICS" -> {
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("NOTIFICATION_ID", dbId)
                        putExtra("NOTIFICATION_ACTION", "CLICK")
                        putExtra("EXTRA_ROUTE", "reports")
                    }
                }
                "SETTINGS" -> {
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("NOTIFICATION_ID", dbId)
                        putExtra("NOTIFICATION_ACTION", "CLICK")
                        putExtra("EXTRA_ROUTE", "settings")
                    }
                }
                "BACKUP_NOW" -> {
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = "BACKUP_NOW"
                        putExtra("NOTIFICATION_ID", dbId)
                    }
                }
                "SNOOZE" -> {
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = "SNOOZE"
                        putExtra("NOTIFICATION_ID", dbId)
                    }
                }
                "DISMISS" -> {
                    Intent(context, NotificationActionReceiver::class.java).apply {
                        action = "DISMISS_NOTIFICATION"
                        putExtra("NOTIFICATION_ID", dbId)
                    }
                }
                else -> null
            }

            if (actionIntent != null) {
                val requestCode = dbId.toInt() + 10000 + (index * 500)
                val pending = if (actionKey == "BACKUP_NOW" || actionKey == "SNOOZE" || actionKey == "DISMISS") {
                    PendingIntent.getBroadcast(context, requestCode, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                } else {
                    PendingIntent.getActivity(context, requestCode, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                }
                val label = actionKey.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
                builder.addAction(0, label, pending)
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(dbId.toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to dispatch notification: Permission denied", e)
        }

        // 4. Update unread Inbox-Style summary dynamically
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
