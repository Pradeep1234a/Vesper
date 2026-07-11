package com.vesper.ledger.data.local

import androidx.room.TypeConverter
import com.vesper.ledger.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
