package com.vesper.ledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "split_groups")
data class SplitGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "General", // Trip, Home, Event, Food, General
    val iconName: String = "group",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "split_members")
data class SplitMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val name: String,
    val isCurrentUser: Boolean = false
)

@Entity(tableName = "split_expenses")
data class SplitExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val totalAmount: Double,
    val paidByMemberId: Long,
    val paidByMemberName: String,
    val paymentMethod: String = "UPI", // UPI, Cash, Credit Card, Debit Card, Bank Transfer
    val accountId: Long = 0, // Linked account if paid by current user
    val categoryName: String = "Food & Groceries",
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val splitType: String = "EQUAL", // EQUAL, EXACT, PERCENTAGE
    val isSettlement: Boolean = false
)

@Entity(tableName = "split_expense_shares")
data class SplitExpenseShare(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expenseId: Long,
    val memberId: Long,
    val memberName: String,
    val shareAmount: Double,
    val isPaid: Boolean = false
)

/**
 * Calculated Debt Transaction representing direct settlement between two members
 */
data class DebtSettlement(
    val debtorId: Long,
    val debtorName: String,
    val creditorId: Long,
    val creditorName: String,
    val amount: Double,
    val isDebtorCurrentUser: Boolean,
    val isCreditorCurrentUser: Boolean
)
