package com.vesper.ledger.ui.split

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
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
import com.vesper.ledger.data.model.Account
import com.vesper.ledger.data.model.DebtSettlement
import com.vesper.ledger.data.model.GroupAnalyticsSummary
import com.vesper.ledger.data.model.SplitGroup
import com.vesper.ledger.data.model.SplitMember
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitGroupsScreen(
    viewModel: SplitViewModel,
    currencySymbol: String = "₹",
    onCreateGroupClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val groups by viewModel.allGroups.collectAsState()
    val totalOwedToUser by viewModel.totalOwedToUser.collectAsState()
    val totalUserOwes by viewModel.totalUserOwes.collectAsState()
    val overallDebts by viewModel.overallDebts.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val df = remember { DecimalFormat("#,##0.00") }
    var showAddExpenseGroup by remember { mutableStateOf<SplitGroup?>(null) }
    var showSettleDebt by remember { mutableStateOf<DebtSettlement?>(null) }
    var showAnalyticsGroup by remember { mutableStateOf<SplitGroup?>(null) }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isFabVisible by com.vesper.ledger.ui.components.rememberFabVisibility(lazyListState)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.vesper.ledger.ui.components.M3SingleFab(
                onClick = onCreateGroupClick,
                contentDescription = "Create Split Group",
                visible = isFabVisible,
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
                state = lazyListState,
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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

                                TextButton(
                                    onClick = onHistoryClick,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Split History", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

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
                                        text = "+$currencySymbol${df.format(totalOwedToUser)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
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
                                        text = "-$currencySymbol${df.format(totalUserOwes)}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. OVERALL DEBT SUMMARY CARDS (Who Owes Whom)
                if (overallDebts.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "WHO OWES WHOM SUMMARY",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            overallDebts.forEach { debt ->
                                DebtSummaryCard(
                                    debt = debt,
                                    currencySymbol = currencySymbol,
                                    df = df,
                                    onSettleUp = { showSettleDebt = debt }
                                )
                            }
                        }
                    }
                }

                // 3. GROUPS LIST / EMPTY STATE
                if (groups.isEmpty()) {
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

                                Button(
                                    onClick = onCreateGroupClick,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Split Group", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "ACTIVE GROUPS (${groups.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    items(groups, key = { it.id }) { group ->
                        val members by viewModel.getMembersForGroup(group.id).collectAsState(initial = emptyList())
                        val groupDebts by viewModel.getGroupDebts(group.id).collectAsState(initial = emptyList())

                        SplitGroupCard(
                            group = group,
                            members = members,
                            debts = groupDebts,
                            currencySymbol = currencySymbol,
                            df = df,
                            onAddExpense = { showAddExpenseGroup = group },
                            onAnalyticsClick = { showAnalyticsGroup = group },
                            onSettleUp = { debt -> showSettleDebt = debt },
                            onDeleteGroup = { viewModel.deleteGroup(group.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseGroup != null) {
        val group = showAddExpenseGroup!!
        val members by viewModel.getMembersForGroup(group.id).collectAsState(initial = emptyList())

        AddSplitExpenseDialog(
            group = group,
            members = members,
            accounts = accounts,
            currencySymbol = currencySymbol,
            onDismiss = { showAddExpenseGroup = null },
            onConfirm = { title, amount, paidByMemberId, paidByMemberName, paymentMethod, accountId, categoryName, sharesMap, isPaidByCurrentUser ->
                viewModel.addExpense(
                    groupId = group.id,
                    title = title,
                    totalAmount = amount,
                    paidByMemberId = paidByMemberId,
                    paidByMemberName = paidByMemberName,
                    paymentMethod = paymentMethod,
                    accountId = accountId,
                    categoryName = categoryName,
                    shares = sharesMap,
                    isPaidByCurrentUser = isPaidByCurrentUser,
                    onSuccess = {
                        Toast.makeText(context, "Split expense added successfully!", Toast.LENGTH_SHORT).show()
                        showAddExpenseGroup = null
                    }
                )
            }
        )
    }

    // Settle Up Dialog
    if (showSettleDebt != null) {
        val debt = showSettleDebt!!
        SettleUpDialog(
            debt = debt,
            accounts = accounts,
            currencySymbol = currencySymbol,
            df = df,
            onDismiss = { showSettleDebt = null },
            onConfirm = { amount, paymentMethod, accountId ->
                viewModel.recordSettlement(
                    groupId = debt.groupId,
                    debtorId = debt.debtorId,
                    debtorName = debt.debtorName,
                    creditorId = debt.creditorId,
                    creditorName = debt.creditorName,
                    amount = amount,
                    paymentMethod = paymentMethod,
                    accountId = accountId,
                    isDebtorCurrentUser = debt.isDebtorCurrentUser,
                    isCreditorCurrentUser = debt.isCreditorCurrentUser,
                    onSuccess = {
                        Toast.makeText(context, "Settlement recorded!", Toast.LENGTH_SHORT).show()
                        showSettleDebt = null
                    }
                )
            }
        )
    }

    // Group Analytics Dialog
    if (showAnalyticsGroup != null) {
        val group = showAnalyticsGroup!!
        val analytics by viewModel.getGroupAnalyticsSummary(group.id).collectAsState(initial = null)

        GroupAnalyticsDialog(
            group = group,
            analytics = analytics,
            currencySymbol = currencySymbol,
            df = df,
            onDismiss = { showAnalyticsGroup = null }
        )
    }
}

@Composable
fun DebtSummaryCard(
    debt: DebtSettlement,
    currencySymbol: String,
    df: DecimalFormat,
    onSettleUp: () -> Unit
) {
    val isOwedToUser = debt.isCreditorCurrentUser

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isOwedToUser) Color(0xFF16A34A).copy(alpha = 0.08f) else Color(0xFFDC2626).copy(alpha = 0.08f))
            .border(1.dp, if (isOwedToUser) Color(0xFF16A34A).copy(alpha = 0.2f) else Color(0xFFDC2626).copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = if (isOwedToUser) "${debt.debtorName} owes You" else "You owe ${debt.creditorName}",
                fontFamily = SpaceGroteskFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$currencySymbol${df.format(debt.amount)}",
                fontFamily = SpaceGroteskFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOwedToUser) Color(0xFF16A34A) else Color(0xFFDC2626)
            )
        }

        Button(
            onClick = onSettleUp,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOwedToUser) Color(0xFF16A34A) else Color(0xFFDC2626)
            )
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Settle Up", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SplitGroupCard(
    group: SplitGroup,
    members: List<SplitMember>,
    debts: List<DebtSettlement>,
    currencySymbol: String,
    df: DecimalFormat,
    onAddExpense: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onSettleUp: (DebtSettlement) -> Unit,
    onDeleteGroup: () -> Unit
) {
    ShCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = group.title,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${group.category} • ${members.size} Members",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onAnalyticsClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Analytics, contentDescription = "Group Analytics", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeleteGroup, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    Button(
                        onClick = onAddExpense,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Expense", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (debts.isNotEmpty()) {
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "GROUP SETTLEMENT STATUS",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    debts.forEach { debt ->
                        DebtSummaryCard(debt = debt, currencySymbol = currencySymbol, df = df, onSettleUp = { onSettleUp(debt) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpDialog(
    debt: DebtSettlement,
    accounts: List<Account>,
    currencySymbol: String,
    df: DecimalFormat,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String, accountId: Long) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.amount.toString()) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (debt.isCreditorCurrentUser) "Record Settlement from ${debt.debtorName}" else "Record Settlement to ${debt.creditorName}",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Settlement Amount ($currencySymbol)", fontFamily = PlusJakartaSansFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("PAYMENT METHOD", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "Cash", "Bank Transfer", "Card").forEach { pm ->
                        FilterChip(
                            selected = selectedPaymentMethod == pm,
                            onClick = { selectedPaymentMethod = pm },
                            label = { Text(pm, fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                        )
                    }
                }

                if (accounts.isNotEmpty()) {
                    Text("LINK TO MY ACCOUNT", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.take(3).forEach { acct ->
                            FilterChip(
                                selected = selectedAccount?.id == acct.id,
                                onClick = { selectedAccount = acct },
                                label = { Text(acct.name, fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        Toast.makeText(context, "Please enter valid settlement amount", Toast.LENGTH_SHORT).show()
                    } else {
                        onConfirm(amt, selectedPaymentMethod, selectedAccount?.id ?: 0L)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Confirm Settlement", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGroteskFamily)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupAnalyticsDialog(
    group: SplitGroup,
    analytics: GroupAnalyticsSummary?,
    currencySymbol: String,
    df: DecimalFormat,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${group.title} Analytics",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            if (analytics == null) {
                Box(modifier = Modifier.padding(24.dp)) { CircularProgressIndicator() }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Total Volume: $currencySymbol${df.format(analytics.totalExpenseVolume)}", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                    Text("Top Category: ${analytics.topCategory}", fontFamily = PlusJakartaSansFamily)
                    Text("Top Spender: ${analytics.topSpenderName} ($currencySymbol${df.format(analytics.topSpenderAmount)})", fontFamily = PlusJakartaSansFamily)

                    Divider()
                    Text("MEMBER NET BALANCES", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    analytics.memberBalances.forEach { (name, bal) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, fontFamily = PlusJakartaSansFamily)
                            Text(
                                text = if (bal >= 0) "+$currencySymbol${df.format(bal)}" else "-$currencySymbol${df.format(kotlin.math.abs(bal))}",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = if (bal >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSplitExpenseDialog(
    group: SplitGroup,
    members: List<SplitMember>,
    accounts: List<Account>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        paidByMemberId: Long,
        paidByMemberName: String,
        paymentMethod: String,
        accountId: Long,
        categoryName: String,
        sharesMap: Map<Long, Double>,
        isPaidByCurrentUser: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedMember by remember { mutableStateOf(members.firstOrNull()) }
    var selectedPaymentMethod by remember { mutableStateOf("UPI") }
    var selectedCategory by remember { mutableStateOf("Food & Groceries") }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var splitMode by remember { mutableStateOf("EQUAL") } // EQUAL, EXACT

    val customShares = remember { mutableStateMapOf<Long, String>() }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Expense to ${group.title}",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Dinner, Taxi)", fontFamily = PlusJakartaSansFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Amount ($currencySymbol)", fontFamily = PlusJakartaSansFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Split Mode Selector
                Text("SPLIT MODE", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = splitMode == "EQUAL",
                        onClick = { splitMode = "EQUAL" },
                        label = { Text("Equal", fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = splitMode == "EXACT",
                        onClick = { splitMode = "EXACT" },
                        label = { Text("Custom Exact", fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                    )
                }

                if (splitMode == "EXACT") {
                    Text("MEMBER SHARES", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    members.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(m.name, fontFamily = PlusJakartaSansFamily, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = customShares[m.id] ?: "",
                                onValueChange = { customShares[m.id] = it },
                                placeholder = { Text("0.00", fontFamily = SpaceGroteskFamily) },
                                singleLine = true,
                                modifier = Modifier.width(100.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Paid By Member Selector
                Text("PAID BY", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEach { m ->
                        val isSelected = selectedMember?.id == m.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMember = m },
                            label = { Text(m.name, fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                        )
                    }
                }

                // Payment Method Selector
                Text("PAYMENT METHOD", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("UPI", "Cash", "Card", "Bank Transfer").forEach { pm ->
                        FilterChip(
                            selected = selectedPaymentMethod == pm,
                            onClick = { selectedPaymentMethod = pm },
                            label = { Text(pm, fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                        )
                    }
                }

                // If paid by "Me", allow selecting linked main account
                if (selectedMember?.isCurrentUser == true && accounts.isNotEmpty()) {
                    Text("LINK TO MY ACCOUNT (Auto-Deduct)", fontFamily = SpaceGroteskFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.take(3).forEach { acct ->
                            val isSelected = selectedAccount?.id == acct.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedAccount = acct },
                                label = { Text(acct.name, fontFamily = SpaceGroteskFamily, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (title.isBlank() || amt == null || amt <= 0) {
                        Toast.makeText(context, "Please enter valid title and amount", Toast.LENGTH_SHORT).show()
                    } else if (selectedMember == null) {
                        Toast.makeText(context, "Please select who paid", Toast.LENGTH_SHORT).show()
                    } else {
                        val m = selectedMember!!
                        val calculatedShares = mutableMapOf<Long, Double>()

                        if (splitMode == "EXACT") {
                            var sumExact = 0.0
                            members.forEach { mem ->
                                val shareVal = customShares[mem.id]?.toDoubleOrNull() ?: 0.0
                                calculatedShares[mem.id] = shareVal
                                sumExact += shareVal
                            }
                            if (kotlin.math.abs(sumExact - amt) > 0.1) {
                                Toast.makeText(context, "Custom shares ($sumExact) must equal total amount ($amt)", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                        } else {
                            val eq = amt / (if (members.isNotEmpty()) members.size else 1)
                            members.forEach { mem -> calculatedShares[mem.id] = eq }
                        }

                        onConfirm(
                            title.trim(),
                            amt,
                            m.id,
                            m.name,
                            selectedPaymentMethod,
                            if (m.isCurrentUser) (selectedAccount?.id ?: 0L) else 0L,
                            selectedCategory,
                            calculatedShares,
                            m.isCurrentUser
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Add Expense", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = SpaceGroteskFamily)
            }
        }
    )
}
