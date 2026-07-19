package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // CASH, BANK, SAVINGS, CREDIT_CARD, DEBIT_CARD, DIGITAL_WALLET, INVESTMENT, LOAN, CUSTOM
    val initialBalance: Double,
    val currency: String = "USD",
    val bankInfo: String? = null,
    val notes: String? = null,
    val iconName: String = "account_balance_wallet"
)
