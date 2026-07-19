package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val accountId: Long, // source account
    val targetAccountId: Long? = null, // dest account for transfers
    val categoryId: Long,
    val paymentMethod: String,
    val frequency: String, // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
    val startDate: Long,
    val endDate: Long? = null,
    val notes: String? = null,
    val reminderEnabled: Boolean = true,
    val autoCreate: Boolean = true,
    val isPaused: Boolean = false,
    val lastTriggeredEpochMillis: Long = 0L,
    val lastExecutedDate: Long? = null
)
