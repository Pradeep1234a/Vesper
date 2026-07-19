package com.vesper.ledger.data.local

import androidx.room.*
import com.vesper.ledger.data.model.RecurringTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY id DESC")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringTransactionById(id: Long): RecurringTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurring: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(recurring: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurring: RecurringTransaction)
}
