package com.vesper.ledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vesper.ledger.data.model.UserAccount

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccount)

    @Query("UPDATE users SET passwordHash = :newHash, salt = :newSalt WHERE email = :email")
    suspend fun updatePassword(email: String, newHash: String, newSalt: String)

    @Query("UPDATE users SET fullName = :fullName WHERE email = :email")
    suspend fun updateFullName(email: String, fullName: String)
}
