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

// Period filter options
enum class AnalyticsPeriod(val label: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    LAST_3_MONTHS("Last 3 Months"),
    LAST_6_MONTHS("Last 6 Months"),
    THIS_YEAR("This Year")
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

    val uiState: StateFlow<ReportsUiState> = combine(
        transactionRepository.allTransactions,
        transactionRepository.allCategories,
        selectedPeriod
    ) { transactions, categories, period ->
        computeState(transactions, categories, period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState()
    )

    fun setPeriod(period: AnalyticsPeriod) {
        selectedPeriod.value = period
    }

    private fun computeState(
        transactions: List<Transaction>,
        categories: List<Category>,
        period: AnalyticsPeriod
    ): ReportsUiState {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Calculate period bounds
        val (startMs, endMs) = getPeriodBounds(period, cal, now)
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

        // Trend points (group by day/week/month depending on period)
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

    private fun getPeriodBounds(period: AnalyticsPeriod, cal: Calendar, now: Long): Pair<Long, Long> {
        cal.timeInMillis = now
        val endMs = now
        val startMs = when (period) {
            AnalyticsPeriod.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.LAST_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.LAST_3_MONTHS -> {
                cal.add(Calendar.MONTH, -3)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.LAST_6_MONTHS -> {
                cal.add(Calendar.MONTH, -6)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            AnalyticsPeriod.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
        }
        return Pair(startMs, endMs)
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
            AnalyticsPeriod.THIS_WEEK, AnalyticsPeriod.THIS_MONTH, AnalyticsPeriod.LAST_MONTH -> {
                // Group by day
                val groups = expenses.groupBy { tx ->
                    cal.timeInMillis = tx.dateEpochMillis
                    "${monthNames[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}"
                }
                // Build consecutive days
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
                // Group by month
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
        // Build a map of day -> count
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

            val dayOfWeek = (dayCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0..Sun=6
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
