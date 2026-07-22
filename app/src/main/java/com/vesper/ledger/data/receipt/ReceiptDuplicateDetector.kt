package com.vesper.ledger.data.receipt

import android.content.Context
import android.content.SharedPreferences

object ReceiptDuplicateDetector {

    /**
     * Checks if a receipt with identical merchant, date, and amount hash already exists.
     */
    fun checkForDuplicate(
        context: Context,
        receipt: ScannedReceipt
    ): ReceiptDuplicate {
        val prefs = context.getSharedPreferences("vesper_receipt_hashes", Context.MODE_PRIVATE)
        val fp = ReceiptImageProcessor.generateReceiptFingerprint(
            merchant = receipt.merchantName,
            date = receipt.dateString,
            amount = receipt.grandTotal,
            receiptNo = receipt.receiptNumber
        )

        val existingDate = prefs.getString("hash_$fp", null)
        return if (existingDate != null) {
            ReceiptDuplicate(
                isDuplicate = true,
                existingReceiptId = fp,
                matchedMerchant = receipt.merchantName,
                matchedTotal = receipt.grandTotal,
                matchedDate = existingDate
            )
        } else {
            ReceiptDuplicate(isDuplicate = false)
        }
    }

    /**
     * Registers a scanned receipt fingerprint in local memory.
     */
    fun registerReceiptFingerprint(context: Context, receipt: ScannedReceipt) {
        val prefs = context.getSharedPreferences("vesper_receipt_hashes", Context.MODE_PRIVATE)
        val fp = ReceiptImageProcessor.generateReceiptFingerprint(
            merchant = receipt.merchantName,
            date = receipt.dateString,
            amount = receipt.grandTotal,
            receiptNo = receipt.receiptNumber
        )
        prefs.edit().putString("hash_$fp", receipt.dateString).apply()
    }
}

object CategoryLearningEngine {

    private const val PREFS_NAME = "vesper_category_learning"

    /**
     * Learns category preference when user re-assigns an item.
     */
    fun learnUserPreference(context: Context, itemName: String, newCategory: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(itemName.lowercase().trim(), newCategory).apply()
    }

    /**
     * Returns learned category for an item if available.
     */
    fun getLearnedCategory(context: Context, itemName: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(itemName.lowercase().trim(), null)
    }

    /**
     * Resets all learned category rules.
     */
    fun resetLearnedRules(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
