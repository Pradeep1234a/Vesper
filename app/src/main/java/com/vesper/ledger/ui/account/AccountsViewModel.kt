package com.vesper.ledger.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vesper.ledger.VesperApplication
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountWithBalance(
    val account: Account,
    val balance: Double
)

class AccountsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val accountRepository = (application as VesperApplication).accountRepository
    private val transactionRepository = (application as VesperApplication).transactionRepository

    val accounts: StateFlow<List<Account>> = accountRepository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = transactionRepository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountsWithBalances: StateFlow<List<AccountWithBalance>> = combine(accounts, transactions) { accList, txList ->
        accList.map { account ->
            val totalIncome = txList.filter { it.accountId == account.id && it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = txList.filter { it.accountId == account.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val transfersOut = txList.filter { it.accountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            val transfersIn = txList.filter { it.targetAccountId == account.id && it.type == TransactionType.TRANSFER }.sumOf { it.amount }

            val currentBalance = account.initialBalance + totalIncome - totalExpense - transfersOut + transfersIn
            AccountWithBalance(account, currentBalance)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalNetWorth: StateFlow<Double> = accountsWithBalances.map { list ->
        list.sumOf { it.balance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cashBalance: StateFlow<Double> = accountsWithBalances.map { list ->
        list.filter { it.account.type == "CASH" }.sumOf { it.balance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addAccount(name: String, type: String, initialBalance: Double, bankInfo: String?, notes: String?) {
        viewModelScope.launch {
            val account = Account(
                name = name,
                type = type,
                initialBalance = initialBalance,
                bankInfo = bankInfo,
                notes = notes,
                iconName = when (type) {
                    "CASH" -> "payments"
                    "BANK" -> "account_balance"
                    "SAVINGS" -> "savings"
                    "CREDIT_CARD" -> "credit_card"
                    else -> "account_balance_wallet"
                }
            )
            accountRepository.insertAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
        }
    }
}

class AccountsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountsViewModel::class.java)) {
            return AccountsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
