package com.vesper.ledger.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Skip button (fixed top-right) ──
        if (currentPage < 3) {
            Text(
                text = "Skip",
                fontFamily = SpaceGroteskFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = onSurfaceVar.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFinish() }
            )
        }

        // ── Main unified content Column ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 24.dp, end = 24.dp),
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
                    Spacer(modifier = Modifier.height(16.dp))

                    val illustrationRes = when (page) {
                        0 -> com.vesper.ledger.R.drawable.ill_onboarding_1
                        1 -> com.vesper.ledger.R.drawable.ill_onboarding_2
                        2 -> com.vesper.ledger.R.drawable.ill_onboarding_3
                        else -> com.vesper.ledger.R.drawable.ill_onboarding_4
                    }

                    // Illustration (larger size, responsive scaling, minimal margins)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(max = 340.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Dynamic organic backdrop blob (theme-aware, zero eye strain)
                        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                        val blobColor = if (isDark) {
                            Color(0xFF1E293B).copy(alpha = 0.4f) // Faint slate in dark theme
                        } else {
                            Color(0xFFF1F5F9) // Soft grey in light theme
                        }
                        
                        val blobShape = RoundedCornerShape(
                            topStart = 130.dp,
                            topEnd = 115.dp,
                            bottomStart = 105.dp,
                            bottomEnd = 135.dp
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(260.dp)
                                .clip(blobShape)
                                .background(blobColor)
                        )

                        Image(
                            painter = painterResource(id = illustrationRes),
                            contentDescription = pages[page].title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp)) // Clean premium gap

                    // Text Content (wrapped, standard readable margins)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
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
                    
                    Spacer(modifier = Modifier.height(16.dp)) // Padding before controls
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Comfortable spacing before controls

            // Static controls at bottom (Pagination + CTA Button)
            Column(
                modifier = Modifier.fillMaxWidth(),
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
