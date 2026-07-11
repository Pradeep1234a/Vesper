package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.SavingsDao
import com.vesper.ledger.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

class SavingsRepository(private val savingsDao: SavingsDao) {
    val allSavingsGoals: Flow<List<SavingsGoal>> = savingsDao.getAllSavingsGoals()

    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal) {
        savingsDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        savingsDao.updateSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        savingsDao.deleteSavingsGoal(savingsGoal)
    }

    suspend fun getSavingsGoalById(id: Long): SavingsGoal? {
        return savingsDao.getSavingsGoalById(id)
    }
}
