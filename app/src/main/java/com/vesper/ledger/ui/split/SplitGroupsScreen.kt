package com.vesper.ledger.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

data class SplitGroupItem(
    val id: String,
    val title: String,
    val category: String,
    val memberCount: Int,
    val netBalance: Double, // Positive = You are owed, Negative = You owe
    val icon: ImageVector,
    val members: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitGroupsScreen(
    currencySymbol: String = "₹",
    onCreateGroupClick: () -> Unit = {},
    onAddExpenseClick: (groupId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sampleGroups = remember { emptyList<SplitGroupItem>() }

    val totalOwed = sampleGroups.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    val totalOwe = sampleGroups.filter { it.netBalance < 0 }.sumOf { kotlin.math.abs(it.netBalance) }

    Scaffold(
        topBar = {
            com.vesper.ledger.ui.components.VesperUnifiedTopBar(
                title = "Split Expense Groups",
                isRoot = false,
                onNavigationClick = { },
                actions = {
                    IconButton(onClick = onCreateGroupClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Group",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SingleFab(
                onClick = onCreateGroupClick,
                contentDescription = "Create Split Group",
                hasBottomBar = false
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. NET SPLIT BALANCE BANNER
                item {
                    ShCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(18.dp)
                    ) {
                        Column {
                            Text(
                                text = "NET GROUP SPLIT BALANCE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.2.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "You are owed",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "+$currencySymbol${String.format("%,.2f", totalOwed)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E)
                                        )
                                    )
                                }

                                Divider(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .width(1.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                Column {
                                    Text(
                                        text = "You owe",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "-$currencySymbol${String.format("%,.2f", totalOwe)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. SECTION TITLE OR EMPTY STATE
                if (sampleGroups.isEmpty()) {
                    item {
                        ShCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Group,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No Split Groups Created Yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Create a group to start splitting bills, trips, and shared expenses with flatmates or friends.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = PlusJakartaSansFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "ACTIVE GROUPS (${sampleGroups.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    items(sampleGroups) { group ->
                    ShCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddExpenseClick(group.id) },
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = group.icon,
                                        contentDescription = group.title,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = group.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${group.memberCount} members • ${group.members.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = PlusJakartaSansFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    val isOwed = group.netBalance >= 0
                                    Text(
                                        text = if (isOwed) "You get" else "You owe",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = PlusJakartaSansFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "$currencySymbol${String.format("%,.2f", kotlin.math.abs(group.netBalance))}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOwed) Color(0xFF22C55E) else Color(0xFFEF4444)
                                        )
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onAddExpenseClick(group.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AddCard,
                                        contentDescription = "Add Expense",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Expense", fontSize = 12.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { /* Settle Up */ },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Settle Up",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Settle Up", fontSize = 12.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}
