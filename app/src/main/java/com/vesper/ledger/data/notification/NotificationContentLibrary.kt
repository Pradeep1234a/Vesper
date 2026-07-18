package com.vesper.ledger.data.notification

import android.content.Context
import kotlin.random.Random

enum class NotificationCategory(val channelId: String, val label: String) {
    WELCOME("vesper_system", "Welcome"),
    DAILY_REMINDER("vesper_daily_reminders", "Daily Reminder"),
    FRIENDLY_REMINDER("vesper_daily_reminders", "Friendly Reminder"),
    MOTIVATION("vesper_goals_achievements", "Motivation"),
    STREAK_CELEBRATION("vesper_goals_achievements", "Streak Celebration"),
    WEEKLY_SUMMARY("vesper_financial_insights", "Weekly Summary"),
    MONTHLY_INSIGHT("vesper_financial_insights", "Monthly Insight"),
    SMART_SUGGESTIONS("vesper_smart_suggestions", "Smart Suggestions"),
    WARNING("vesper_system", "Warning"),
    ACHIEVEMENT("vesper_goals_achievements", "Achievement"),
    BACKUP_REMINDER("vesper_security", "Backup Reminder"),
    PRODUCT_UPDATES("vesper_updates", "Product Updates")
}

object NotificationContentLibrary {

    private val library = mapOf(
        NotificationCategory.WELCOME to listOf(
            "Welcome to Vesper Ledger" to "Your journey toward financial clarity begins today. Let's build healthy habits together.",
            "Welcome to Vesper!" to "Keep track of where your money goes. Simple entries, complete control.",
            "A New Chapter" to "Every large wealth is built on consistent tracking. We are here to support your path."
        ),
        NotificationCategory.DAILY_REMINDER to listOf(
            "Record Today's Entry" to "Your ledger is waiting for today's transactions. Take a minute to update it.",
            "Log Today's Spending" to "A quick update today keeps your finances clear tomorrow. Record any changes.",
            "Keep the Ledger Fresh" to "Record today's transactions while they are still fresh in your memory.",
            "Habit Builder" to "Every small entry builds better habits. Open Vesper to log today's activity.",
            "Financial Clarity" to "Staying consistent makes tracking effortless. Log today's spending in seconds."
        ),
        NotificationCategory.FRIENDLY_REMINDER to listOf(
            "It's been a few days" to "We haven't seen you recently. Let's continue building your tracking habit.",
            "Quick Catchup" to "Take a moment to record any recent expenses you made over the last few days.",
            "Mindful Finances" to "A complete log is a clear mind. Log your recent activity to stay on track."
        ),
        NotificationCategory.MOTIVATION to listOf(
            "Mindful Spending" to "Every recorded transaction brings you one step closer to financial clarity.",
            "Better Financial Habits" to "Simple tracking is the foundation of wealth. Keep going, your future self will thank you.",
            "Tracking Makes a Difference" to "Consistency over intensity. Small daily updates compound into powerful insights."
        ),
        NotificationCategory.STREAK_CELEBRATION to listOf(
            "Streaking Ahead!" to "Your tracking streak is alive and strong. Keep up this incredible habit!",
            "Habit Master" to "You've been logging entries consistently. Keep the streak going today!",
            "Unstoppable Streak" to "Consistency is key. Tap to log today's transactions and keep your record clean."
        ),
        NotificationCategory.WEEKLY_SUMMARY to listOf(
            "Weekly Report Ready" to "Your weekly spending trend is calculated. View which categories took the most.",
            "Your Week in Review" to "Take a look at your financial progress over the past 7 days."
        ),
        NotificationCategory.MONTHLY_INSIGHT to listOf(
            "Monthly Insight Available" to "A complete summary of this month's cash flow is ready for review.",
            "Monthly Review" to "Understand where your money went this month. Open Vesper to inspect."
        ),
        NotificationCategory.SMART_SUGGESTIONS to listOf(
            "Subscription Check" to "We noticed an upcoming bill. Make sure your account has enough coverage.",
            "Budget Alert Warning" to "You've consumed most of your category budget. Plan your upcoming purchases accordingly."
        ),
        NotificationCategory.WARNING to listOf(
            "Ledger Incomplete Warning" to "No transactions logged in the past week. Log recent items to prevent gaps.",
            "Quiet Ledger Alert" to "Your dashboard charts need data to be meaningful. Log any transaction today."
        ),
        NotificationCategory.ACHIEVEMENT to listOf(
            "Milestone Unlocked!" to "Congratulations! You've successfully reached another tracking milestone.",
            "Goal Reached" to "You are saving consistently. Celebrate this small victory and keep pushing forward!"
        ),
        NotificationCategory.BACKUP_REMINDER to listOf(
            "Secure Your History" to "Protect your financial ledger by creating a secure local database backup now.",
            "Data Safety Check" to "It's been a while since your last backup. Keep your data safe in case of device failure."
        ),
        NotificationCategory.PRODUCT_UPDATES to listOf(
            "Vesper Ledger v2.0" to "Discover new Habituated Reminders, Notifications timelines, and theme upgrades.",
            "What's New in Vesper" to "Check out the latest features designed to make budget tracking even cleaner."
        )
    )

    /**
     * Intelligently rotates unused variations for a category using SharedPreferences state tracking.
     */
    fun getNextVariation(context: Context, category: NotificationCategory): Pair<String, String> {
        val list = library[category] ?: return "Vesper Ledger" to "Your financial tracking partner."
        if (list.size <= 1) return list.first()

        val prefs = context.getSharedPreferences("vesper_notification_rotation", Context.MODE_PRIVATE)
        val key = "last_index_${category.name}"
        val lastIndex = prefs.getInt(key, -1)

        // Rotate index
        var nextIndex = lastIndex + 1
        if (nextIndex >= list.size) {
            nextIndex = 0
        }

        prefs.edit().putInt(key, nextIndex).apply()
        return list[nextIndex]
    }
}
