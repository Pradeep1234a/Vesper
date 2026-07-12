package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateNext: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            title = "Fast Transaction Entry",
            description = "Quickly log your expenses and income inside a clean, keyboard-first entry form designed for speed.",
            icon = Icons.Outlined.AccountBalanceWallet
        ),
        OnboardingPage(
            title = "Organized Transactions",
            description = "Search, filter, and group transactions chronologically. Retrieve financial logs instantly without hassle.",
            icon = Icons.Outlined.BarChart
        ),
        OnboardingPage(
            title = "Financial Bento Overview",
            description = "Gain complete financial clarity with a modular bento dashboard displaying balances, goals, and savings progress.",
            icon = Icons.Outlined.Dashboard
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VESPER",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 3.sp
                )
                TextButton(onClick = onNavigateNext) {
                    Text(
                        text = "Skip",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Onboarding Pager Container (No outer card/border layout)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val onboardingPage = pages[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    // Visual Anchor Mockup (occupying 40-50% height, approx 260dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (page) {
                            0 -> AddTransactionPreview()
                            1 -> TransactionLogPreview()
                            2 -> BentoDashboardPreview()
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = onboardingPage.title,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = onboardingPage.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            // Bottom CTA section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isSelected) 16.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onBackground 
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateNext()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddTransactionPreview() {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(0.95f)
            .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Entry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBgColor
                )
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = secTextColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Big Amount Display
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Amount",
                    fontSize = 10.sp,
                    color = secTextColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$ 250.00",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = onBgColor
                )
            }

            // Type Selector (Segmented)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Expense", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Income", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = secTextColor)
                }
            }

            // Category tag badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                        .background(surfaceColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Utilities", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                }
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Food", fontSize = 9.sp, color = secTextColor)
                }
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Rent", fontSize = 9.sp, color = secTextColor)
                }
            }

            // Mini Note Text Input Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                    .background(surfaceColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Electricity Bill", fontSize = 10.sp, color = onBgColor)
            }

            // Submit Button
            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Add Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionLogPreview() {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(0.95f)
            .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mini Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                    .background(surfaceColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = secTextColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Search...", fontSize = 10.sp, color = secTextColor)
            }

            // Mini filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("All", "Expense", "Income").forEachIndexed { idx, label ->
                    val isActive = idx == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else outlineColor,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else secTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Timeline header group
            Text(
                text = "Today, 12 July",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = secTextColor
            )

            // Mini transaction stacked list cards
            // Item 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(surfaceColor, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.TrendingDown, null, tint = onBgColor, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Netflix", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                        Text("Utilities", fontSize = 8.sp, color = secTextColor)
                    }
                }
                Text("-$15.99", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBgColor)
            }

            // Item 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, outlineColor, RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(surfaceColor, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.TrendingUp, null, tint = onBgColor, modifier = Modifier.size(12.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Salary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                        Text("Work", fontSize = 8.sp, color = secTextColor)
                    }
                }
                Text("+$2,450", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = onBgColor)
            }
        }
    }
}

@Composable
fun BentoDashboardPreview() {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(0.95f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bento Row 1: Total Balance & Savings Goal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card A: Total Balance
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Balance", fontSize = 9.sp, color = secTextColor, fontWeight = FontWeight.Medium)
                    Text("$5,840.20", fontFamily = SpaceGroteskFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                }
            }

            // Card B: Savings Goal Progress
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Savings Goal", fontSize = 9.sp, color = secTextColor, fontWeight = FontWeight.Medium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Target", fontSize = 8.sp, color = secTextColor)
                            Text("70%", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                        }
                        // Progress bar indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(surfaceColor, RoundedCornerShape(2.dp))
                                .clip(RoundedCornerShape(2.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .fillMaxHeight()
                                    .background(onBgColor)
                            )
                        }
                    }
                }
            }
        }

        // Bento Row 2: Income & Expense cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card C: Income mini summary
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Income", fontSize = 9.sp, color = secTextColor)
                        Icon(Icons.Outlined.ArrowUpward, null, tint = onBgColor, modifier = Modifier.size(10.dp))
                    }
                    Text("+$3,200", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                }
            }

            // Card D: Expense mini summary
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expenses", fontSize = 9.sp, color = secTextColor)
                        Icon(Icons.Outlined.ArrowDownward, null, tint = onBgColor, modifier = Modifier.size(10.dp))
                    }
                    Text("-$840.10", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                }
            }
        }
    }
}
