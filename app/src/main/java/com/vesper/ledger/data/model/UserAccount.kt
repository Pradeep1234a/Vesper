package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey val email: String,
    val fullName: String,
    val passwordHash: String,
    val salt: String
)
