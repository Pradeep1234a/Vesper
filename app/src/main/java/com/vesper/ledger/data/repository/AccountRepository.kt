package com.vesper.ledger.data.repository

import com.vesper.ledger.data.local.AccountDao
import com.vesper.ledger.data.local.PaymentMethodDao
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

class AccountRepository(
    private val accountDao: AccountDao,
    private val paymentMethodDao: PaymentMethodDao
) {
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()
    val allPaymentMethods: Flow<List<PaymentMethod>> = paymentMethodDao.getAllPaymentMethods()

    suspend fun getAccountById(id: Long): Account? {
        return accountDao.getAccountById(id)
    }

    suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccount(account)
    }

    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod): Long {
        return paymentMethodDao.insertPaymentMethod(paymentMethod)
    }

    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.deletePaymentMethod(paymentMethod)
    }
}
