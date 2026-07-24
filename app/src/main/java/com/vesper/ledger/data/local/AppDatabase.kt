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
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.Budget
import com.vesper.ledger.data.model.PaymentMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Transaction::class,
        Category::class,
        SavingsGoal::class,
        UserAccount::class,
        Account::class,
        Budget::class,
        PaymentMethod::class
    ],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsDao(): SavingsDao
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun paymentMethodDao(): PaymentMethodDao

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
                        seedDatabase(db)
                    }
                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        seedDatabase(db)
                    }
                })
                .build()
                INSTANCE = instance
                CURRENT_DB_NAME = dbName
                instance
            }
        }

        private fun seedDatabase(db: SupportSQLiteDatabase) {
            try {
                // Categories
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (1, 'Groceries', 'shopping_cart', 'EXPENSE', '#DC2626')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (2, 'Food & Dining', 'restaurant', 'EXPENSE', '#EA580C')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (3, 'Electronics', 'laptop', 'EXPENSE', '#2563EB')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (4, 'Books & Stationery', 'book', 'EXPENSE', '#059669')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (5, 'Clothing & Apparel', 'checkroom', 'EXPENSE', '#DB2777')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (6, 'Beauty & Care', 'spa', 'EXPENSE', '#D97706')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (7, 'Health & Medical', 'medical', 'EXPENSE', '#DC2626')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (8, 'Home Supplies', 'home', 'EXPENSE', '#0D9488')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (9, 'Entertainment', 'movie', 'EXPENSE', '#9333EA')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (10, 'Transportation', 'car', 'EXPENSE', '#2563EB')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (11, 'Pets', 'pets', 'EXPENSE', '#D97706')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (12, 'Utilities & Bills', 'receipt_long', 'EXPENSE', '#EA580C')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (13, 'Subscriptions', 'card', 'EXPENSE', '#9333EA')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (14, 'General Expense', 'category', 'EXPENSE', '#71717A')")

                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (101, 'Salary', 'work', 'INCOME', '#16A34A')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (102, 'Freelance & Business', 'laptop', 'INCOME', '#0D9488')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (103, 'Investments', 'trending_up', 'INCOME', '#2563EB')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (104, 'Bonus & Incentives', 'gift', 'INCOME', '#9333EA')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (105, 'Interest Income', 'bank', 'INCOME', '#059669')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (106, 'Gifts & Grants', 'gift', 'INCOME', '#D97706')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (107, 'Rental Income', 'home', 'INCOME', '#2563EB')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (108, 'Side Hustle', 'bolt', 'INCOME', '#EA580C')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (109, 'Cashback & Refunds', 'payments', 'INCOME', '#16A34A')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName, type, colorHex) VALUES (110, 'Other Income', 'money', 'INCOME', '#71717A')")

                // Default payment methods
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (1, 'Cash', 1)")
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (2, 'Debit Card', 0)")
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (3, 'Credit Card', 0)")
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (4, 'UPI', 0)")
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (5, 'Bank Transfer', 0)")
                db.execSQL("INSERT OR IGNORE INTO payment_methods (id, name, isDefault) VALUES (6, 'Wallet', 0)")
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Error seeding database: ${e.message}", e)
            }
        }
    }
}
