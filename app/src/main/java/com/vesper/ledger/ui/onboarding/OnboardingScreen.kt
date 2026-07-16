package com.vesper.ledger.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.launch

// ─── Data ────────────────────────────────────────────────────────────────────

data class OnboardingPage(
    val title: String,
    val description: String
)

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    // Premium Narrative Headlines and Expressions (consistent, uniform storytelling)
    val pages = listOf(
        OnboardingPage(
            title = "Spending becomes\npart of living",
            description = "Your purchases organize themselves quietly in the background. No manual entry. Just living."
        ),
        OnboardingPage(
            title = "Clarity replaces\nfinancial stress",
            description = "Understanding where your money goes happens naturally, during a quiet moment of reflection."
        ),
        OnboardingPage(
            title = "Splitting feels\nlike friendship",
            description = "Sharing dinner, trips, and expenses with people you love should never feel like a transaction."
        ),
        OnboardingPage(
            title = "Your peace of mind\nis absolute",
            description = "Bank-grade security and encryption work quietly in the background. Your data is always yours."
        )
    )

    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    val bgColor = MaterialTheme.colorScheme.background
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Top Header Bar (Logo + Skip Button) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = com.vesper.ledger.R.drawable.ic_launcher_foreground),
                contentDescription = "Vesper Brand Logo",
                tint = onBgColor,
                modifier = Modifier.size(36.dp)
            )
            
            if (currentPage < 3) {
                Text(
                    text = "Skip",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceVar.copy(alpha = 0.5f),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFinish() }
                )
            }
        }

        // ── Main unified content Column (Edge-to-edge layout) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 64.dp, bottom = 24.dp), // Added top margin to clear the logo header
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager containing BOTH Illustration and text for a seamless, synchronized swipe transition
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    val illustrationRes = when (page) {
                        0 -> if (isDark) com.vesper.ledger.R.drawable.ill_onboarding_1_dark else com.vesper.ledger.R.drawable.ill_onboarding_1
                        1 -> if (isDark) com.vesper.ledger.R.drawable.ill_onboarding_2_dark else com.vesper.ledger.R.drawable.ill_onboarding_2
                        2 -> if (isDark) com.vesper.ledger.R.drawable.ill_onboarding_3_dark else com.vesper.ledger.R.drawable.ill_onboarding_3
                        else -> if (isDark) com.vesper.ledger.R.drawable.ill_onboarding_4_dark else com.vesper.ledger.R.drawable.ill_onboarding_4
                    }

                    // Calculate page offset to apply transition effects (gesture transition motion)
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    val absOffset = Math.abs(pageOffset)
                    
                    // Gesture Transition Motion: scale, fade and parallax translation
                    val scale = 1f - (absOffset * 0.12f).coerceIn(0f, 0.12f)
                    val alpha = 1f - (absOffset * 0.8f).coerceIn(0f, 0.8f)
                    val translationX = pageOffset * 220f

                    // Illustration (true edge-to-edge size, responsive scaling, zero background blobs)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f) // Slightly reduced weight to pull text up
                            .heightIn(max = 340.dp)
                            .graphicsLayer {
                                this.alpha = alpha
                                this.scaleX = scale
                                this.scaleY = scale
                                this.translationX = translationX
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = illustrationRes),
                            contentDescription = pages[page].title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // Reduced height to pull text upward

                    // Text Content (wrapped, standard readable margins)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .graphicsLayer {
                                this.alpha = alpha
                                this.scaleX = scale
                                this.scaleY = scale
                                // Text parallax is slightly slower to create a beautiful 3D depth effect
                                this.translationX = translationX * 0.5f
                            }
                    ) {
                        Text(
                            text = pages[page].title,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = onBgColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = pages[page].description,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = onSurfaceVar.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(0.15f)) // Pulls everything upwards as a spring!
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Comfortable spacing before controls

            // Static controls at bottom (Pagination + CTA Button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pagination indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { i ->
                        val active = i == currentPage
                        val width by animateDpAsState(
                            if (active) 24.dp else 8.dp,
                            animationSpec = tween(250),
                            label = "dot"
                        )
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .clip(CircleShape)
                                .background(
                                    if (active) onBgColor
                                    else onSurfaceVar.copy(alpha = 0.2f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Cohesive gap before CTA

                // CTA Button
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    if (isPressed) 0.98f else 1f,
                    animationSpec = tween(120),
                    label = "btnScale"
                )

                Button(
                    onClick = {
                        if (currentPage < 3) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    currentPage + 1,
                                    animationSpec = tween(350, easing = FastOutSlowInEasing)
                                )
                            }
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
                        containerColor = onBgColor,
                        contentColor = bgColor
                    )
                ) {
                    Text(
                        text = if (currentPage == 3) "Get Started" else "Continue",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
