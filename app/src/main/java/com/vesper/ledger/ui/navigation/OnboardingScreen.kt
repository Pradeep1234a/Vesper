package com.vesper.ledger.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.isSystemInDarkTheme
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
            title = "Track Every Transaction",
            description = "Add expenses, income, and transfers in seconds with a clean and distraction-free workflow.",
            icon = Icons.Outlined.AccountBalanceWallet
        ),
        OnboardingPage(
            title = "Understand Your Spending",
            description = "Explore trends, categories, and spending patterns with powerful analytics designed for clarity.",
            icon = Icons.Outlined.BarChart
        ),
        OnboardingPage(
            title = "Your Data Stays Yours",
            description = "Everything is stored securely on your device without requiring an online account or cloud storage.",
            icon = Icons.Outlined.Shield
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
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Safe Area (24dp)
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Fixed Header (56dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogoV()
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "VESPER",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 2.sp
                    )
                }

                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.clickable { onNavigateNext() }
                )
            }

            // 2. Illustration Area (280dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // HorizontalPager captures swipe gestures across full width
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner screen size box aligned to center
                        Box(
                            modifier = Modifier.size(width = 124.dp, height = 238.dp)
                        ) {
                            when (page) {
                                0 -> OnboardingSlide1Content()
                                1 -> OnboardingSlide2Content()
                                2 -> OnboardingSlide3Content()
                            }
                        }
                    }
                }

                // Opaque mask overlay using offscreen composition to clear the phone viewport hole
                val maskColor = MaterialTheme.colorScheme.background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawRect(color = maskColor)

                            val rectWidth = 124.dp.toPx()
                            val rectHeight = 238.dp.toPx()
                            val rectLeft = (size.width - rectWidth) / 2f
                            val rectTop = (size.height - rectHeight) / 2f

                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = androidx.compose.ui.geometry.Offset(rectLeft, rectTop),
                                size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
                                blendMode = BlendMode.Clear
                            )
                        }
                )

                // Static Phone Outline Frame
                Box(
                    modifier = Modifier
                        .size(width = 136.dp, height = 250.dp)
                        .border(2.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                )
            }

            // 3. Title Container (96dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = pages[pagerState.currentPage].title,
                    transitionSpec = {
                        val slideDirection = if (targetState > initialState) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                        (slideIntoContainer(slideDirection, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350)))
                            .togetherWith(slideOutOfContainer(slideDirection, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350)))
                    },
                    label = "TitleTransition"
                ) { title ->
                    Text(
                        text = title,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                        maxLines = 2
                    )
                }
            }

            // 4. Description Container (88dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = pages[pagerState.currentPage].description,
                    transitionSpec = {
                        val slideDirection = if (targetState > initialState) AnimatedContentTransitionScope.SlideDirection.Left else AnimatedContentTransitionScope.SlideDirection.Right
                        (slideIntoContainer(slideDirection, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350)))
                            .togetherWith(slideOutOfContainer(slideDirection, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350)))
                    },
                    label = "DescTransition"
                ) { desc ->
                    Text(
                        text = desc,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        modifier = Modifier.fillMaxWidth(0.82f),
                        maxLines = 3
                    )
                }
            }

            // 5. Pagination Area (32dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
            }

            // 6. Primary Action Button Area (56dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center
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
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                        contentColor = if (isSystemInDarkTheme()) Color.Black else Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Bottom Safe Area (24dp)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun LogoV(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D0E11))
            .padding(6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.dp.toPx()

            // Left branch of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.18f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Right branch of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.18f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Stem of Y
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Horizontal baseline
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.82f),
                end = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // White dot
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f)
            )
        }
    }
}

