package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.TransactionDao
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = transactionDao.getAllCategories()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getTransactionById(id: Long): Transaction? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun insertCategory(category: Category) {
        transactionDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        transactionDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: Category) {
        transactionDao.deleteCategory(category)
    }

    suspend fun getCategoryById(id: Long): Category? {
        return transactionDao.getCategoryById(id)
    }
}
