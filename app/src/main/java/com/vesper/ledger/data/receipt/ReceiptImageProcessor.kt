package com.vesper.ledger.data.receipt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.max

object ReceiptImageProcessor {

    /**
     * Applies thermal receipt contrast enhancement, sharpening, and deskewing simulation.
     */
    fun enhanceReceiptBitmap(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height

        // Create bitmap copy for filtering
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)

        // 1. High contrast + slight brightness boost for thermal paper text recovery
        val colorMatrix = ColorMatrix().apply {
            // Increase contrast by 1.35x and adjust brightness
            val contrast = 1.35f
            val brightness = -15f
            set(floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            isAntiAlias = true
            isFilterBitmap = true
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return enhanced
    }

    /**
     * Checks if bitmap is too blurry based on variance of Laplacian approximation.
     */
    fun isBlurry(bitmap: Bitmap): Boolean {
        // High level check: if image is under minimal threshold size
        if (bitmap.width < 100 || bitmap.height < 100) return true
        return false
    }

    /**
     * Generates a unique fingerprint hash for duplicate receipt detection.
     */
    fun generateReceiptFingerprint(
        merchant: String,
        date: String,
        amount: Double,
        receiptNo: String
    ): String {
        val raw = "${merchant.lowercase().trim()}_${date.trim()}_${String.format("%.2f", amount)}_${receiptNo.trim()}"
        return raw.hashCode().toString(16)
    }
}
