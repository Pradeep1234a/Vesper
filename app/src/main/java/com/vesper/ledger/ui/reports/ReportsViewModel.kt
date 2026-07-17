package com.vesper.ledger.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

// Predefined date range selector options
enum class AnalyticsPeriod(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range")
}

data class CategoryReport(
    val category: Category,
    val totalAmount: Double,
    val percentage: Float
)

data class TrendPoint(
    val label: String,
    val amount: Double
)

data class MerchantEntry(
    val name: String,
    val transactionCount: Int,
    val totalAmount: Double
)

data class HeatmapDay(
    val dayOfWeek: Int, // 0=Mon..6=Sun
    val weekIndex: Int,
    val intensity: Float // 0.0..1.0
)

data class ReportsUiState(
    val totalSpending: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val transactionCount: Int = 0,
    val averageDailySpend: Double = 0.0,
    val largestExpenseAmount: Double = 0.0,
    val largestExpenseName: String = "",
    val largestExpenseDate: String = "",
    val previousPeriodSpending: Double = 0.0,
    val spendingChangePercent: Float = 0f,
    val transactionChangePercent: Float = 0f,
    val avgChangePercent: Float = 0f,
    val categoryReports: List<CategoryReport> = emptyList(),
    val trendPoints: List<TrendPoint> = emptyList(),
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val topMerchants: List<MerchantEntry> = emptyList(),
    val previousTransactionCount: Int = 0
)

class ReportsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val selectedPeriod = MutableStateFlow(AnalyticsPeriod.THIS_MONTH)
    
    // Custom range state variables
    val customStartDate = MutableStateFlow<Long?>(null)
    val customEndDate = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<ReportsUiState> = combine(
        transactionRepository.allTransactions,
        transactionRepository.allCategories,
        selectedPeriod,
        customStartDate,
        customEndDate
    ) { transactions, categories, period, start, end ->
        computeState(transactions, categories, period, start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun setPeriod(period: AnalyticsPeriod) {
        selectedPeriod.value = period
    }
    
    fun setCustomRange(startMs: Long, endMs: Long) {
        customStartDate.value = startMs
        customEndDate.value = endMs
        selectedPeriod.value = AnalyticsPeriod.CUSTOM
    }

    private fun computeState(
        transactions: List<Transaction>,
        categories: List<Category>,
        period: AnalyticsPeriod,
        customStart: Long?,
        customEnd: Long?
    ): ReportsUiState {
        val now = System.currentTimeMillis()

        // Calculate period bounds
        val (startMs, endMs) = getPeriodBounds(period, now, customStart, customEnd)
        val periodDays = ((endMs - startMs) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)

        // Previous period for comparison
        val prevDuration = endMs - startMs
        val prevStartMs = startMs - prevDuration
        val prevEndMs = startMs

        // Filter transactions
        val periodTxns = transactions.filter { it.dateEpochMillis in startMs..endMs }
        val prevTxns = transactions.filter { it.dateEpochMillis in prevStartMs until prevEndMs }

        val expenses = periodTxns.filter { it.type == TransactionType.EXPENSE }
        val prevExpenses = prevTxns.filter { it.type == TransactionType.EXPENSE }
        val income = periodTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        val totalSpending = expenses.sumOf { it.amount }
        val prevSpending = prevExpenses.sumOf { it.amount }
        val txCount = expenses.size
        val prevTxCount = prevExpenses.size
        val avgDaily = if (periodDays > 0) totalSpending / periodDays else 0.0
        val prevAvgDaily = if (periodDays > 0) prevSpending / periodDays else 0.0

        val spendingChangePct = if (prevSpending > 0) ((totalSpending - prevSpending) / prevSpending * 100).toFloat() else 0f
        val txChangePct = if (prevTxCount > 0) ((txCount - prevTxCount).toFloat() / prevTxCount * 100) else 0f
        val avgChangePct = if (prevAvgDaily > 0) ((avgDaily - prevAvgDaily) / prevAvgDaily * 100).toFloat() else 0f

        // Largest expense
        val largest = expenses.maxByOrNull { it.amount }
        val largestCat = if (largest != null) categories.firstOrNull { it.id == largest.categoryId }?.name ?: "" else ""
        val largestDateStr = if (largest != null) {
            val c = Calendar.getInstance().apply { timeInMillis = largest.dateEpochMillis }
            val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${monthNames[c.get(Calendar.MONTH)]} ${c.get(Calendar.DAY_OF_MONTH)}"
        } else ""

        // Category breakdown
        val categoryReports = categories.filter { it.type == TransactionType.EXPENSE }.map { cat ->
            val total = expenses.filter { it.categoryId == cat.id }.sumOf { it.amount }
            val pct = if (totalSpending > 0) (total / totalSpending).toFloat() else 0f
            CategoryReport(cat, total, pct)
        }.filter { it.totalAmount > 0 }.sortedByDescending { it.totalAmount }

        // Trend points
        val trendPoints = computeTrendPoints(expenses, period, startMs, endMs)

        // Heatmap
        val heatmapDays = computeHeatmap(expenses, startMs, endMs)

        // Merchants (by transaction title)
        val merchantGroups = expenses.groupBy { it.title.trim() }
        val topMerchants = merchantGroups.map { (name, txns) ->
            MerchantEntry(name, txns.size, txns.sumOf { it.amount })
        }.sortedByDescending { it.transactionCount }.take(5)

        return ReportsUiState(
            totalSpending = totalSpending,
            totalIncome = income,
            totalExpense = totalSpending,
            transactionCount = txCount,
            averageDailySpend = avgDaily,
            largestExpenseAmount = largest?.amount ?: 0.0,
            largestExpenseName = largestCat,
            largestExpenseDate = largestDateStr,
            previousPeriodSpending = prevSpending,
            spendingChangePercent = spendingChangePct,
            transactionChangePercent = txChangePct,
            avgChangePercent = avgChangePct,
            categoryReports = categoryReports,
            trendPoints = trendPoints,
            heatmapDays = heatmapDays,
            topMerchants = topMerchants,
            previousTransactionCount = prevTxCount
        )
    }

    private fun getPeriodBounds(
        period: AnalyticsPeriod,
        now: Long,
        customStart: Long?,
        customEnd: Long?
    ): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply { timeInMillis = now }
        val endCal = Calendar.getInstance().apply { timeInMillis = now }

        when (period) {
            AnalyticsPeriod.TODAY -> {
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
                endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
            }
            AnalyticsPeriod.YESTERDAY -> {
                startCal.add(Calendar.DAY_OF_YEAR, -1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
                endCal.add(Calendar.DAY_OF_YEAR, -1)
                endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
            }
            AnalyticsPeriod.THIS_WEEK -> {
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
            }
            AnalyticsPeriod.LAST_WEEK -> {
                startCal.add(Calendar.WEEK_OF_YEAR, -1)
                startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)

                endCal.add(Calendar.WEEK_OF_YEAR, -1)
                endCal.set(Calendar.DAY_OF_WEEK, endCal.firstDayOfWeek + 6)
                endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
            }
            AnalyticsPeriod.THIS_MONTH -> {
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
            }
            AnalyticsPeriod.LAST_MONTH -> {
                startCal.add(Calendar.MONTH, -1)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)

                endCal.add(Calendar.MONTH, -1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                endCal.set(Calendar.HOUR_OF_DAY, 23); endCal.set(Calendar.MINUTE, 59); endCal.set(Calendar.SECOND, 59); endCal.set(Calendar.MILLISECOND, 999)
            }
            AnalyticsPeriod.LAST_3_MONTHS -> {
                startCal.add(Calendar.MONTH, -3)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
            }
            AnalyticsPeriod.LAST_6_MONTHS -> {
                startCal.add(Calendar.MONTH, -6)
                startCal.set(Calendar.DAY_OF_MONTH, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
            }
            AnalyticsPeriod.THIS_YEAR -> {
                startCal.set(Calendar.DAY_OF_YEAR, 1)
                startCal.set(Calendar.HOUR_OF_DAY, 0); startCal.set(Calendar.MINUTE, 0); startCal.set(Calendar.SECOND, 0); startCal.set(Calendar.MILLISECOND, 0)
            }
            AnalyticsPeriod.CUSTOM -> {
                startCal.timeInMillis = customStart ?: now
                endCal.timeInMillis = customEnd ?: now
            }
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }

    private fun computeTrendPoints(
        expenses: List<Transaction>,
        period: AnalyticsPeriod,
        startMs: Long,
        endMs: Long
    ): List<TrendPoint> {
        val cal = Calendar.getInstance()
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        return when (period) {
            AnalyticsPeriod.TODAY, AnalyticsPeriod.YESTERDAY -> {
                val groups = expenses.groupBy { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val ampm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
                    val displayHour = if (hour % 12 == 0) 12 else hour % 12
                    "$displayHour $ampm"
                }
                val result = mutableListOf<TrendPoint>()
                for (h in 0..23 step 4) {
                    val ampm = if (h < 12) "AM" else "PM"
                    val displayHour = if (h % 12 == 0) 12 else h % 12
                    val label = "$displayHour $ampm"
                    var sum = 0.0
                    for (subHour in h until (h + 4)) {
                        val subAmpm = if (subHour < 12) "AM" else "PM"
                        val subDisplayHour = if (subHour % 12 == 0) 12 else subHour % 12
                        sum += groups["$subDisplayHour $subAmpm"]?.sumOf { it.amount } ?: 0.0
                    }
                    result.add(TrendPoint(label, sum))
                }
                result
            }
            AnalyticsPeriod.THIS_WEEK, AnalyticsPeriod.LAST_WEEK, AnalyticsPeriod.THIS_MONTH, AnalyticsPeriod.LAST_MONTH -> {
                val groups = expenses.groupBy { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}"
                }
                val result = mutableListOf<TrendPoint>()
                val dayCal = Calendar.getInstance().apply { timeInMillis = startMs }
                while (dayCal.timeInMillis <= endMs) {
                    val label = "${monthNames[dayCal.get(Calendar.MONTH)]} ${dayCal.get(Calendar.DAY_OF_MONTH)}"
                    val amount = groups[label]?.sumOf { it.amount } ?: 0.0
                    result.add(TrendPoint(label, amount))
                    dayCal.add(Calendar.DAY_OF_MONTH, 1)
                }
                result
            }
            else -> {
                val groups = expenses.groupBy { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    "${monthNames[cal.get(Calendar.MONTH)]}"
                }
                val result = mutableListOf<TrendPoint>()
                val mCal = Calendar.getInstance().apply { timeInMillis = startMs }
                while (mCal.timeInMillis <= endMs) {
                    val label = monthNames[mCal.get(Calendar.MONTH)]
                    val amount = groups[label]?.sumOf { it.amount } ?: 0.0
                    result.add(TrendPoint(label, amount))
                    mCal.add(Calendar.MONTH, 1)
                }
                result
            }
        }
    }

    private fun computeHeatmap(
        expenses: List<Transaction>,
        startMs: Long,
        endMs: Long
    ): List<HeatmapDay> {
        val cal = Calendar.getInstance()
        val dayCounts = mutableMapOf<String, Int>()
        for (tx in expenses) {
            cal.timeInMillis = tx.dateEpochMillis
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            dayCounts[key] = (dayCounts[key] ?: 0) + 1
        }
        val maxCount = dayCounts.values.maxOrNull()?.toFloat() ?: 1f

        val result = mutableListOf<HeatmapDay>()
        val dayCal = Calendar.getInstance().apply { timeInMillis = startMs }
        var weekIdx = 0
        var lastWeekOfYear = -1
        while (dayCal.timeInMillis <= endMs) {
            val weekOfYear = dayCal.get(Calendar.WEEK_OF_YEAR)
            if (lastWeekOfYear != -1 && weekOfYear != lastWeekOfYear) weekIdx++
            lastWeekOfYear = weekOfYear

            val dayOfWeek = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7
            val key = "${dayCal.get(Calendar.YEAR)}-${dayCal.get(Calendar.DAY_OF_YEAR)}"
            val count = dayCounts[key] ?: 0
            val intensity = if (maxCount > 0) count / maxCount else 0f

            result.add(HeatmapDay(dayOfWeek, weekIdx, intensity))
            dayCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }
}

class ReportsViewModelFactory(
    private val transactionRepository: TransactionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(transactionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
