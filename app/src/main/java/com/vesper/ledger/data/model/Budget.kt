package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val period: String, // WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    val categoryId: Long, // links to Category id
    val startDate: Long,
    val endDate: Long,
    val notes: String? = null,
    val reminderEnabled: Boolean = true,
    val rolloverEnabled: Boolean = false,
    val isArchived: Boolean = false
)
