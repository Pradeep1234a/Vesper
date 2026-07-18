package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_history")
data class NotificationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val message: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val openedCount: Int = 0,
    val clickedCount: Int = 0,
    val dismissedCount: Int = 0
)
