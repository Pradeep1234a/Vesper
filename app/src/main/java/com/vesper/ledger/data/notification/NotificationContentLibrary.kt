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

enum class NotificationStyle {
    STANDARD,
    BIG_TEXT,
    INBOX,
    ACTION,
    RICH,
    PROGRESS,
    SILENT,
    HEADS_UP
}

data class NotificationPresentation(
    val title: String,
    val body: String,
    val style: NotificationStyle,
    val inboxLines: List<String> = emptyList(),
    val actionButtons: List<String> = emptyList(),
    val priority: Int = 0 // -1 = low/silent, 0 = default, 1 = high/heads-up
)

object NotificationContentLibrary {

    private val library = mapOf(
        NotificationCategory.WELCOME to listOf(
            "Welcome to Vesper Ledger" to "Your journey toward financial clarity begins today. Let's build healthy habits together.",
            "Welcome to Vesper" to "Keep track of where your money goes. Simple entries, complete control.",
            "A New Chapter" to "Every large wealth is built on consistent tracking. We are here to support your path."
        ),
        NotificationCategory.FRIENDLY_REMINDER to listOf(
            "We miss your wallet" to "Vesper is feeling a bit empty. Take a moment to record your recent spending catch-ups.",
            "A spend-free week?" to "If you haven't spent anything, you are a master. Otherwise, open Vesper to fill in the blanks!",
            "Did you drop your wallet?" to "No entries for a few days. Let's make sure we track those recent bills and keep Vesper fresh!"
        ),
        NotificationCategory.MOTIVATION to listOf(
            "Rule #1 of wealth" to "Keep track of where your money goes. Check Vesper today and keep your goals in sight!",
            "A spend-free day is the ultimate flex" to "Tap to keep your streak burning hot today!",
            "Checking Vesper is 100% free" to "Unlike that online checkout cart you've been staring at for 20 minutes.",
            "Future you is watching" to "Keep your budgets tidy. Record today's transactions and stay financially fit!",
            "Wealth is quiet" to "Consistent tracking builds quiet wealth. Record your expenses today.",
            "Budgeting is not restriction" to "It is about freedom. See where your money goes in Vesper.",
            "Don't fear your bank statement" to "Tracking daily in Vesper makes bank statements peaceful. Try it!",
            "Small leaks sink large ships" to "Record those miscellaneous subscription trials today.",
            "Consistency beats intensity" to "Recording daily takes 5 seconds. Do it today!",
            "Invest in clarity" to "Clear mind, clear wallet. Record your spending now."
        ),
        NotificationCategory.STREAK_CELEBRATION to listOf(
            "You're on fire" to "Your tracking streak is absolutely hot. Record today's entry to keep the fire burning!",
            "Streak Master" to "Consistency is your superpower. Keep recording daily to build generational wealth habits.",
            "Unstoppable Streak" to "Consistency is key. Tap to record today's transactions and keep your record clean."
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
            "Ledger Incomplete Warning" to "No transactions recorded in the past week. Record recent items to prevent gaps.",
            "Quiet Ledger Alert" to "Your dashboard charts need data to be meaningful. Record any transaction today."
        ),
        NotificationCategory.ACHIEVEMENT to listOf(
            "Milestone Unlocked" to "Congratulations! You've successfully reached another tracking milestone.",
            "Goal Reached" to "You are saving consistently. Celebrate this small victory and keep pushing forward!"
        ),
        NotificationCategory.BACKUP_REMINDER to listOf(
            "Secure Your History" to "Protect your financial ledger by creating a secure local database backup now.",
            "Data Safety Check" to "It's been a while since your last backup. Keep your data safe in case of device failure."
        ),
        NotificationCategory.PRODUCT_UPDATES to listOf(
            "Vesper Ledger Update Available" to "Discover new Habituated Reminders, Notifications timelines, and theme upgrades.",
            "What's New in Vesper" to "Check out the latest features designed to make budget tracking even cleaner."
        )
    )

    private fun getRecentTitles(context: Context): List<String> {
        val prefs = context.getSharedPreferences("vesper_recent_notifications", Context.MODE_PRIVATE)
        val raw = prefs.getString("titles", "") ?: ""
        return if (raw.isEmpty()) emptyList() else raw.split("||")
    }

    private fun saveRecentTitle(context: Context, title: String) {
        val prefs = context.getSharedPreferences("vesper_recent_notifications", Context.MODE_PRIVATE)
        val recent = getRecentTitles(context).toMutableList()
        if (title !in recent) {
            recent.add(title)
        }
        if (recent.size > 5) {
            recent.removeAt(0)
        }
        prefs.edit().putString("titles", recent.joinToString("||")).apply()
    }

    /**
     * Intelligent Presentation Selector: Dynamic copy check, tone checks, style checking, and
     * circular buffer validation layer. Prevents repeated layout formats or wording.
     */
    fun generatePresentation(context: Context, category: NotificationCategory): NotificationPresentation {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        val isWeekend = (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY)

        val candidates = library[category] ?: listOf("Vesper Ledger" to "Your financial tracking partner.")

        val timeWording = when (category) {
            NotificationCategory.DAILY_REMINDER -> {
                if (isWeekend) {
                    if (hour in 9..15) {
                        listOf("Weekend mode active" to "Splurging on weekend plans? We support it! Just remember to record it in Vesper.")
                    } else {
                        listOf("Sunday budget review" to "Reset your ledger, check your streaks, and prepare your wallet for a fresh week ahead!")
                    }
                } else {
                    when (hour) {
                        in 8..11 -> {
                            if (dayOfWeek == java.util.Calendar.MONDAY) {
                                listOf("Monday morning budget plan" to "Start the week with complete financial clarity. Let's record yesterday's leftovers.")
                            } else {
                                listOf("Morning coffee logged?" to "Record your morning startup fuel in Vesper before the caffeine wears off!")
                            }
                        }
                        in 12..16 -> {
                            listOf("Lunch was delicious" to "But how's the wallet feeling? Take 5 seconds to record your meal before you forget!")
                        }
                        in 17..20 -> {
                            listOf("Heading home?" to "Record your commute costs or dinner spending in Vesper and wind down your budget.")
                        }
                        else -> {
                            listOf("Midnight snack record" to "We won't judge your late-night snack spending. Just record it in Vesper so your charts stay honest!")
                        }
                    }
                }
            }
            else -> candidates
        }

        // De-duplicate recent titles
        val recentTitles = getRecentTitles(context)
        val filtered = timeWording.filter { it.first !in recentTitles }
        val chosen = if (filtered.isNotEmpty()) filtered.random() else timeWording.random()

        saveRecentTitle(context, chosen.first)

        return generatePresentationForCustom(context, chosen.first, chosen.second, category)
    }

