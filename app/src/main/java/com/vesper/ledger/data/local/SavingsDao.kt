package com.vesper.ledger.data.local

import androidx.room.*
import com.vesper.ledger.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_goals ORDER BY targetDateEpochMillis ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal)

    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
}
