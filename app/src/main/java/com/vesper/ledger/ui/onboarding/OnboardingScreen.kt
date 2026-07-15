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
import androidx.compose.ui.graphics.Brush
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
    val pages = listOf(
        OnboardingPage(
            title = "Track Every\nTransaction",
            description = "Add expenses and income effortlessly. Categorize, organize, and watch your financial picture form."
        ),
        OnboardingPage(
            title = "Stay on Track\nAutomatically",
            description = "Smart reminders and milestones keep you motivated. Build better money habits without thinking about it."
        ),
        OnboardingPage(
            title = "Split With\nFriends",
            description = "Share expenses naturally. Dinners, trips, and monthly bills settled without awkward conversations."
        ),
        OnboardingPage(
            title = "Your Data\nStays Yours",
            description = "Bank-grade encryption and biometric lock protect everything. Your finances remain completely private."
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
        // ── 1. Full-screen swipeable pager (illustrations) ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                val illustrationRes = when (page) {
                    0 -> com.vesper.ledger.R.drawable.ill_onboarding_1
                    1 -> com.vesper.ledger.R.drawable.ill_onboarding_2
                    2 -> com.vesper.ledger.R.drawable.ill_onboarding_3
                    else -> com.vesper.ledger.R.drawable.ill_onboarding_4
                }

                Image(
                    painter = painterResource(id = illustrationRes),
                    contentDescription = pages[page].title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.48f)
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp)
                )
            }
        }

        // ── 2. Gradient scrim blending illustration into UI ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.56f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bgColor.copy(alpha = 0f),
                            bgColor.copy(alpha = 0.7f),
                            bgColor,
                            bgColor
                        )
                    )
                )
        )

        // ── 3. Skip button (fixed top-right) ──
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

        // ── 4. Bottom content block (fixed vertical position) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text crossfade (fixed-height container)
            Crossfade(
                targetState = currentPage,
                animationSpec = tween(300),
                label = "text"
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Text(
                        text = pages[page].title,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBgColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 34.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pages[page].description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = onSurfaceVar.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pagination — expanding capsule dot
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

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button — tactile press, fixed position
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
