package com.vesper.ledger.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume

object ReceiptOcrEngine {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Runs ML Kit OCR on a receipt bitmap and parses all receipt metadata, fields, and line items.
     */
    suspend fun processReceiptImage(context: Context, imageUri: Uri): ScannedReceipt {
        val inputImage = try {
            InputImage.fromFilePath(context, imageUri)
        } catch (e: Exception) {
            return createEmptyReceiptForImage(imageUri.toString())
        }

        val visionText = suspendCancellableCoroutine<Text?> { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { text ->
                    continuation.resume(text)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

        return if (visionText != null && visionText.text.isNotBlank()) {
            parseVisionText(visionText, imageUri.toString())
        } else {
            createEmptyReceiptForImage(imageUri.toString())
        }
    }

    private fun parseVisionText(visionText: Text, imageUriStr: String): ScannedReceipt {
        val rawText = visionText.text
        val lines = visionText.textBlocks.flatMap { block -> block.lines }

        var merchantName = ""
        var dateStr = ""
        var timeStr = ""
        var receiptNo = ""
        var invoiceNo = ""
        var grandTotal = 0.0
        var subtotal = 0.0
        var taxAmount = 0.0
        var discountAmount = 0.0
        var serviceCharge = 0.0
        var paymentMethod = "Card"
        var cardLast4 = ""
        var currency = "$"

        val lineItems = mutableListOf<ReceiptLineItem>()
        val confidences = mutableMapOf<String, FieldConfidence>()

        // 1. Merchant Extraction (Usually first prominent text line)
        if (lines.isNotEmpty()) {
            val firstLine = lines.firstOrNull { it.text.trim().length > 2 }?.text?.trim() ?: "Store Merchant"
            merchantName = cleanMerchantName(firstLine)
            confidences["merchantName"] = FieldConfidence(merchantName, 0.94f)
        }

        // Regex Patterns
        val datePattern = Pattern.compile("(?:\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b|\\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]* \\d{1,2},? \\d{4}\\b)", Pattern.CASE_INSENSITIVE)
        val timePattern = Pattern.compile("\\b\\d{1,2}:\\d{2}(?::\\d{2})?\\s*(?:AM|PM)?\\b", Pattern.CASE_INSENSITIVE)
        val pricePattern = Pattern.compile("(?:\\$|€|£|₹)?\\s*(\\d{1,5}\\.\\d{2})\\b")
        val receiptNoPattern = Pattern.compile("(?:RCPT|RECEIPT|INV|INVOICE|NO|#)[:.#\\s]*([A-Z0-9-]{4,16})", Pattern.CASE_INSENSITIVE)

        // Parse line by line
        for (line in lines) {
            val txt = line.text.trim()

            // Currency detection
            if (txt.contains("€")) currency = "€"
            else if (txt.contains("£")) currency = "£"
            else if (txt.contains("₹")) currency = "₹"

            // Date & Time
            if (dateStr.isEmpty()) {
                val m = datePattern.matcher(txt)
                if (m.find()) dateStr = m.group(0) ?: ""
            }
            if (timeStr.isEmpty()) {
                val m = timePattern.matcher(txt)
                if (m.find()) timeStr = m.group(0) ?: ""
            }

            // Invoice / Receipt No
            if (receiptNo.isEmpty()) {
                val m = receiptNoPattern.matcher(txt)
                if (m.find()) receiptNo = m.group(1) ?: ""
            }

            // Keyword Total, Tax, Subtotal
            val lower = txt.lowercase()
            val pm = pricePattern.matcher(txt)

            if (lower.contains("grand total") || lower.contains("total amount") || lower.contains("total:")) {
                if (pm.find()) {
                    grandTotal = pm.group(1)?.toDoubleOrNull() ?: grandTotal
                }
            } else if (lower.contains("subtotal") || lower.contains("sub total")) {
                if (pm.find()) subtotal = pm.group(1)?.toDoubleOrNull() ?: subtotal
            } else if (lower.contains("tax") || lower.contains("vat") || lower.contains("gst")) {
                if (pm.find()) taxAmount = pm.group(1)?.toDoubleOrNull() ?: taxAmount
            } else if (lower.contains("discount") || lower.contains("savings") || lower.contains("coupon")) {
                if (pm.find()) discountAmount = pm.group(1)?.toDoubleOrNull() ?: discountAmount
            } else if (lower.contains("service charge") || lower.contains("tip")) {
                if (pm.find()) serviceCharge = pm.group(1)?.toDoubleOrNull() ?: serviceCharge
            }

            // Payment method
            if (lower.contains("cash")) paymentMethod = "Cash"
            else if (lower.contains("visa")) { paymentMethod = "Visa Card"; cardLast4 = extractCardLast4(txt) }
            else if (lower.contains("mastercard") || lower.contains("mc")) { paymentMethod = "Mastercard"; cardLast4 = extractCardLast4(txt) }
            else if (lower.contains("upi") || lower.contains("gpay") || lower.contains("apple pay")) paymentMethod = "Digital Wallet"

            // Line Item Extraction (item description + price at end of line)
            val linePriceMatcher = Pattern.compile("^(.*?)\\s+(\\d+)?\\s*(?:x|@)?\\s*(?:\\$|€|£|₹)?\\s*(\\d{1,4}\\.\\d{2})$", Pattern.CASE_INSENSITIVE).matcher(txt)
            if (linePriceMatcher.find()) {
                val itemName = linePriceMatcher.group(1)?.trim() ?: ""
                val qtyStr = linePriceMatcher.group(2)
                val priceStr = linePriceMatcher.group(3)

                val itemTotal = priceStr?.toDoubleOrNull() ?: 0.0
                val qty = qtyStr?.toIntOrNull() ?: 1
                val unitPrice = if (qty > 0) itemTotal / qty else itemTotal

                if (itemName.length > 2 && !lower.contains("total") && !lower.contains("subtotal") && !lower.contains("tax") && !lower.contains("balance")) {
                    val box = line.boundingBox
                    val rect = if (box != null) Rect(box.left.toFloat(), box.top.toFloat(), box.right.toFloat(), box.bottom.toFloat()) else null
                    
                    lineItems.add(
                        ReceiptLineItem(
                            name = itemName,
                            quantity = qty,
                            unitPrice = unitPrice,
                            totalPrice = itemTotal,
                            confidenceScore = if (itemName.length > 3) 0.92f else 0.78f,
                            boundingBox = rect
                        )
                    )
                }
            }
        }

        if (dateStr.isEmpty()) dateStr = "Today"
        if (grandTotal == 0.0 && lineItems.isNotEmpty()) {
            grandTotal = lineItems.sumOf { it.totalPrice } + taxAmount + serviceCharge - discountAmount
        }
        if (subtotal == 0.0 && lineItems.isNotEmpty()) {
            subtotal = lineItems.sumOf { it.totalPrice }
        }

        // If no line items parsed, generate fallback parsed item list
        if (lineItems.isEmpty() && grandTotal > 0) {
            lineItems.add(
                ReceiptLineItem(
                    name = "$merchantName Purchase",
                    quantity = 1,
                    unitPrice = grandTotal,
                    totalPrice = grandTotal,
                    confidenceScore = 0.88f
                )
            )
        }

        confidences["grandTotal"] = FieldConfidence(grandTotal.toString(), 0.95f)
        confidences["date"] = FieldConfidence(dateStr, if (dateStr != "Today") 0.92f else 0.65f)

        val receipt = ScannedReceipt(
            merchantName = merchantName.ifBlank { "Retail Store" },
            receiptNumber = receiptNo,
            invoiceNumber = invoiceNo,
            dateString = dateStr,
            timeString = timeStr,
            currencySymbol = currency,
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            serviceCharge = serviceCharge,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            cardLastFour = cardLast4,
            rawOcrText = rawText,
            imageUriString = imageUriStr,
            lineItems = lineItems,
            fieldConfidences = confidences
        )

        // Run multi-category semantic categorization & proportional split calculation
        ReceiptCategorySplitter.categorizeAndSplit(receipt)

        return receipt
    }

    private fun cleanMerchantName(raw: String): String {
        return raw.replace(Regex("[^a-zA-Z0-9 &.'-]"), "").trim().take(30)
    }

    private fun extractCardLast4(text: String): String {
        val m = Pattern.compile("\\*{4}\\s*(\\d{4})").matcher(text)
        return if (m.find()) m.group(1) ?: "" else ""
    }

    fun createEmptyReceiptForImage(imageUriStr: String): ScannedReceipt {
        val now = java.util.Date()
        val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(now)
        val timeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(now)
        val receipt = ScannedReceipt(
            merchantName = "Scanned Receipt",
            dateString = dateStr,
            timeString = timeStr,
            currencySymbol = "₹",
            grandTotal = 0.0,
            imageUriString = imageUriStr,
            lineItems = mutableListOf()
        )
        return receipt
    }
}
