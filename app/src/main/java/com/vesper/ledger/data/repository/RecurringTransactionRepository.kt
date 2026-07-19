package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.RecurringTransactionDao
import com.vesper.ledger.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

class RecurringTransactionRepository(private val recurringDao: RecurringTransactionDao) {
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = recurringDao.getAllRecurringTransactions()

    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction? {
        return recurringDao.getRecurringTransactionById(id)
    }

    suspend fun insertRecurringTransaction(recurring: RecurringTransaction): Long {
        return recurringDao.insertRecurringTransaction(recurring)
    }

    suspend fun updateRecurringTransaction(recurring: RecurringTransaction) {
        recurringDao.updateRecurringTransaction(recurring)
    }

    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction) {
        recurringDao.deleteRecurringTransaction(recurring)
    }
}
