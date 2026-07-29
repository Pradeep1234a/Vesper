package com.vesper.ledger.data.local

import androidx.room.*
import com.vesper.ledger.data.model.SplitExpense
import com.vesper.ledger.data.model.SplitExpenseShare
import com.vesper.ledger.data.model.SplitGroup
import com.vesper.ledger.data.model.SplitMember
import com.vesper.ledger.data.model.SplitSettlement
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitDao {

    // ── Split Groups ──
    @Query("SELECT * FROM split_groups ORDER BY createdAtEpochMillis DESC")
    fun getAllGroups(): Flow<List<SplitGroup>>

    @Query("SELECT * FROM split_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): SplitGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: SplitGroup): Long

    @Delete
    suspend fun deleteGroup(group: SplitGroup)

    @Query("DELETE FROM split_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    // ── Split Members ──
    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    fun getMembersForGroup(groupId: Long): Flow<List<SplitMember>>

    @Query("SELECT * FROM split_members WHERE groupId = :groupId")
    suspend fun getMembersForGroupSync(groupId: Long): List<SplitMember>

    @Query("SELECT * FROM split_members")
    fun getAllMembers(): Flow<List<SplitMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<SplitMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: SplitMember): Long

    // ── Split Expenses ──
    @Query("SELECT * FROM split_expenses ORDER BY dateEpochMillis DESC")
    fun getAllExpenses(): Flow<List<SplitExpense>>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY dateEpochMillis DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<SplitExpense>>

    @Query("SELECT * FROM split_expenses WHERE groupId = :groupId ORDER BY dateEpochMillis DESC")
    suspend fun getExpensesForGroupSync(groupId: Long): List<SplitExpense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: SplitExpense): Long

    @Delete
    suspend fun deleteExpense(expense: SplitExpense)

    // ── Expense Shares ──
    @Query("SELECT * FROM split_expense_shares WHERE expenseId = :expenseId")
    fun getSharesForExpense(expenseId: Long): Flow<List<SplitExpenseShare>>

    @Query("SELECT * FROM split_expense_shares WHERE expenseId = :expenseId")
    suspend fun getSharesForExpenseSync(expenseId: Long): List<SplitExpenseShare>

    @Query("SELECT * FROM split_expense_shares")
    fun getAllShares(): Flow<List<SplitExpenseShare>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShares(shares: List<SplitExpenseShare>)

    @Query("UPDATE split_expense_shares SET isPaid = :isPaid WHERE id = :shareId")
    suspend fun updateSharePaymentStatus(shareId: Long, isPaid: Boolean)

    // ── Settlements ──
    @Query("SELECT * FROM split_settlements WHERE groupId = :groupId ORDER BY dateEpochMillis DESC")
    fun getSettlementsForGroup(groupId: Long): Flow<List<SplitSettlement>>

    @Query("SELECT * FROM split_settlements ORDER BY dateEpochMillis DESC")
    fun getAllSettlements(): Flow<List<SplitSettlement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SplitSettlement): Long
}
