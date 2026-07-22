package com.vesper.ledger.data.receipt

import androidx.compose.ui.geometry.Rect
import java.util.UUID

enum class ConfidenceLevel {
    HIGH,
    NEEDS_REVIEW
}

data class FieldConfidence(
    val value: String,
    val score: Float,
    val isNeedsReview: Boolean = score < 0.85f,
    val boundingBox: Rect? = null
)

data class ReceiptLineItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var quantity: Int = 1,
    var unitPrice: Double = 0.0,
    var totalPrice: Double = quantity * unitPrice,
    var category: String = "Uncategorized",
    var confidenceScore: Float = 0.95f,
    var boundingBox: Rect? = null
) {
    val isNeedsReview: Boolean get() = confidenceScore < 0.85f || name.isBlank() || totalPrice <= 0.0
}

data class CategorizedGroup(
    val categoryName: String,
    val items: MutableList<ReceiptLineItem>,
    val subtotal: Double,
    val allocatedTax: Double,
    val allocatedDiscount: Double,
    val allocatedServiceCharge: Double,
    val finalTotal: Double
)

data class ScannedReceipt(
    val id: String = UUID.randomUUID().toString(),
    var merchantName: String = "",
    var storeAddress: String = "",
    var invoiceNumber: String = "",
    var receiptNumber: String = "",
    var dateString: String = "",
    var timeString: String = "",
    var currencySymbol: String = "$",
    var subtotal: Double = 0.0,
    var taxAmount: Double = 0.0,
    var discountAmount: Double = 0.0,
    var serviceCharge: Double = 0.0,
    var grandTotal: Double = 0.0,
    var paymentMethod: String = "Card",
    var cardLastFour: String = "",
    var rawOcrText: String = "",
    var imageUriString: String = "",
    var confidenceScore: Float = 0.92f,
    val lineItems: MutableList<ReceiptLineItem> = mutableListOf(),
    val categorizedGroups: MutableList<CategorizedGroup> = mutableListOf(),
    var fieldConfidences: Map<String, FieldConfidence> = emptyMap(),
    var isDuplicateDetected: Boolean = false,
    var duplicateMatchMessage: String = ""
)

data class ReceiptDuplicate(
    val isDuplicate: Boolean,
    val existingReceiptId: String? = null,
    val matchedMerchant: String? = null,
    val matchedTotal: Double? = null,
    val matchedDate: String? = null
)

data class UndoRedoState(
    val lineItemsSnapshot: List<ReceiptLineItem>,
    val merchantName: String,
    val dateString: String,
    val paymentMethod: String
)
