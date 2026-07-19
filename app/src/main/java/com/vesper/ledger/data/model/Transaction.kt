package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val dateEpochMillis: Long,
    val note: String = "",
    val accountName: String = "Cash Wallet",
    val paymentMethod: String = "Cash",
    val recurringPattern: String = "One Time",
    val location: String = "",
    val accountId: Long = 0,
    val targetAccountId: Long? = null
)
