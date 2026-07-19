package com.vesper.ledger

import android.app.Application
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.repository.SavingsRepository
import com.vesper.ledger.data.repository.TransactionRepository
import com.vesper.ledger.data.update.UpdateRepository

class VesperApplication : Application() {
    private var cachedDatabase: AppDatabase? = null
    private var cachedTxRepo: TransactionRepository? = null
    private var cachedSavingsRepo: SavingsRepository? = null
    private var cachedAccountRepo: com.vesper.ledger.data.repository.AccountRepository? = null
    private var cachedBudgetRepo: com.vesper.ledger.data.repository.BudgetRepository? = null
    private var cachedRecurringRepo: com.vesper.ledger.data.repository.RecurringTransactionRepository? = null

    val database: AppDatabase
        get() {
            val current = AppDatabase.getDatabase(this)
            if (cachedDatabase != current) {
                cachedDatabase = current
                cachedTxRepo = TransactionRepository(current.transactionDao())
                cachedSavingsRepo = SavingsRepository(current.savingsDao())
                cachedAccountRepo = com.vesper.ledger.data.repository.AccountRepository(current.accountDao(), current.paymentMethodDao())
                cachedBudgetRepo = com.vesper.ledger.data.repository.BudgetRepository(current.budgetDao())
                cachedRecurringRepo = com.vesper.ledger.data.repository.RecurringTransactionRepository(current.recurringTransactionDao())
            }
            return current
        }

    val transactionRepository: TransactionRepository
        get() {
            database
            return cachedTxRepo!!
        }

    val savingsRepository: SavingsRepository
        get() {
            database
            return cachedSavingsRepo!!
        }

    val accountRepository: com.vesper.ledger.data.repository.AccountRepository
        get() {
            database
            return cachedAccountRepo!!
        }

    val budgetRepository: com.vesper.ledger.data.repository.BudgetRepository
        get() {
            database
            return cachedBudgetRepo!!
        }

    val recurringRepository: com.vesper.ledger.data.repository.RecurringTransactionRepository
        get() {
            database
            return cachedRecurringRepo!!
        }

    val updateRepository by lazy { UpdateRepository(this) }

    fun clearDatabaseCaches() {
        cachedDatabase = null
        cachedTxRepo = null
        cachedSavingsRepo = null
        cachedAccountRepo = null
        cachedBudgetRepo = null
        cachedRecurringRepo = null
    }

    companion object {
        private lateinit var instance: VesperApplication
        fun getContext(): VesperApplication = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("VesperApplication", "CRITICAL: Uncaught exception on thread: ${thread.name}", throwable)
            if (thread.name.contains("main", ignoreCase = true) || thread.id == android.os.Looper.getMainLooper().thread.id) {
                android.os.Process.killProcess(android.os.Process.myPid())
                java.lang.System.exit(10)
            } else {
                android.util.Log.w("VesperApplication", "Swallowed background exception to prevent process termination.")
            }
        }
    }
}
