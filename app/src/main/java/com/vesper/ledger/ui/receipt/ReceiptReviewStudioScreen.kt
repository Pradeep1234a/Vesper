package com.vesper.ledger.ui.receipt

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.vesper.ledger.data.receipt.*
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewStudioScreen(
    scannedReceipt: ScannedReceipt,
    onBackClick: () -> Unit,
    onCommitTransactions: (ScannedReceipt) -> Unit
) {
    val context = LocalContext.current

    var merchantName by remember { mutableStateOf(scannedReceipt.merchantName) }
    var dateString by remember { mutableStateOf(scannedReceipt.dateString) }
    var paymentMethod by remember { mutableStateOf(scannedReceipt.paymentMethod) }
    var currencySymbol by remember { mutableStateOf(scannedReceipt.currencySymbol) }

    val lineItems = remember { mutableStateListOf<ReceiptLineItem>().apply { addAll(scannedReceipt.lineItems) } }

    // Undo / Redo history stacks
    val undoStack = remember { mutableStateListOf<UndoRedoState>() }
    val redoStack = remember { mutableStateListOf<UndoRedoState>() }

    fun saveSnapshot() {
        undoStack.add(
            UndoRedoState(
                lineItemsSnapshot = lineItems.map { it.copy() },
                merchantName = merchantName,
                dateString = dateString,
                paymentMethod = paymentMethod
            )
        )
        redoStack.clear()
    }

    fun recalculateTotals() {
        scannedReceipt.merchantName = merchantName
        scannedReceipt.dateString = dateString
        scannedReceipt.paymentMethod = paymentMethod
        scannedReceipt.currencySymbol = currencySymbol
        scannedReceipt.lineItems.clear()
        scannedReceipt.lineItems.addAll(lineItems)

        // Re-run category split & proportional tax/discount math
        ReceiptCategorySplitter.categorizeAndSplit(scannedReceipt)
    }

    // Initial calculation
    recalculateTotals()

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Receipt Studio",
                onBackClick = onBackClick,
                actions = {
                    // Undo Button
                    IconButton(
                        enabled = undoStack.isNotEmpty(),
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                val state = undoStack.removeAt(undoStack.lastIndex)
                                redoStack.add(
                                    UndoRedoState(
                                        lineItemsSnapshot = lineItems.map { it.copy() },
                                        merchantName = merchantName,
                                        dateString = dateString,
                                        paymentMethod = paymentMethod
                                    )
                                )
                                merchantName = state.merchantName
                                dateString = state.dateString
                                paymentMethod = state.paymentMethod
                                lineItems.clear()
                                lineItems.addAll(state.lineItemsSnapshot)
                                recalculateTotals()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) Color.White else Color.Gray
                        )
                    }

                    // Redo Button
                    IconButton(
                        enabled = redoStack.isNotEmpty(),
                        onClick = {
                            if (redoStack.isNotEmpty()) {
                                val state = redoStack.removeAt(redoStack.lastIndex)
                                undoStack.add(
                                    UndoRedoState(
                                        lineItemsSnapshot = lineItems.map { it.copy() },
                                        merchantName = merchantName,
                                        dateString = dateString,
                                        paymentMethod = paymentMethod
                                    )
                                )
                                merchantName = state.merchantName
                                dateString = state.dateString
                                paymentMethod = state.paymentMethod
                                lineItems.clear()
                                lineItems.addAll(state.lineItemsSnapshot)
                                recalculateTotals()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Redo,
                            contentDescription = "Redo",
                            tint = if (redoStack.isNotEmpty()) Color.White else Color.Gray
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        recalculateTotals()
                        ReceiptDuplicateDetector.registerReceiptFingerprint(context, scannedReceipt)
                        onCommitTransactions(scannedReceipt)
                        Toast.makeText(context, "Scanned receipt committed to ledger!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import ${scannedReceipt.categorizedGroups.size} Category Transactions ($currencySymbol${String.format("%.2f", scannedReceipt.grandTotal)})",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
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
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Possible duplicate receipt detected for ${scannedReceipt.merchantName} on ${scannedReceipt.dateString}.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFF59E0B),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Header Info Card: Merchant, Date, Payment Method
            item {
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "RECEIPT DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )

                        OutlinedTextField(
                            value = merchantName,
                            onValueChange = {
                                saveSnapshot()
                                merchantName = it
                                recalculateTotals()
                            },
                            label = { Text("Merchant Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = dateString,
                                onValueChange = {
                                    saveSnapshot()
                                    dateString = it
                                    recalculateTotals()
                                },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )

                            OutlinedTextField(
                                value = paymentMethod,
                                onValueChange = {
                                    saveSnapshot()
                                    paymentMethod = it
                                    recalculateTotals()
                                },
                                label = { Text("Payment Method") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // Section Header for Items
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXTRACTED LINE ITEMS (${lineItems.size})",
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
                                    unitPrice = 1.00,
                                    totalPrice = 1.00,
                                    category = "General Expense"
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

            // Line Items List
            itemsIndexed(lineItems) { idx, item ->
                ShCard(
                    modifier = Modifier.fillMaxWidth(),
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
                                            .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Needs Review",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFEF4444),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF16A34A).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "High Confidence",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF22C55E),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
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
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
                            OutlinedTextField(
                                value = item.category,
                                onValueChange = { newCat ->
                                    saveSnapshot()
                                    item.category = newCat
                                    CategoryLearningEngine.learnUserPreference(context, item.name, newCat)
                                    recalculateTotals()
                                },
                                label = { Text("Category") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp)
                            )

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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.categoryName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.2f", group.finalTotal)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "${group.items.size} item(s): ${group.items.joinToString { it.name }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        )

                        Text(
                            text = "Allocated Tax: $currencySymbol${String.format("%.2f", group.allocatedTax)}  •  Discount: -$currencySymbol${String.format("%.2f", group.allocatedDiscount)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
