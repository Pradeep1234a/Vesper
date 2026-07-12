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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, Category::class, SavingsGoal::class, UserAccount::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsDao(): SavingsDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vesper_ledger_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).transactionDao()
                            dao.insertCategories(defaultCategories)
                        }
                    }
                })
                .build()
                INSTANCE = instance
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
