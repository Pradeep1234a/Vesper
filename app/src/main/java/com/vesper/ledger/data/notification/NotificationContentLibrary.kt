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
            "Welcome to Vesper" to "Keep track of where your money goes. Simple entries, complete control.",
            "A New Chapter" to "Every large wealth is built on consistent tracking. We are here to support your path."
        ),
        NotificationCategory.FRIENDLY_REMINDER to listOf(
            "We miss your wallet" to "Vesper is feeling a bit empty. Take a moment to log your recent spending catch-ups.",
            "A spend-free week?" to "If you haven't spent anything, you are a master. Otherwise, open Vesper to fill in the blanks!",
            "Did you drop your wallet?" to "No entries for a few days. Let's make sure we track those recent bills and keep Vesper fresh!"
        ),
        NotificationCategory.MOTIVATION to listOf(
            "Rule #1 of wealth" to "Keep track of where your money goes. Check Vesper today and keep your goals in sight!",
            "A spend-free day is the ultimate flex" to "Tap to keep your streak burning hot today!",
            "Checking Vesper is 100% free" to "Unlike that online checkout cart you've been staring at for 20 minutes.",
            "Future you is watching" to "Keep your budgets tidy. Log today's transactions and stay financially fit!",
            "Wealth is quiet" to "Consistent tracking builds quiet wealth. Log your expenses today.",
            "Budgeting is not restriction" to "It is about freedom. See where your money goes in Vesper.",
            "Don't fear your bank statement" to "Tracking daily in Vesper makes bank statements peaceful. Try it!",
            "Small leaks sink large ships" to "Log those miscellaneous subscription trials today.",
            "Consistency beats intensity" to "Logging daily takes 5 seconds. Do it today!",
            "Invest in clarity" to "Clear mind, clear wallet. Log your spending now."
        ),
        NotificationCategory.STREAK_CELEBRATION to listOf(
            "You're on fire" to "Your tracking streak is absolutely hot. Log today's entry to keep the fire burning!",
            "Streak Master" to "Consistency is your superpower. Keep logging daily to build generational wealth habits.",
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
            "Milestone Unlocked" to "Congratulations! You've successfully reached another tracking milestone.",
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
     * Dynamically generates witty, contextual, Zomato/Swiggy-style alerts based on time-of-day,
     * day-of-week, and user context, falling back to randomized dynamic library rotation.
     */
    fun getNextVariation(context: Context, category: NotificationCategory): Pair<String, String> {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2, ...
        
        val isWeekend = (dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY)

        when (category) {
            NotificationCategory.DAILY_REMINDER -> {
                return if (isWeekend) {
                    if (hour in 9..15) {
                        "Weekend mode active" to "Splurging on weekend plans? We support it! Just remember to record it in Vesper."
                    } else {
                        "Sunday budget review" to "Reset your ledger, check your streaks, and prepare your wallet for a fresh week ahead!"
                    }
                } else {
                    when (hour) {
                        in 8..11 -> {
                            if (dayOfWeek == java.util.Calendar.MONDAY) {
                                "Monday morning budget plan" to "Start the week with complete financial clarity. Let's record yesterday's leftovers."
                            } else {
                                "Morning coffee logged?" to "Log your morning startup fuel in Vesper before the caffeine wears off!"
                            }
                        }
                        in 12..16 -> {
                            "Lunch was delicious" to "But how's the wallet feeling? Take 5 seconds to log your meal before you forget!"
                        }
                        in 17..20 -> {
                            "Heading home?" to "Record your commute costs or dinner spending in Vesper and wind down your budget."
                        }
                        else -> { // Late night / early morning
                            "Midnight snack log" to "We won't judge your late-night snack spending. Just log it in Vesper so your charts stay honest!"
                        }
                    }
                }
            }
            
            NotificationCategory.FRIENDLY_REMINDER -> {
                val variations = library[NotificationCategory.FRIENDLY_REMINDER] ?: listOf(
                    "We miss your wallet" to "Vesper is feeling a bit empty. Take a moment to log your recent spending catch-ups."
                )
                return variations[Random.nextInt(variations.size)]
            }
            
            NotificationCategory.MOTIVATION -> {
                val variations = library[NotificationCategory.MOTIVATION] ?: listOf(
                    "Rule #1 of wealth" to "Keep track of where your money goes."
                )
                return variations[Random.nextInt(variations.size)]
            }

            NotificationCategory.STREAK_CELEBRATION -> {
                val variations = library[NotificationCategory.STREAK_CELEBRATION] ?: listOf(
                    "You're on fire" to "Your tracking streak is absolutely hot."
                )
                return variations[Random.nextInt(variations.size)]
            }
            
            else -> {
                val list = library[category] ?: return "Vesper Ledger" to "Your financial tracking partner."
                if (list.size <= 1) return list.first()
                val randomIndex = Random.nextInt(list.size)
                return list[randomIndex]
            }
        }
    }
}
