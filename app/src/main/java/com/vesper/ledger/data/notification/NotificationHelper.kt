package com.vesper.ledger.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.vesper.ledger.MainActivity
import com.vesper.ledger.R

object NotificationHelper {

    const val CHANNEL_BUDGET = "vesper_budget_alerts"
    const val CHANNEL_SAVINGS = "vesper_savings_progress"
    const val CHANNEL_RECURRING = "vesper_recurring_reminders"
    const val CHANNEL_ENGAGEMENT = "vesper_engagement"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET,
                "Budget Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when spending is close to or exceeds category budgets"
            }

            val savingsChannel = NotificationChannel(
                CHANNEL_SAVINGS,
                "Savings Progress",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Celebrates goal milestones and contributions"
            }

            val recurringChannel = NotificationChannel(
                CHANNEL_RECURRING,
                "Subscription Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you of expected upcoming bills and subscriptions"
            }

            val engagementChannel = NotificationChannel(
                CHANNEL_ENGAGEMENT,
                "Weekly Insights & Habits",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Delivers spending trend summaries and habit analytics"
            }

            manager.createNotificationChannels(
                listOf(budgetChannel, savingsChannel, recurringChannel, engagementChannel)
            )
        }
    }

    private fun getAsciiProgressBar(percent: Int): String {
        val filledBlocks = (percent / 10).coerceIn(0, 10)
        val emptyBlocks = 10 - filledBlocks
        return "█".repeat(filledBlocks) + "░".repeat(emptyBlocks)
    }

    fun showBudgetAlert(context: Context, categoryName: String, spent: Double, limit: Double, currencySymbol: String) {
        val percent = ((spent / limit) * 100).toInt()
        val bar = getAsciiProgressBar(percent)
        val remaining = limit - spent

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (remaining >= 0) {
            "Budget Alert\n$categoryName: $bar\n$currencySymbol${spent.toInt()} / $currencySymbol${limit.toInt()}\nOnly $currencySymbol${remaining.toInt()} remaining."
        } else {
            "Budget Alert\n$categoryName: $bar\n$currencySymbol${spent.toInt()} / $currencySymbol${limit.toInt()}\nBudget exceeded by $currencySymbol${(-remaining).toInt()}!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_BUDGET)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Budget Alert: $categoryName")
            .setContentText(if (remaining >= 0) "Only $currencySymbol${remaining.toInt()} remaining." else "Budget exceeded!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "View Budget", pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(categoryName.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Handled dynamically on permission model
        }
    }

    fun showSavingsGoalAlert(context: Context, goalId: Long, goalName: String, current: Double, target: Double, currencySymbol: String) {
        val percent = (((current / target) * 100).toInt()).coerceIn(0, 100)
        val bar = getAsciiProgressBar(percent)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = "Savings Goal Progress\n$goalName: $percent% Complete\n$bar\n$currencySymbol${current.toInt()} / $currencySymbol${target.toInt()}"

        val builder = NotificationCompat.Builder(context, CHANNEL_SAVINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Savings Update: $goalName")
            .setContentText("$percent% complete towards your target!")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Add Contribution", pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(goalId.toInt(), builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun showRecurringTransactionAlert(context: Context, txId: Long, txName: String, amount: Double, currencySymbol: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Paid Action
        val paidIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "MARK_PAID"
            putExtra("TX_ID", txId)
        }
        val paidPendingIntent = PendingIntent.getBroadcast(
            context, txId.toInt() * 10, paidIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("TX_ID", txId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, txId.toInt() * 10 + 1, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = "Expected Tomorrow:\n$txName Subscription\nAmount due: $currencySymbol${amount.toInt()}"

        val builder = NotificationCompat.Builder(context, CHANNEL_RECURRING)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Upcoming Payment: $txName")
            .setContentText("$currencySymbol${amount.toInt()} due tomorrow.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Mark Paid", paidPendingIntent)
            .addAction(0, "Snooze", snoozePendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(txId.toInt() + 10000, builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun showMissedEntryAlert(context: Context, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 3, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ENGAGEMENT)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(0, "Add Transaction", pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(9999, builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun showEngagementAlert(context: Context, title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 4, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ENGAGEMENT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(9998, builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun cancelAllBudgetAlerts(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Cancel arbitrary budget hashes
        manager.cancelAll()
    }
}
