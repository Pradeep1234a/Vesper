package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.BudgetDao
import com.vesper.ledger.data.model.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allBudgetsIncludeArchived: Flow<List<Budget>> = budgetDao.getAllBudgetsIncludeArchived()

    suspend fun getBudgetById(id: Long): Budget? {
        return budgetDao.getBudgetById(id)
    }

    suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }
}
