package com.vesper.ledger.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

// Onboarding page model
data class OnboardingPage(
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var pageIndex by remember { mutableStateOf(0) }
    
    val pages = listOf(
        OnboardingPage(
            title = "Digital Receipts",
            description = "Snap pictures of bills and receipts. We parse and categorize them instantly, keeping your records clean and organized."
        ),
        OnboardingPage(
            title = "Live Analytics",
            description = "Watch your expenses form clear trajectories. Pulse metrics and bento grids keep your visual rhythm in check."
        ),
        OnboardingPage(
            title = "Group Expense Splits",
            description = "Connect with friends to split dinners, trips, and monthly bills. Settle splits seamlessly without manual spreadsheets."
        )
    )

    val currentPage = pages[pageIndex]

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pageIndex < 2) {
                    Text(
                        text = "Skip",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onFinish() }
                    )
                } else {
                    Spacer(modifier = Modifier.height(20.dp)) // Maintain spacing
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1. Live Illustration Canvas (Full-screen feel, borderless)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f),
                contentAlignment = Alignment.Center
            ) {
                OnboardingIllustration(pageIndex = pageIndex)
            }

            // 2. Text Content and Actions Container (Fixed height to prevent layout shifts!)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Animated Text Content with smooth crossfade
                Crossfade(
                    targetState = currentPage,
                    animationSpec = tween(300),
                    label = "textContent"
                ) { targetPage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp) // Fixed height prevents shifting of indicators/buttons
                    ) {
                        Text(
                            text = targetPage.title,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = targetPage.description,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. Pagination Dots (Shadcn style: pill and dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val active = index == pageIndex
                        val width by animateDpAsState(if (active) 24.dp else 8.dp, label = "dotWidth")
                        val color = if (active) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outlineVariant
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Primary shadcn style action button with tactile press motion
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "buttonScale")

                Button(
                    onClick = {
                        if (pageIndex < 2) {
                            pageIndex++
                        } else {
                            onFinish()
                        }
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = if (pageIndex == 2) "Get Started" else "Continue",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingIllustration(pageIndex: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "illustration")

    // Scan line animation for page 0
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan"
    )

    // Bar heights animation for page 1
    val barHeight1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val barHeight2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val barHeight3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    // Coin offset animation for page 2
    val coinProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coin"
    )

    // Pre-resolve colors outside Canvas block to avoid @Composable invocation errors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // 1. Draw CENTRAL character (Identical on all pages for seamless pose)
        // Head
        drawCircle(
            color = onSurfaceVariant.copy(alpha = 0.15f),
            radius = 32.dp.toPx(),
            center = Offset(cx, cy - 20.dp.toPx())
        )
        drawCircle(
            color = onSurfaceVariant.copy(alpha = 0.4f),
            radius = 32.dp.toPx(),
            center = Offset(cx, cy - 20.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Body / Shoulders path
        val bodyPath = Path().apply {
            moveTo(cx - 50.dp.toPx(), cy + 80.dp.toPx())
            quadraticBezierTo(cx - 45.dp.toPx(), cy + 30.dp.toPx(), cx - 15.dp.toPx(), cy + 25.dp.toPx())
            lineTo(cx + 15.dp.toPx(), cy + 25.dp.toPx())
            quadraticBezierTo(cx + 45.dp.toPx(), cy + 30.dp.toPx(), cx + 50.dp.toPx(), cy + 80.dp.toPx())
        }
        drawPath(
            path = bodyPath,
            color = onSurfaceVariant.copy(alpha = 0.1f)
        )
        drawPath(
            path = bodyPath,
            color = onSurfaceVariant.copy(alpha = 0.4f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Hands holding a phone/device in the middle
        val phoneRect = Rect(
            offset = Offset(cx - 16.dp.toPx(), cy + 20.dp.toPx()),
            size = Size(32.dp.toPx(), 48.dp.toPx())
        )
        drawRoundRect(
            color = backgroundColor,
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
        )
        drawRoundRect(
            color = onSurfaceVariant.copy(alpha = 0.5f),
            topLeft = phoneRect.topLeft,
            size = phoneRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // 2. Draw PAGE-SPECIFIC floating animations (Canvas looks live!)
        when (pageIndex) {
            0 -> {
                // PAGE 0: Smart Bill Scanner
                val billTopLeft = Offset(cx - 110.dp.toPx(), cy - 40.dp.toPx())
                val billSize = Size(64.dp.toPx(), 90.dp.toPx())

                drawRoundRect(
                    color = surfaceVariantColor.copy(alpha = 0.5f),
                    topLeft = billTopLeft,
                    size = billSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )
                drawRoundRect(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    topLeft = billTopLeft,
                    size = billSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Dotted line connecting phone to bill
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = Offset(cx - 16.dp.toPx(), cy + 40.dp.toPx()),
                    end = Offset(billTopLeft.x + billSize.width, billTopLeft.y + billSize.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Receipt content lines
                for (i in 0..4) {
                    val lineY = billTopLeft.y + 16.dp.toPx() + i * 14.dp.toPx()
                    val lineW = if (i == 0) 36.dp.toPx() else if (i == 4) 20.dp.toPx() else 44.dp.toPx()
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.3f),
                        start = Offset(billTopLeft.x + 10.dp.toPx(), lineY),
                        end = Offset(billTopLeft.x + 10.dp.toPx() + lineW, lineY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Laser scan line sliding up/down across the bill
                val scanLineY = billTopLeft.y + 8.dp.toPx() + scanOffset * (billSize.height - 16.dp.toPx())
                drawLine(
                    color = primaryColor,
                    start = Offset(billTopLeft.x + 4.dp.toPx(), scanLineY),
                    end = Offset(billTopLeft.x + billSize.width - 4.dp.toPx(), scanLineY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            1 -> {
                // PAGE 1: Analytics Overview
                val cardTopLeft = Offset(cx + 50.dp.toPx(), cy - 50.dp.toPx())
                val cardSize = Size(80.dp.toPx(), 80.dp.toPx())

                drawRoundRect(
                    color = surfaceVariantColor.copy(alpha = 0.5f),
                    topLeft = cardTopLeft,
                    size = cardSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
                )
                drawRoundRect(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    topLeft = cardTopLeft,
                    size = cardSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Dotted line connecting phone to analytics card
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = Offset(cx + 16.dp.toPx(), cy + 40.dp.toPx()),
                    end = Offset(cardTopLeft.x, cardTopLeft.y + cardSize.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Draw pulsing bars
                val barW = 12.dp.toPx()
                val gap = 6.dp.toPx()
                val startX = cardTopLeft.x + 12.dp.toPx()
                val baseLimitY = cardTopLeft.y + cardSize.height - 12.dp.toPx()

                // Bar 1
                val h1 = 50.dp.toPx() * barHeight1
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(startX, baseLimitY - h1),
                    size = Size(barW, h1),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                // Bar 2
                val h2 = 50.dp.toPx() * barHeight2
                drawRoundRect(
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    topLeft = Offset(startX + barW + gap, baseLimitY - h2),
                    size = Size(barW, h2),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )

                // Bar 3
                val h3 = 50.dp.toPx() * barHeight3
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.5f),
                    topLeft = Offset(startX + (barW + gap) * 2, baseLimitY - h3),
                    size = Size(barW, h3),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                )
            }
            2 -> {
                // PAGE 2: Split Bills / Group Expense
                val friend1Center = Offset(cx - 95.dp.toPx(), cy - 35.dp.toPx())
                val friend2Center = Offset(cx + 95.dp.toPx(), cy - 35.dp.toPx())
                val radius = 16.dp.toPx()

                // Friend 1
                drawCircle(
                    color = onSurfaceVariant.copy(alpha = 0.15f),
                    radius = radius,
                    center = friend1Center
                )
                drawCircle(
                    color = onSurfaceVariant.copy(alpha = 0.4f),
                    radius = radius,
                    center = friend1Center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Friend 2
                drawCircle(
                    color = onSurfaceVariant.copy(alpha = 0.15f),
                    radius = radius,
                    center = friend2Center
                )
                drawCircle(
                    color = onSurfaceVariant.copy(alpha = 0.4f),
                    radius = radius,
                    center = friend2Center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Connect phone to Friend 1
                val path1Start = Offset(cx - 16.dp.toPx(), cy + 40.dp.toPx())
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = path1Start,
                    end = friend1Center,
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Connect phone to Friend 2
                val path2Start = Offset(cx + 16.dp.toPx(), cy + 40.dp.toPx())
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.3f),
                    start = path2Start,
                    end = friend2Center,
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Sliding coin along path 1
                val coin1X = path1Start.x + coinProgress * (friend1Center.x - path1Start.x)
                val coin1Y = path1Start.y + coinProgress * (friend1Center.y - path1Start.y)
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(coin1X, coin1Y)
                )

                // Sliding coin along path 2
                val coin2X = path2Start.x + coinProgress * (friend2Center.x - path2Start.x)
                val coin2Y = path2Start.y + coinProgress * (friend2Center.y - path2Start.y)
                drawCircle(
                    color = primaryColor,
                    radius = 6.dp.toPx(),
                    center = Offset(coin2X, coin2Y)
                )
            }
        }
    }
}
