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

        // Map category type to generic small drawable icon
        val iconRes = when (category) {
            NotificationCategory.WELCOME -> android.R.drawable.ic_dialog_info
            NotificationCategory.DAILY_REMINDER -> android.R.drawable.ic_menu_edit
            NotificationCategory.FRIENDLY_REMINDER -> android.R.drawable.ic_menu_edit
            NotificationCategory.MOTIVATION -> android.R.drawable.ic_dialog_map
            NotificationCategory.STREAK_CELEBRATION -> android.R.drawable.ic_dialog_info
            NotificationCategory.WEEKLY_SUMMARY -> android.R.drawable.ic_menu_today
            NotificationCategory.MONTHLY_INSIGHT -> android.R.drawable.ic_menu_today
            NotificationCategory.SMART_SUGGESTIONS -> android.R.drawable.ic_dialog_alert
            NotificationCategory.WARNING -> android.R.drawable.ic_dialog_alert
            NotificationCategory.ACHIEVEMENT -> android.R.drawable.ic_dialog_info
            NotificationCategory.BACKUP_REMINDER -> android.R.drawable.ic_lock_lock
            NotificationCategory.PRODUCT_UPDATES -> android.R.drawable.ic_popup_sync
        }

        val builder = NotificationCompat.Builder(context, category.channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (category.channelId == "vesper_daily_reminders" || category.channelId == "vesper_security") 
                    NotificationCompat.PRIORITY_HIGH 
                else 
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(clickPendingIntent)
            .setDeleteIntent(deletePendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(dbId.toInt(), builder.build())
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Failed to dispatch notification: Permission denied", e)
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
            context = context,
            category = NotificationCategory.SMART_SUGGESTIONS,
            customTitle = "Budget Warning: $categoryName",
            customMessage = text
        )
    }

    fun showSavingsGoalAlert(context: Context, goalId: Long, goalName: String, current: Double, target: Double, currencySymbol: String) {
        val percent = (((current / target) * 100).toInt()).coerceIn(0, 100)
        val text = "Savings Milestone for '$goalName': $percent% achieved ($currencySymbol${current.toInt()} of $currencySymbol${target.toInt()})."
        
        VesperNotificationApi.sendNotification(
            context = context,
            category = NotificationCategory.ACHIEVEMENT,
            customTitle = "Savings Milestone!",
            customMessage = text
        )
    }

    fun showRecurringTransactionAlert(context: Context, txId: Long, txName: String, amount: Double, currencySymbol: String) {
        val text = "Your subscription '$txName' of $currencySymbol${amount.toInt()} is expected tomorrow."
        
        VesperNotificationApi.sendNotification(
            context = context,
            category = NotificationCategory.SMART_SUGGESTIONS,
            customTitle = "Upcoming Payment Tomorrow",
            customMessage = text
        )
    }

    fun showMissedEntryAlert(context: Context, title: String, text: String) {
        VesperNotificationApi.sendNotification(
            context = context,
            category = NotificationCategory.DAILY_REMINDER,
            customTitle = title,
            customMessage = text
        )
    }

    fun showEngagementAlert(context: Context, title: String, text: String) {
        VesperNotificationApi.sendNotification(
            context = context,
            category = NotificationCategory.MOTIVATION,
            customTitle = title,
            customMessage = text
        )
    }

    fun cancelAllBudgetAlerts(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }
}
