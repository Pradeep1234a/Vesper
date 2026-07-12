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
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.TrendingDown
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
            title = "Minimalist Tracking",
            description = "Track income, expenses, and savings with a shadcn-inspired interface that prioritizes clarity, whitespace, and visual balance.",
            icon = Icons.Outlined.AccountBalanceWallet
        ),
        OnboardingPage(
            title = "Deep Analytics",
            description = "Analyze your spending rhythms and income sources with clean typographic hierarchy and highly responsive reports.",
            icon = Icons.Outlined.BarChart
        ),
        OnboardingPage(
            title = "100% Private & Local",
            description = "Your financial data never leaves your device. No cloud storage, no account creation, and absolute security by default.",
            icon = Icons.Outlined.Security
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // Zinc-950 (Shadcn Dark background)
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
                    color = Color(0xFFFAFAFA), // Zinc-50
                    letterSpacing = 3.sp
                )
                TextButton(onClick = onNavigateNext) {
                    Text(
                        text = "Skip",
                        color = Color(0xFFA1A1AA), // Zinc-400
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Shadcn Strict Card Container
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF27272A), // Zinc-800 border
                        shape = RoundedCornerShape(8.dp) // Strict 8dp corner radius
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF09090B) // Zinc-950 content fill
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
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
                            // Dynamic native UI visual anchors explaining the feature
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (page) {
                                    0 -> TrackingVisual()
                                    1 -> RetrievalVisual()
                                    2 -> HistoryVisual()
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Text(
                                text = onboardingPage.title,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFAFAFA), // Zinc-50
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = onboardingPage.description,
                                fontSize = 14.sp,
                                color = Color(0xFFA1A1AA), // Zinc-400
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Dot indicators inside the card
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(if (isSelected) 16.dp else 4.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFFFAFAFA) else Color(0xFF27272A))
                            )
                        }
                    }
                }
            }

            // Bottom CTA section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
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
                            color = Color(0xFF27272A),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp), // Strict 8dp corner radius
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFAFAFA), // Solid white background
                        contentColor = Color(0xFF09090B) // Pure black text
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
fun TrackingVisual() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card 1 (Expense)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
                .background(Color(0xFF09090B), RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFFAFAFA),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Grocery Store", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Food & Dining", fontSize = 10.sp, color = Color(0xFFA1A1AA))
                }
            }
            Text("-$42.50", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Card 2 (Income)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
                .background(Color(0xFF09090B), RoundedCornerShape(8.dp))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFFFAFAFA),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Freelance Client", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Salary", fontSize = 10.sp, color = Color(0xFFA1A1AA))
                }
            }
            Text("+$450.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun RetrievalVisual() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
                .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF71717A),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search transactions...", fontSize = 11.sp, color = Color(0xFF71717A))
        }

        // Mini Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Expense", "Income").forEachIndexed { idx, label ->
                val isActive = idx == 1 // Expense is active
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .background(
                            color = if (isActive) Color.White else Color(0xFF09090B),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color.White else Color(0xFF27272A),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF09090B) else Color(0xFFA1A1AA)
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryVisual() {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(105.dp)
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(8.dp))
            .background(Color(0xFF18181B), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Mini Bar Chart comparing Income, Expense, Savings
            // Bar 1: Income (taller, outline)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(0.85f)
                    .border(1.5.dp, Color.White, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Bar 2: Expense (filled)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(0.55f)
                    .background(Color.White, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
            // Bar 3: Savings (outlined)
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(0.35f)
                    .border(1.5.dp, Color(0xFFA1A1AA), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Timeline Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Income", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Expense", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Savings", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA1A1AA))
        }
    }
}