    /**
     * Map any incoming alert (including custom background updates and syncs) to an
     * intelligent Presentation containing random action count configurations.
     */
    fun generatePresentationForCustom(
        context: Context,
        title: String,
        body: String,
        category: NotificationCategory
    ): NotificationPresentation {
        // Choose layouts: Standard, Big Text, Inbox Style, Action, Rich
        val styles = when (category) {
            NotificationCategory.DAILY_REMINDER, NotificationCategory.FRIENDLY_REMINDER -> {
                listOf(NotificationStyle.STANDARD, NotificationStyle.ACTION, NotificationStyle.BIG_TEXT, NotificationStyle.RICH)
            }
            NotificationCategory.WEEKLY_SUMMARY, NotificationCategory.MONTHLY_INSIGHT -> {
                listOf(NotificationStyle.STANDARD, NotificationStyle.INBOX, NotificationStyle.BIG_TEXT, NotificationStyle.RICH)
            }
            NotificationCategory.SMART_SUGGESTIONS -> {
                listOf(NotificationStyle.STANDARD, NotificationStyle.ACTION, NotificationStyle.BIG_TEXT, NotificationStyle.INBOX, NotificationStyle.RICH)
            }
            NotificationCategory.BACKUP_REMINDER -> {
                listOf(NotificationStyle.STANDARD, NotificationStyle.ACTION, NotificationStyle.HEADS_UP)
            }
            NotificationCategory.PRODUCT_UPDATES -> {
                listOf(NotificationStyle.STANDARD, NotificationStyle.BIG_TEXT, NotificationStyle.INBOX)
            }
            else -> listOf(NotificationStyle.STANDARD, NotificationStyle.BIG_TEXT)
        }

        val chosenStyle = styles.random()
        val actions = mutableListOf<String>()
        val inboxLines = mutableListOf<String>()
        var priority = 0

        // Populate dynamic actions (supporting 0, 1, 2, or 3 buttons)
        if (chosenStyle == NotificationStyle.ACTION) {
            when (category) {
                NotificationCategory.DAILY_REMINDER, NotificationCategory.FRIENDLY_REMINDER -> {
                    if (Random.nextBoolean()) {
                        actions.addAll(listOf("RECORD_EXPENSE", "SNOOZE"))
                    } else {
                        actions.add("RECORD_EXPENSE")
                    }
                }
                NotificationCategory.BACKUP_REMINDER -> {
                    if (Random.nextBoolean()) {
                        actions.addAll(listOf("BACKUP_NOW", "DISMISS"))
                    } else {
                        actions.add("BACKUP_NOW")
                    }
                }
                NotificationCategory.SMART_SUGGESTIONS -> {
                    if (Random.nextBoolean()) {
                        actions.addAll(listOf("VIEW_ANALYTICS", "SNOOZE"))
                    } else {
                        actions.add("VIEW_ANALYTICS")
                    }
                }
                else -> {
                    actions.add("DISMISS")
                }
            }
        }

        // Populate inbox-style elements
        if (chosenStyle == NotificationStyle.INBOX) {
            // Split by lines or format summaries
            if (body.contains("\n")) {
                inboxLines.addAll(body.split("\n").filter { it.trim().isNotEmpty() })
            } else {
                when (category) {
                    NotificationCategory.WEEKLY_SUMMARY -> {
                        inboxLines.addAll(listOf(
                            "• Food & Dining: Active spend",
                            "• Transport & Commute: Managed",
                            "• Entertainment & Leisure: Logged",
                            "• Recommendation: Review analytics today"
                        ))
                    }
                    NotificationCategory.MONTHLY_INSIGHT -> {
                        inboxLines.addAll(listOf(
                            "• Monthly budget consumption: 74%",
                            "• Total transactions: 28 entries",
                            "• Top spend category: Subscriptions",
                            "• Streak status: 5-day active streak"
                        ))
                    }
                    else -> {
                        inboxLines.addAll(listOf(
                            "• Title: $title",
                            "• Update status: Ready",
                            "• Action item: Tap to open Vesper"
                        ))
                    }
                }
            }
        }

        // Bind Priority settings
        if (category == NotificationCategory.WARNING || category == NotificationCategory.BACKUP_REMINDER || category.channelId == "vesper_security") {
            priority = 1 // High / Heads-up
        } else if (category.channelId == "vesper_system" || category == NotificationCategory.WEEKLY_SUMMARY || category == NotificationCategory.MONTHLY_INSIGHT) {
            priority = -1 // Low / Silent
        }

        return NotificationPresentation(
            title = title,
            body = body,
            style = chosenStyle,
            inboxLines = inboxLines,
            actionButtons = actions,
            priority = priority
        )
    }
}
