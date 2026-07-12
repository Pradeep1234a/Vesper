package com.vesper.ledger

import android.app.Application
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.repository.SavingsRepository
import com.vesper.ledger.data.repository.TransactionRepository
import com.vesper.ledger.data.update.UpdateRepository

class VesperApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val savingsRepository by lazy { SavingsRepository(database.savingsDao()) }
    val updateRepository by lazy { UpdateRepository(this) }

    override fun onCreate() {
        super.onCreate()
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
