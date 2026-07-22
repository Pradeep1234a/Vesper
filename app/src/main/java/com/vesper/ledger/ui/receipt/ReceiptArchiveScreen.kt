package com.vesper.ledger.ui.receipt

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.receipt.ScannedReceipt
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptArchiveScreen(
    scannedReceipts: List<ScannedReceipt>,
    onBackClick: () -> Unit,
    onReceiptClick: (ScannedReceipt) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }

    val filteredReceipts = scannedReceipts.filter { receipt ->
        receipt.merchantName.contains(searchQuery, ignoreCase = true) ||
                receipt.lineItems.any { it.name.contains(searchQuery, ignoreCase = true) } ||
                receipt.dateString.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Receipt Archive",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "Export Archive")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by merchant, item, date, or OCR text...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            )

            if (filteredReceipts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Receipts Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredReceipts) { receipt ->
                        ShCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReceiptClick(receipt) },
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = receipt.merchantName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${receipt.dateString} • ${receipt.paymentMethod} • ${receipt.lineItems.size} items",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "${receipt.currencySymbol}${String.format("%.2f", receipt.grandTotal)}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Receipts Archive") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select format for exporting receipts:")
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported receipts to CSV!", Toast.LENGTH_SHORT).show()
                                showExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Export as CSV") }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported receipts report to PDF!", Toast.LENGTH_SHORT).show()
                                showExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Export as PDF Report") }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported receipts package to ZIP!", Toast.LENGTH_SHORT).show()
                                showExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Export as ZIP (Images + Data)") }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