@Composable
fun OnboardingSlide1Illustration() {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val cardBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentGreen = Color(0xFF16A34A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = 250.dp, height = 230.dp)
        ) {
            // 1. Main Transaction Card
            Card(
                modifier = Modifier
                    .size(width = 200.dp, height = 155.dp)
                    .align(Alignment.TopStart)
                    .border(1.dp, outlineColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Card Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = onBgColor
                        )
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = secTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Item 1: Coffee
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(cardBgColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Restaurant,
                                    contentDescription = null,
                                    tint = onBgColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Column {
                                Text("Coffee", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = onBgColor)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(Color(0xFFF43F5E), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Food & Dining", fontSize = 8.sp, color = secTextColor)
                                }
                            }
                        }
                        Text("-₹250", fontSize = 10.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = onBgColor)
                    }

                    // Item 2: Salary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(accentGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WorkOutline,
                                    contentDescription = null,
                                    tint = accentGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Column {
                                Text("Salary", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = onBgColor)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(accentGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Income", fontSize = 8.sp, color = secTextColor)
                                }
                            }
                        }
                        Text("+₹45,000", fontSize = 10.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = accentGreen)
                    }

                    // Item 3: Transfer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(cardBgColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = onBgColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Column {
                                Text("Transfer", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = onBgColor)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(Color.Gray, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("To Savings", fontSize = 8.sp, color = secTextColor)
                                }
                            }
                        }
                        Text("-₹5,000", fontSize = 10.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = onBgColor)
                    }
                }
            }

            // 2. Floating Action Button (+)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 48.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }

            // 3. Floating Wallet Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-16).dp, y = 92.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = onBgColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // 4. Category Icons Row at the Bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = (-12).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val catIcons = listOf(
                    Icons.Outlined.Restaurant,
                    Icons.Outlined.DirectionsCar,
                    Icons.Outlined.ShoppingBag,
                    Icons.Outlined.Home
                )
                catIcons.forEach { icon ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(1.dp, outlineColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = onBgColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide2Illustration() {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.size(width = 250.dp, height = 230.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Monthly Spending & Top Category
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Monthly Spending
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monthly Spending", fontSize = 8.sp, color = secTextColor)
                            Text("₹32,450", fontSize = 13.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = onBgColor)
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val path = Path().apply {
                                moveTo(0f, h * 0.8f)
                                lineTo(w * 0.2f, h * 0.7f)
                                lineTo(w * 0.4f, h * 0.85f)
                                lineTo(w * 0.6f, h * 0.4f)
                                lineTo(w * 0.8f, h * 0.5f)
                                lineTo(w, h * 0.15f)
                            }
                            drawPath(
                                path = path,
                                color = onBgColor,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }

                // Card 2: Top Category
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Top Category", fontSize = 8.sp, color = secTextColor)
                            Text("Food & Dining", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                            Text("28%", fontSize = 11.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = onBgColor)
                        }
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = outlineColor,
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                                drawArc(
                                    color = onBgColor,
                                    startAngle = -90f,
                                    sweepAngle = 100f,
                                    useCenter = false,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Spending Trend (Bar Chart)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Spending Trend (This Month)", fontSize = 8.sp, color = secTextColor)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val barHeights = listOf(0.3f, 0.45f, 0.25f, 0.7f, 0.5f, 0.85f, 0.4f, 0.6f)
                        barHeights.forEach { fraction ->
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .fillMaxHeight(fraction)
                                    .background(onBgColor.copy(alpha = 0.8f), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            )
                        }
                    }
                }
            }

            // Card 4: Heatmap
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f)
                    .border(1.dp, outlineColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Heatmap (This Week)", fontSize = 8.sp, color = secTextColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val intensities = listOf(0.1f, 0.5f, 0.8f, 0.2f, 0.9f, 0.3f, 0.6f)
                        intensities.forEach { intensity ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(onBgColor.copy(alpha = intensity), RoundedCornerShape(4.dp))
                                    .border(0.5.dp, outlineColor, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide1Content() {
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ADD TRANSACTION",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = secTextColor,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = "₹250.00",
            fontSize = 20.sp,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                )
                Text(
                    text = "Food & Dining",
                    fontSize = 7.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icons = listOf(
                Icons.Outlined.Restaurant,
                Icons.Outlined.DirectionsCar,
                Icons.Outlined.ShoppingBag,
                Icons.Outlined.Home
            )
            icons.forEachIndexed { index, icon ->
                val isSelected = index == 0
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (isSelected) Color.White.copy(alpha = 0.15f)
                            else Color.White.copy(alpha = 0.04f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            0.5.dp,
                            if (isSelected) Color.White.copy(alpha = 0.4f)
                            else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color.White, CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun OnboardingSlide2Content() {
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ANALYTICS",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = secTextColor,
            letterSpacing = 1.sp
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Monthly Spending", fontSize = 6.sp, color = secTextColor)
                Text("₹32,450", fontSize = 10.sp, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(0f, h * 0.8f)
                        lineTo(w * 0.2f, h * 0.7f)
                        lineTo(w * 0.4f, h * 0.85f)
                        lineTo(w * 0.6f, h * 0.4f)
                        lineTo(w * 0.8f, h * 0.5f)
                        lineTo(w, h * 0.15f)
                    }
                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("Top Category", fontSize = 6.sp, color = secTextColor)
                    Text("Food", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("28%", fontSize = 8.sp, fontFamily = SpaceGroteskFamily, color = secTextColor)
                }
                Box(
                    modifier = Modifier.size(20.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.White.copy(alpha = 0.1f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawArc(
                            color = Color.White,
                            startAngle = -90f,
                            sweepAngle = 100f,
                            useCenter = false,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Spending Trend", fontSize = 6.sp, color = secTextColor)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val heights = listOf(0.3f, 0.5f, 0.25f, 0.7f, 0.4f, 0.85f, 0.6f)
                    heights.forEach { fraction ->
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight(fraction)
                                .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide3Content() {
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PRIVACY FIRST",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = secTextColor,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "100% Local Storage",
                    fontSize = 7.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                    Text(
                        text = "Offline Mode Enabled",
                        fontSize = 7.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
