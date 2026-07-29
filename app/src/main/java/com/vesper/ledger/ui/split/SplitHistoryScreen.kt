package com.vesper.ledger.ui.split

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.data.model.SplitExpense
import com.vesper.ledger.data.model.SplitGroup
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitHistoryScreen(
    viewModel: SplitViewModel,
    currencySymbol: String = "₹",
    onBackClick: () -> Unit = {}
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val totalOwedToUser by viewModel.totalOwedToUser.collectAsState()
    val totalUserOwes by viewModel.totalUserOwes.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PAID_BY_ME, OWED_TO_ME
    val df = remember { DecimalFormat("#,##0.00") }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    val filteredExpenses = remember(allExpenses, selectedFilter) {
        when (selectedFilter) {
            "PAID_BY_ME" -> allExpenses.filter { it.paidByMemberName.equals("Me", ignoreCase = true) }
            "OWED_TO_ME" -> allExpenses.filter { !it.paidByMemberName.equals("Me", ignoreCase = true) }
            else -> allExpenses
        }
    }

    Scaffold(
        topBar = {
            ChildHeader(
                title = "Split Transactions History",
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. SUMMARY METRICS HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Receivable Card
                ShCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column {
                        Text(
                            text = "TOTAL RECEIVABLE",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${df.format(totalOwedToUser)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }
                }

                // Payable Card
                ShCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column {
                        Text(
                            text = "TOTAL PAYABLE",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${df.format(totalUserOwes)}",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            // 2. FILTER CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "ALL" to "All Splits",
                    "PAID_BY_ME" to "Paid By Me",
                    "OWED_TO_ME" to "Owed To Me"
                )
                items(filters) { (key, label) ->
                    val isSelected = selectedFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // 3. EXPENSES LIST
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Split Transactions Found",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add a split expense in your groups to track history.",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        val group = allGroups.find { it.id == expense.groupId }
                        SplitHistoryItemCard(
                            expense = expense,
                            groupName = group?.title ?: "Split Group",
                            currencySymbol = currencySymbol,
                            df = df,
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplitHistoryItemCard(
    expense: SplitExpense,
    groupName: String,
    currencySymbol: String,
    df: DecimalFormat,
    dateFormat: SimpleDateFormat
) {
    var expanded by remember { mutableStateOf(false) }
    val isPaidByMe = expense.paidByMemberName.equals("Me", ignoreCase = true)

    ShCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isPaidByMe) Color(0xFF16A34A).copy(alpha = 0.12f)
                                else Color(0xFFDC2626).copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaidByMe) Icons.Outlined.ArrowUpward else Icons.Outlined.ArrowDownward,
                            contentDescription = null,
                            tint = if (isPaidByMe) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = expense.title,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = groupName,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Paid by ${expense.paidByMemberName}",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$currencySymbol${df.format(expense.totalAmount)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = expense.paymentMethod,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TRANSACTION DETAILS",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Category: ${expense.categoryName}",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Date: ${dateFormat.format(Date(expense.dateEpochMillis))}",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
