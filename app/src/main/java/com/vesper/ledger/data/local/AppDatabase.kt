package com.vesper.ledger.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vesper.ledger.data.model.Category
import com.vesper.ledger.data.model.SavingsGoal
import com.vesper.ledger.data.model.Transaction
import com.vesper.ledger.data.model.TransactionType
import com.vesper.ledger.data.model.UserAccount
import com.vesper.ledger.data.model.NotificationHistory
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.PaymentMethod
import com.vesper.ledger.data.model.RecurringTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        Category::class,
        SavingsGoal::class,
        UserAccount::class,
        NotificationHistory::class,
        Account::class,
        Budget::class,
        PaymentMethod::class,
        RecurringTransaction::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsDao(): SavingsDao
    abstract fun userDao(): UserDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private var CURRENT_DB_NAME: String? = null

        fun getDatabase(context: Context): AppDatabase {
            val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)
            val email = sharedPrefs.getString("user_email", "") ?: ""
            val dbName = if (email.isBlank()) {
                "vesper_ledger_db_guest"
            } else {
                "vesper_ledger_db_${email.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
            }

            return INSTANCE?.takeIf { CURRENT_DB_NAME == dbName } ?: synchronized(this) {
                INSTANCE?.let {
                    if (CURRENT_DB_NAME != dbName) {
                        try {
                            it.close()
                        } catch (e: Exception) {
                            android.util.Log.e("AppDatabase", "Error closing database connection: $CURRENT_DB_NAME", e)
                        }
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dbInstance = getDatabase(context)
                            val dao = dbInstance.transactionDao()
                            dao.insertCategories(defaultCategories)

                            // Seed default account
                            dbInstance.accountDao().insertAccount(
                                Account(
                                    name = "Cash Wallet",
                                    type = "CASH",
                                    initialBalance = 0.0,
                                    currency = "USD",
                                    iconName = "account_balance_wallet"
                                )
                            )

                            // Seed default payment methods
                            dbInstance.paymentMethodDao().insertPaymentMethods(
                                listOf(
                                    PaymentMethod(name = "Cash", isDefault = true),
                                    PaymentMethod(name = "Debit Card"),
                                    PaymentMethod(name = "Credit Card"),
                                    PaymentMethod(name = "UPI"),
                                    PaymentMethod(name = "Bank Transfer"),
                                    PaymentMethod(name = "Wallet")
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                CURRENT_DB_NAME = dbName
                instance
            }
        }

        private val defaultCategories = listOf(
            Category(id = 1, name = "Salary", iconName = "work", type = TransactionType.INCOME, colorHex = "#16A34A"),
            Category(id = 2, name = "Investments", iconName = "trending_up", type = TransactionType.INCOME, colorHex = "#0D9488"),
            Category(id = 3, name = "Gifts", iconName = "card_giftcard", type = TransactionType.INCOME, colorHex = "#2563EB"),
            Category(id = 4, name = "Other Income", iconName = "more_horiz", type = TransactionType.INCOME, colorHex = "#71717A"),
            
            Category(id = 5, name = "Food & Groceries", iconName = "restaurant", type = TransactionType.EXPENSE, colorHex = "#DC2626"),
            Category(id = 6, name = "Rent & Housing", iconName = "home", type = TransactionType.EXPENSE, colorHex = "#D97706"),
            Category(id = 7, name = "Utilities", iconName = "bolt", type = TransactionType.EXPENSE, colorHex = "#EA580C"),
            Category(id = 8, name = "Transport & Fuel", iconName = "directions_car", type = TransactionType.EXPENSE, colorHex = "#2563EB"),
            Category(id = 9, name = "Entertainment", iconName = "sports_esports", type = TransactionType.EXPENSE, colorHex = "#9333EA"),
            Category(id = 10, name = "Shopping", iconName = "shopping_bag", type = TransactionType.EXPENSE, colorHex = "#DB2777"),
            Category(id = 11, name = "Healthcare", iconName = "medical_services", type = TransactionType.EXPENSE, colorHex = "#0D9488"),
            Category(id = 12, name = "Other Expense", iconName = "more_horiz", type = TransactionType.EXPENSE, colorHex = "#71717A")
        )
    }
}
