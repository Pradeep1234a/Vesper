package com.vesper.ledger

import android.app.Application
import com.vesper.ledger.data.local.AppDatabase
import com.vesper.ledger.data.repository.SavingsRepository
import com.vesper.ledger.data.repository.TransactionRepository

class VesperApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val transactionRepository by lazy { TransactionRepository(database.transactionDao()) }
    val savingsRepository by lazy { SavingsRepository(database.savingsDao()) }
}
