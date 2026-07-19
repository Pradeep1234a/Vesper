package com.vesper.ledger.data.notification

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

class IntelligentNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val triggerType = inputData.getString("TRIGGER_TYPE")
        val app = applicationContext as VesperApplication
        val sharedPrefs = app.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)

        // Read settings toggles
        val missedEntryReminderEnabled = sharedPrefs.getBoolean("missedEntryReminder", true)
        val budgetReminderEnabled = sharedPrefs.getBoolean("budgetReminder", true)
        val recurringReminderEnabled = sharedPrefs.getBoolean("recurringReminder", true)
        val weeklySummaryEnabled = sharedPrefs.getBoolean("weeklySummaryReminder", true)

        val currencySymbol = sharedPrefs.getString("currency", "$") ?: "$"

        Log.d("IntelligentNotifyWrk", "Worker active. TriggerType: $triggerType, BudgetEnabled: $budgetReminderEnabled")

        if (triggerType == "SCHEDULED_ALERT") {
            val catName = inputData.getString("CATEGORY_NAME")
            val customTitle = inputData.getString("CUSTOM_TITLE")
            val customBody = inputData.getString("CUSTOM_BODY")
            if (catName != null) {
                try {
                    val category = NotificationCategory.valueOf(catName)
                    if (customTitle != null && customBody != null) {
                        VesperNotificationApi.sendNotification(customTitle, customBody, category)
                    } else {
                        VesperNotificationApi.sendNotification(category)
                    }
                } catch (e: Exception) {
                    Log.e("IntelligentNotifyWrk", "Failed triggering scheduled alert", e)
                }
            }
            return Result.success()
        }

        // 1. Direct snooze triggers
        if (triggerType == "SNOOZE_RECURRING") {
            val txId = inputData.getLong("TX_ID", -1L)
            if (txId != -1L) {
                val tx = app.database.transactionDao().getTransactionById(txId)
                if (tx != null) {
                    NotificationHelper.showRecurringTransactionAlert(
                        applicationContext,
                        tx.id,
                        tx.title.take(20),
                        tx.amount,
                        currencySymbol
                    )
                }
            }
            return Result.success()
        }

        // 2. Perform Intelligent Analytical Checks
        val transactions = app.database.transactionDao().getAllTransactions().first()
        val categories = app.database.transactionDao().getAllCategories().first()
        val categoryMap = categories.associateBy { it.id }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }

        // Budget Alerts Checking
        if (budgetReminderEnabled && expenses.isNotEmpty()) {
            runBudgetAnalysis(app, expenses, categoryMap, currencySymbol)
        }

        // Savings Goal Alerts Checking
        val savingsGoals = app.database.savingsDao().getAllSavingsGoals().first()
        if (savingsGoals.isNotEmpty()) {
            runSavingsGoalAnalysis(app, savingsGoals, currencySymbol)
        }

        // Recurring Reminders Checking
        if (recurringReminderEnabled && transactions.isNotEmpty()) {
            runRecurringAnalysis(app, transactions, currencySymbol)
        }

        // Missed Entries Checking
        if (missedEntryReminderEnabled) {
            runMissedEntryAnalysis(app, transactions)
        }

        // Weekly Trends Insights Checking
        if (weeklySummaryEnabled && expenses.isNotEmpty()) {
            runWeeklyTrendAnalysis(app, expenses)
        }

        // 3. Background Update Checking
        try {
            val updateRepo = com.vesper.ledger.data.update.UpdateRepository(app)
            val updateInfo = updateRepo.checkForUpdate()
            if (updateInfo != null && updateInfo.updateAvailable) {
                VesperNotificationApi.sendNotification(
                    title = "Update Available",
                    body = "Vesper Ledger v${updateInfo.latestVersionName} is ready to install. Review changelogs now.",
                    category = NotificationCategory.PRODUCT_UPDATES,
                    bypassCooldown = false
                )
            }
        } catch (e: Exception) {
            Log.e("IntelligentNotifyWrk", "Background update check failed", e)
        }

        return Result.success()
    }

    private fun runBudgetAnalysis(
        context: Context,
        expenses: List<Transaction>,
        categoryMap: Map<Long, com.vesper.ledger.data.model.Category>,
        currencySymbol: String
    ) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthlyExpenses = expenses.filter {
            val txCal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
            txCal.get(Calendar.MONTH) == currentMonth && txCal.get(Calendar.YEAR) == currentYear
        }

        // Default Category Budgets
        val budgetLimits = mapOf(
            "Food & Dining" to 200.0,
            "Food & Groceries" to 250.0,
            "Groceries" to 150.0,
            "Shopping" to 300.0,
            "Transport" to 100.0,
            "Entertainment" to 150.0,
            "Rent & Housing" to 1000.0,
            "Utilities" to 200.0,
            "Healthcare" to 100.0
        )

        // Sum expenses by category name
        val categorySpends = monthlyExpenses.groupBy { categoryMap[it.categoryId]?.name ?: "Other" }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }

        categorySpends.forEach { (catName, spent) ->
            val limit = budgetLimits[catName] ?: 200.0 // Default limit if not specified
            val ratio = spent / limit
            if (ratio >= 0.8) {
                // Check if we should warn (avoid spamming, only warn once a day per category)
                val lastWarnKey = "last_budget_warn_$catName"
                val sharedPrefs = context.getSharedPreferences("vesper_notify_st", Context.MODE_PRIVATE)
                val lastWarnTime = sharedPrefs.getLong(lastWarnKey, 0L)
                val elapsed = System.currentTimeMillis() - lastWarnTime

                if (elapsed > 24 * 60 * 60 * 1000L) { // 24 hours cooldown
                    NotificationHelper.showBudgetAlert(context, catName, spent, limit, currencySymbol)
                    sharedPrefs.edit().putLong(lastWarnKey, System.currentTimeMillis()).apply()
                }
            }
        }
    }

    private fun runSavingsGoalAnalysis(context: Context, goals: List<com.vesper.ledger.data.model.SavingsGoal>, currencySymbol: String) {
        val sharedPrefs = context.getSharedPreferences("vesper_notify_st", Context.MODE_PRIVATE)

        goals.forEach { goal ->
            if (goal.targetAmount <= 0.0) return@forEach
            val percent = ((goal.currentAmount / goal.targetAmount) * 100).toInt()
            
            // Determine milestone
            val milestone = when {
                percent >= 100 -> 100
                percent >= 80 -> 80
                percent >= 50 -> 50
                else -> 0
            }

            if (milestone > 0) {
                val lastMilestoneKey = "last_savings_milestone_${goal.id}"
                val lastMilestone = sharedPrefs.getInt(lastMilestoneKey, 0)
                if (milestone > lastMilestone) {
                    NotificationHelper.showSavingsGoalAlert(context, goal.id, goal.name, goal.currentAmount, goal.targetAmount, currencySymbol)
                    sharedPrefs.edit().putInt(lastMilestoneKey, milestone).apply()
                }
            }
        }
    }

    private fun runRecurringAnalysis(context: Context, transactions: List<Transaction>, currencySymbol: String) {
        val calendar = Calendar.getInstance()

        val recurringTxs = transactions.filter { it.recurringPattern != "One Time" }

        recurringTxs.forEach { tx ->
            val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateEpochMillis }
            val txDay = txCal.get(Calendar.DAY_OF_MONTH)

            // Simplistic due-date check for Monthly subscriptions (expected on same day of month)
            val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val diff = txDay - currentDayOfMonth
            
            // If due tomorrow (diff == 1 or diff == -30 for rollover), trigger notification
            if (diff == 1 || (diff == -30 && txDay == 1)) {
                val lastRemindKey = "last_recurring_remind_${tx.id}"
                val sharedPrefs = context.getSharedPreferences("vesper_notify_st", Context.MODE_PRIVATE)
                val lastRemindTime = sharedPrefs.getLong(lastRemindKey, 0L)
                val elapsed = System.currentTimeMillis() - lastRemindTime

                if (elapsed > 24 * 60 * 60 * 1000L) { // 24 hours cooldown
                    NotificationHelper.showRecurringTransactionAlert(context, tx.id, tx.title.take(20), tx.amount, currencySymbol)
                    sharedPrefs.edit().putLong(lastRemindKey, System.currentTimeMillis()).apply()
                }
            }
        }
    }

    private fun runMissedEntryAnalysis(context: Context, transactions: List<Transaction>) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // 1. Analyze active hours (preferred transaction hour)
        val activeHour = if (transactions.isNotEmpty()) {
            // Group by hour and find the peak hour
            transactions.map {
                val cal = Calendar.getInstance().apply { timeInMillis = it.dateEpochMillis }
                cal.get(Calendar.HOUR_OF_DAY)
            }.groupBy { it }.maxByOrNull { it.value.size }?.key ?: 20
        } else {
            20 // Default 8 PM
        }

        // Avoid triggering reminders during deep sleep hours (e.g. 11 PM to 7 AM)
        if (currentHour < 8 || currentHour > 22) return

        // 2. Check if we've logged a transaction today
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val loggedToday = transactions.any { it.dateEpochMillis >= today }

        if (!loggedToday && currentHour >= activeHour) {
            val lastRemindKey = "last_missed_entry_remind"
            val sharedPrefs = context.getSharedPreferences("vesper_notify_st", Context.MODE_PRIVATE)
            val lastRemindTime = sharedPrefs.getLong(lastRemindKey, 0L)
            val elapsed = System.currentTimeMillis() - lastRemindTime

            if (elapsed > 24 * 60 * 60 * 1000L) { // Limit to once per day
                // Variation Messages
                val variations = listOf(
                    "You usually record expenses around this time." to "Looks like today's spending hasn't been logged yet.",
                    "Was today a no-spend day?" to "If not, take a quick moment to log your expenses and stay on track.",
                    "Your financial timeline has a quiet spot today." to "Log your transactions to keep your dashboards up to date.",
                    "Keep your streak alive!" to "Just a quick tap to record any transactions you made today."
                )
                val selected = variations[Random.nextInt(variations.size)]
                NotificationHelper.showMissedEntryAlert(context, selected.first, selected.second)
                sharedPrefs.edit().putLong(lastRemindKey, System.currentTimeMillis()).apply()
            }
        }
    }

    private fun runWeeklyTrendAnalysis(context: Context, expenses: List<Transaction>) {
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L

        // Expenses this week vs last week
        val thisWeekSpends = expenses.filter { it.dateEpochMillis >= now - oneWeekMs }
        val lastWeekSpends = expenses.filter { it.dateEpochMillis in (now - 2 * oneWeekMs)..(now - oneWeekMs) }

        val thisWeekSum = thisWeekSpends.sumOf { it.amount }
        val lastWeekSum = lastWeekSpends.sumOf { it.amount }

        if (lastWeekSum > 0.0) {
            val change = (thisWeekSum - lastWeekSum) / lastWeekSum
            if (change <= -0.15 || change >= 0.15) {
                val lastTrendKey = "last_weekly_trend_warn"
                val sharedPrefs = context.getSharedPreferences("vesper_notify_st", Context.MODE_PRIVATE)
                val lastTrendTime = sharedPrefs.getLong(lastTrendKey, 0L)
                val elapsed = System.currentTimeMillis() - lastTrendTime

                if (elapsed > 7 * 24 * 60 * 60 * 1000L) { // Weekly limit
                    val percent = (kotlin.math.abs(change) * 100).toInt()
                    val title = "Weekly Spend Insights"
                    val body = if (change < 0) {
                        "Brilliant! Your overall spending dropped by $percent% this week compared to last week."
                    } else {
                        "Heads up: Your overall spending increased by $percent% this week. View analytics to inspect category changes."
                    }
                    NotificationHelper.showEngagementAlert(context, title, body)
                    sharedPrefs.edit().putLong(lastTrendKey, System.currentTimeMillis()).apply()
                }
            }
        }
    }
}
