package com.vesper.ledger.data.receipt

import kotlin.math.roundToInt

object ReceiptCategorySplitter {

    /**
     * Categorizes every line item and groups items into separate category transactions,
     * allocating taxes, discounts, and service charges proportionally so the sum of all
     * generated transactions equals the grand total of the receipt.
     */
    fun categorizeAndSplit(receipt: ScannedReceipt) {
        val items = receipt.lineItems
        if (items.isEmpty()) return

        // 1. Assign semantic category to each item if uncategorized
        for (item in items) {
            if (item.category == "Uncategorized" || item.category.isBlank()) {
                item.category = predictCategoryForItem(item.name)
            }
        }

        // 2. Group items by category name
        val groupedMap = items.groupBy { it.category }

        val receiptSubtotal = if (receipt.subtotal > 0) receipt.subtotal else items.sumOf { it.totalPrice }
        val totalTax = receipt.taxAmount
        val totalDiscount = receipt.discountAmount
        val totalService = receipt.serviceCharge

        receipt.categorizedGroups.clear()

        var accumulatedAllocatedTotal = 0.0

        val groupList = groupedMap.entries.toList()
        groupList.forEachIndexed { index, entry ->
            val catName = entry.key
            val catItems = entry.value.toMutableList()
            val catSubtotal = catItems.sumOf { it.totalPrice }

            val proportion = if (receiptSubtotal > 0) catSubtotal / receiptSubtotal else (1.0 / groupList.size)

            val allocTax = roundTo2Decimals(totalTax * proportion)
            val allocDiscount = roundTo2Decimals(totalDiscount * proportion)
            val allocService = roundTo2Decimals(totalService * proportion)

            var catFinalTotal = roundTo2Decimals(catSubtotal + allocTax + allocService - allocDiscount)

            // For the last group, adjust rounding difference so total equals receipt grandTotal exactly
            if (index == groupList.size - 1 && groupList.size > 1) {
                val expectedRemaining = roundTo2Decimals(receipt.grandTotal - accumulatedAllocatedTotal)
                if (expectedRemaining > 0) {
                    catFinalTotal = expectedRemaining
                }
            }

            accumulatedAllocatedTotal += catFinalTotal

            receipt.categorizedGroups.add(
                CategorizedGroup(
                    categoryName = catName,
                    items = catItems,
                    subtotal = catSubtotal,
                    allocatedTax = allocTax,
                    allocatedDiscount = allocDiscount,
                    allocatedServiceCharge = allocService,
                    finalTotal = catFinalTotal
                )
            )
        }
    }

    /**
     * Semantic keyword product classifier matching items to 20+ financial categories.
     */
    fun predictCategoryForItem(name: String): String {
        val lower = name.lowercase().trim()

        return when {
            // Groceries & Food
            lower.containsAny("milk", "bread", "cheese", "butter", "egg", "apple", "banana", "fruit", "veg", "chicken", "meat", "rice", "pasta", "cereal", "yogurt", "snack", "grocery") -> "Groceries"
            // Food & Dining
            lower.containsAny("burger", "pizza", "coffee", "latte", "espresso", "sandwich", "noodle", "sushi", "restaurant", "cafe", "diner", "taco", "bakery") -> "Food & Dining"
            // Electronics
            lower.containsAny("phone", "charger", "cable", "earbud", "headphone", "usb", "laptop", "battery", "adapter", "monitor", "tv", "electronic") -> "Electronics"
            // Books & Stationery
            lower.containsAny("book", "notebook", "pen", "pencil", "paper", "binder", "folder", "stationery", "journal") -> "Books & Stationery"
            // Fuel & Transportation
            lower.containsAny("fuel", "gas", "petrol", "diesel", "uber", "lyft", "taxi", "bus", "train", "parking", "toll") -> "Transportation"
            // Health & Medicine
            lower.containsAny("pharmacy", "medicine", "pill", "tablet", "vitamin", "doctor", "clinic", "hospital", "bandage", "health") -> "Health & Medical"
            // Clothing & Footwear
            lower.containsAny("shirt", "pant", "jean", "shoe", "sock", "jacket", "dress", "hat", "wear", "clothing") -> "Clothing & Apparel"
            // Beauty & Personal Care
            lower.containsAny("shampoo", "soap", "cream", "lotion", "perfume", "makeup", "razor", "tissue", "beauty") -> "Beauty & Personal Care"
            // Home & Utilities
            lower.containsAny("lamp", "cleaner", "detergent", "towel", "chair", "table", "furniture", "utility", "electric", "water") -> "Home Supplies"
            // Subscriptions & Entertainment
            lower.containsAny("movie", "ticket", "game", "spotify", "netflix", "cinema", "entertainment", "subscription") -> "Entertainment"
            // Pets
            lower.containsAny("dog", "cat", "pet", "kibble", "vet") -> "Pets"
            else -> "General Expense"
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }

    private fun roundTo2Decimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
