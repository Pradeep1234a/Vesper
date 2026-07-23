package com.vesper.ledger.ui.receipt

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.receipt.CategoryLearningEngine
import com.vesper.ledger.data.receipt.ReceiptCategorySplitter
import com.vesper.ledger.data.receipt.ReceiptLineItem
import com.vesper.ledger.data.receipt.ScannedReceipt
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewStudioScreen(
    scannedReceipt: ScannedReceipt,
    currencySymbol: String = "$",
    onBackClick: () -> Unit,
    onConfirmSave: (ScannedReceipt) -> Unit
) {
    val context = LocalContext.current
    val df = DecimalFormat("#,##0.00")

    val supportedCategories = listOf(
        "Groceries", "Food & Dining", "Electronics", "Books & Stationery",
        "Clothing & Apparel", "Beauty & Personal Care", "Health & Medical",
        "Home Supplies", "Entertainment", "Transportation", "Pets", "General Expense"
    )

    // Form state
    var merchantName by remember { mutableStateOf(scannedReceipt.merchantName) }
    var dateString by remember { mutableStateOf(scannedReceipt.dateString) }
    var grandTotalStr by remember { mutableStateOf(scannedReceipt.grandTotal.toString()) }
    var taxStr by remember { mutableStateOf(scannedReceipt.taxAmount.toString()) }
    var discountStr by remember { mutableStateOf(scannedReceipt.discountAmount.toString()) }

    val lineItems = remember { mutableStateListOf(*scannedReceipt.lineItems.toTypedArray()) }
    var selectedItemId by remember { mutableStateOf<String?>(lineItems.firstOrNull()?.id) }

    // Undo / Redo Stack History
    val historyStack = remember { mutableStateListOf<List<ReceiptLineItem>>() }
    val redoStack = remember { mutableStateListOf<List<ReceiptLineItem>>() }

    fun saveSnapshot() {
        historyStack.add(lineItems.map { it.copy() })
        redoStack.clear()
    }

    fun recalculateTotals() {
        scannedReceipt.merchantName = merchantName
        scannedReceipt.dateString = dateString
        scannedReceipt.grandTotal = grandTotalStr.toDoubleOrNull() ?: scannedReceipt.grandTotal
        scannedReceipt.taxAmount = taxStr.toDoubleOrNull() ?: scannedReceipt.taxAmount
        scannedReceipt.discountAmount = discountStr.toDoubleOrNull() ?: scannedReceipt.discountAmount
        scannedReceipt.lineItems.clear()
        scannedReceipt.lineItems.addAll(lineItems)

        // Re-run category splitting & tax allocation math
        ReceiptCategorySplitter.categorizeAndSplit(scannedReceipt)
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Review & Edit",
                onBackClick = onBackClick,
                actions = {
                    // Undo button
                    IconButton(
                        onClick = {
                            if (historyStack.isNotEmpty()) {
                                redoStack.add(lineItems.map { it.copy() })
                                val prev = historyStack.removeAt(historyStack.size - 1)
                                lineItems.clear()
                                lineItems.addAll(prev)
                                recalculateTotals()
                            }
                        },
                        enabled = historyStack.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Undo, contentDescription = "Undo", tint = if (historyStack.isNotEmpty()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }

                    // Redo button
                    IconButton(
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                historyStack.add(lineItems.map { it.copy() })
                                val next = redoStack.removeAt(redoStack.size - 1)
                                lineItems.clear()
                                lineItems.addAll(next)
                                recalculateTotals()
                            }
                        },
                        enabled = redoStack.isNotEmpty()
                    ) {
                        Icon(Icons.Filled.Redo, contentDescription = "Redo", tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(
                            text = "$currencySymbol${df.format(scannedReceipt.grandTotal)}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    Button(
                        onClick = {
                            recalculateTotals()
                            onConfirmSave(scannedReceipt)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Transactions", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Interactive Zoomable Canvas
            item {
                Text(
                    text = "RECEIPT CANVAS & OCR HIGHLIGHT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                ReceiptInteractiveCanvas(
                    imageUriStr = scannedReceipt.imageUriString,
                    lineItems = lineItems,
                    selectedItemId = selectedItemId,
                    onItemSelected = { selectedItemId = it }
                )
            }

            // Duplicate Warning Alert if detected
            if (scannedReceipt.isDuplicateDetected) {
                item {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Possible duplicate receipt detected for ${scannedReceipt.merchantName} on ${scannedReceipt.dateString}.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Merchant & Overview Form
            item {
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Merchant & Overview",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = {
                                merchantName = it
                                recalculateTotals()
                            },
                            label = { Text("Merchant Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dateString,
                                onValueChange = {
                                    dateString = it
                                    recalculateTotals()
                                },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = grandTotalStr,
                                onValueChange = {
                                    grandTotalStr = it
                                    recalculateTotals()
                                },
                                label = { Text("Grand Total ($currencySymbol)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Line Items List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PURCHASED LINE ITEMS (${lineItems.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )

                    TextButton(
                        onClick = {
                            saveSnapshot()
                            lineItems.add(
                                ReceiptLineItem(
                                    name = "New Purchased Item",
                                    quantity = 1,
                                    unitPrice = 0.0,
                                    totalPrice = 0.0,
                                    category = "General Expense",
                                    confidenceScore = 1.0f
                                )
                            )
                            recalculateTotals()
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Editable Line Items
            itemsIndexed(lineItems) { idx, item ->
                var expandedCategoryMenu by remember { mutableStateOf(false) }

                ShCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (selectedItemId == item.id) 1.5.dp else 0.dp,
                            color = if (selectedItemId == item.id) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedItemId = item.id },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (item.isNeedsReview) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Needs Review",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "High Confidence",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    saveSnapshot()
                                    lineItems.removeAt(idx)
                                    recalculateTotals()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }

                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { newName ->
                                saveSnapshot()
                                item.name = newName
                                item.category = ReceiptCategorySplitter.predictCategoryForItem(newName)
                                recalculateTotals()
                            },
                            label = { Text("Item Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Category Dropdown Picker
                            Box(modifier = Modifier.weight(1.2f)) {
                                OutlinedTextField(
                                    value = item.category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        IconButton(onClick = { expandedCategoryMenu = true }) {
                                            Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Select Category")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedCategoryMenu = true },
                                    shape = RoundedCornerShape(10.dp)
                                )

                                DropdownMenu(
                                    expanded = expandedCategoryMenu,
                                    onDismissRequest = { expandedCategoryMenu = false }
                                ) {
                                    supportedCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                saveSnapshot()
                                                item.category = cat
                                                CategoryLearningEngine.learnUserPreference(context, item.name, cat)
                                                expandedCategoryMenu = false
                                                recalculateTotals()
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = item.totalPrice.toString(),
                                onValueChange = { priceStr ->
                                    saveSnapshot()
                                    item.totalPrice = priceStr.toDoubleOrNull() ?: item.totalPrice
                                    recalculateTotals()
                                },
                                label = { Text("Total ($currencySymbol)") },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Multi-Category Transaction Auto-Split Overview
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AUTO-GENERATED CATEGORY TRANSACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            itemsIndexed(scannedReceipt.categorizedGroups) { _, group ->
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = group.categoryName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${group.items.size} item(s) • Tax: $currencySymbol${df.format(group.allocatedTax)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Text(
                            text = "$currencySymbol${df.format(group.finalTotal)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }
        }
    }
}
