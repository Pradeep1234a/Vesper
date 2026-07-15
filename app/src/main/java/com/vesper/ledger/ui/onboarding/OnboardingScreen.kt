package com.vesper.ledger.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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

// ─── Fixed Illustration Palette (theme-independent) ──────────────────────────

private val IllPrimary = Color(0xFF1A1A1A)
private val IllSecondary = Color(0xFF444444)
private val IllNeutral = Color(0xFF888888)
private val IllHighlight = Color(0xFFD9D9D9)
private val IllAccent = Color(0xFF333333)

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
        // ── 1. Full-screen swipeable pager (illustration in top ~48%) ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.48f)
                        .align(Alignment.TopCenter)
                ) {
                    when (page) {
                        0 -> drawTransactionsScene()
                        1 -> drawNotificationsScene()
                        2 -> drawSplitScene()
                        3 -> drawSecurityScene()
                    }
                }
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

// ═════════════════════════════════════════════════════════════════════════════
// ILLUSTRATIONS — Theme-independent, fixed palette, editorial fintech style
// ═════════════════════════════════════════════════════════════════════════════

// ─── Screen 1: Add & Manage Transactions ─────────────────────────────────────

private fun DrawScope.drawTransactionsScene() {
    val w = size.width
    val h = size.height

    // ── Ambient background shapes ──
    drawCircle(IllHighlight.copy(alpha = 0.25f), w * 0.28f, Offset(w * 0.75f, h * 0.22f))
    drawCircle(IllHighlight.copy(alpha = 0.15f), w * 0.18f, Offset(w * 0.15f, h * 0.65f))

    // ── Desk surface ──
    val deskY = h * 0.62f
    drawRoundRect(
        IllHighlight.copy(alpha = 0.35f),
        Offset(w * 0.08f, deskY),
        Size(w * 0.84f, h * 0.06f),
        CornerRadius(4.dp.toPx())
    )

    // ── Receipt on desk ──
    val rcptX = w * 0.18f
    val rcptY = deskY - 56.dp.toPx()
    val rcptW = 38.dp.toPx()
    val rcptH = 52.dp.toPx()
    drawRoundRect(IllHighlight.copy(0.50f), Offset(rcptX, rcptY), Size(rcptW, rcptH), CornerRadius(3.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.30f), Offset(rcptX, rcptY), Size(rcptW, rcptH), CornerRadius(3.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Receipt lines
    for (i in 0..5) {
        val ly = rcptY + 8.dp.toPx() + i * 7.dp.toPx()
        val lw = if (i == 0) 18.dp.toPx() else if (i == 5) 12.dp.toPx() else 26.dp.toPx()
        drawLine(IllNeutral.copy(0.25f), Offset(rcptX + 6.dp.toPx(), ly), Offset(rcptX + 6.dp.toPx() + lw, ly), 1.5.dp.toPx(), cap = StrokeCap.Round)
    }

    // ── Phone showing transaction list ──
    val phX = w * 0.42f
    val phY = deskY - 68.dp.toPx()
    val phW = 36.dp.toPx()
    val phH = 64.dp.toPx()
    drawRoundRect(IllHighlight.copy(0.60f), Offset(phX, phY), Size(phW, phH), CornerRadius(6.dp.toPx()))
    drawRoundRect(IllSecondary.copy(0.35f), Offset(phX, phY), Size(phW, phH), CornerRadius(6.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Screen content lines (transaction list)
    for (i in 0..4) {
        val ly = phY + 10.dp.toPx() + i * 10.dp.toPx()
        drawLine(IllNeutral.copy(0.20f), Offset(phX + 5.dp.toPx(), ly), Offset(phX + phW - 5.dp.toPx(), ly), 1.5.dp.toPx(), cap = StrokeCap.Round)
        // Amount on right
        drawLine(IllSecondary.copy(0.15f), Offset(phX + phW - 14.dp.toPx(), ly), Offset(phX + phW - 5.dp.toPx(), ly), 1.5.dp.toPx(), cap = StrokeCap.Round)
    }

    // ── Stacked coins ──
    val coinX = w * 0.70f
    val coinBaseY = deskY - 4.dp.toPx()
    for (i in 0..2) {
        val cy = coinBaseY - i * 6.dp.toPx()
        drawRoundRect(
            IllHighlight.copy(0.50f),
            Offset(coinX, cy - 6.dp.toPx()),
            Size(18.dp.toPx(), 6.dp.toPx()),
            CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            IllNeutral.copy(0.35f),
            Offset(coinX, cy - 6.dp.toPx()),
            Size(18.dp.toPx(), 6.dp.toPx()),
            CornerRadius(3.dp.toPx()),
            style = Stroke(1.dp.toPx())
        )
    }

    // ── Person standing, reaching toward desk ──
    val px = w * 0.58f
    val headCy = deskY - 110.dp.toPx()
    val headR = 18.dp.toPx()

    // Head
    drawCircle(IllHighlight.copy(0.45f), headR, Offset(px, headCy))
    drawCircle(IllSecondary.copy(0.40f), headR, Offset(px, headCy), style = Stroke(1.8.dp.toPx()))
    // Hair
    val hairPath = Path().apply {
        moveTo(px - 14.dp.toPx(), headCy - 10.dp.toPx())
        quadraticBezierTo(px, headCy - headR - 5.dp.toPx(), px + 15.dp.toPx(), headCy - 8.dp.toPx())
    }
    drawPath(hairPath, IllSecondary.copy(0.30f), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    // Neck
    drawLine(IllSecondary.copy(0.25f), Offset(px, headCy + headR), Offset(px - 1.dp.toPx(), headCy + headR + 10.dp.toPx()), 1.8.dp.toPx())
    // Torso
    val torso = Path().apply {
        moveTo(px - 32.dp.toPx(), deskY - 8.dp.toPx())
        quadraticBezierTo(px - 26.dp.toPx(), headCy + headR + 18.dp.toPx(), px, headCy + headR + 12.dp.toPx())
        quadraticBezierTo(px + 26.dp.toPx(), headCy + headR + 18.dp.toPx(), px + 32.dp.toPx(), deskY - 8.dp.toPx())
    }
    drawPath(torso, IllHighlight.copy(0.30f))
    drawPath(torso, IllSecondary.copy(0.30f), style = Stroke(1.8.dp.toPx()))
    // Arm reaching toward phone
    val armPath = Path().apply {
        moveTo(px - 26.dp.toPx(), deskY - 18.dp.toPx())
        quadraticBezierTo(px - 34.dp.toPx(), deskY - 14.dp.toPx(), phX + phW + 2.dp.toPx(), deskY - 6.dp.toPx())
    }
    drawPath(armPath, IllSecondary.copy(0.28f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // ── Shopping bag ──
    val bagX = w * 0.80f
    val bagY = deskY - 28.dp.toPx()
    val bagW = 20.dp.toPx()
    val bagH = 24.dp.toPx()
    drawRoundRect(IllHighlight.copy(0.40f), Offset(bagX, bagY), Size(bagW, bagH), CornerRadius(2.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.30f), Offset(bagX, bagY), Size(bagW, bagH), CornerRadius(2.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Handle
    val handleP = Path().apply {
        moveTo(bagX + 5.dp.toPx(), bagY)
        quadraticBezierTo(bagX + bagW / 2, bagY - 10.dp.toPx(), bagX + bagW - 5.dp.toPx(), bagY)
    }
    drawPath(handleP, IllNeutral.copy(0.30f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))
}

// ─── Screen 2: Smart Notifications & Encouragement ───────────────────────────

private fun DrawScope.drawNotificationsScene() {
    val w = size.width
    val h = size.height

    // ── Ambient shapes ──
    drawCircle(IllHighlight.copy(alpha = 0.20f), w * 0.22f, Offset(w * 0.20f, h * 0.25f))
    drawRoundRect(IllHighlight.copy(0.12f), Offset(w * 0.60f, h * 0.10f), Size(w * 0.35f, h * 0.30f), CornerRadius(20.dp.toPx()))

    val seatY = h * 0.62f

    // ── Couch / chair suggestion ──
    val couchPath = Path().apply {
        moveTo(w * 0.15f, seatY + 8.dp.toPx())
        quadraticBezierTo(w * 0.50f, seatY + 14.dp.toPx(), w * 0.85f, seatY + 8.dp.toPx())
    }
    drawPath(couchPath, IllHighlight.copy(0.30f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    // Armrests
    drawLine(IllHighlight.copy(0.25f), Offset(w * 0.15f, seatY - 20.dp.toPx()), Offset(w * 0.15f, seatY + 8.dp.toPx()), 3.dp.toPx(), cap = StrokeCap.Round)
    drawLine(IllHighlight.copy(0.25f), Offset(w * 0.85f, seatY - 20.dp.toPx()), Offset(w * 0.85f, seatY + 8.dp.toPx()), 3.dp.toPx(), cap = StrokeCap.Round)

    // ── Person sitting, relaxed, phone in hand ──
    val px = w * 0.48f
    val headCy = seatY - 90.dp.toPx()
    val headR = 18.dp.toPx()

    drawCircle(IllHighlight.copy(0.45f), headR, Offset(px, headCy))
    drawCircle(IllSecondary.copy(0.40f), headR, Offset(px, headCy), style = Stroke(1.8.dp.toPx()))
    // Hair
    val hair = Path().apply {
        moveTo(px - 15.dp.toPx(), headCy - 6.dp.toPx())
        quadraticBezierTo(px - 2.dp.toPx(), headCy - headR - 6.dp.toPx(), px + 16.dp.toPx(), headCy - 9.dp.toPx())
    }
    drawPath(hair, IllSecondary.copy(0.28f), style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
    // Neck
    drawLine(IllSecondary.copy(0.22f), Offset(px, headCy + headR), Offset(px, headCy + headR + 10.dp.toPx()), 1.8.dp.toPx())
    // Torso (relaxed lean)
    val torso = Path().apply {
        moveTo(px - 30.dp.toPx(), seatY - 2.dp.toPx())
        quadraticBezierTo(px - 24.dp.toPx(), headCy + headR + 16.dp.toPx(), px, headCy + headR + 12.dp.toPx())
        quadraticBezierTo(px + 24.dp.toPx(), headCy + headR + 16.dp.toPx(), px + 30.dp.toPx(), seatY - 2.dp.toPx())
    }
    drawPath(torso, IllHighlight.copy(0.28f))
    drawPath(torso, IllSecondary.copy(0.28f), style = Stroke(1.8.dp.toPx()))

    // ── Phone in hand (held up, reading) ──
    val phoneX = px + 18.dp.toPx()
    val phoneY = headCy + 20.dp.toPx()
    val phoneW = 16.dp.toPx()
    val phoneH = 26.dp.toPx()
    drawRoundRect(IllHighlight.copy(0.55f), Offset(phoneX, phoneY), Size(phoneW, phoneH), CornerRadius(3.dp.toPx()))
    drawRoundRect(IllSecondary.copy(0.35f), Offset(phoneX, phoneY), Size(phoneW, phoneH), CornerRadius(3.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Arm holding phone
    val armP = Path().apply {
        moveTo(px + 24.dp.toPx(), seatY - 10.dp.toPx())
        quadraticBezierTo(px + 28.dp.toPx(), phoneY + phoneH, phoneX + phoneW / 2, phoneY + phoneH)
    }
    drawPath(armP, IllSecondary.copy(0.25f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // ── Notification dot on phone ──
    drawCircle(IllAccent.copy(0.55f), 3.dp.toPx(), Offset(phoneX + phoneW - 2.dp.toPx(), phoneY + 3.dp.toPx()))

    // ── Achievement badge (floating near top-right) ──
    val badgeCx = w * 0.72f
    val badgeCy = h * 0.28f
    val badgeR = 16.dp.toPx()
    drawCircle(IllHighlight.copy(0.40f), badgeR, Offset(badgeCx, badgeCy))
    drawCircle(IllNeutral.copy(0.35f), badgeR, Offset(badgeCx, badgeCy), style = Stroke(1.5.dp.toPx()))
    // Star inside badge
    val starPath = createStarPath(badgeCx, badgeCy, 6.dp.toPx(), 10.dp.toPx(), 5)
    drawPath(starPath, IllSecondary.copy(0.30f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // ── Progress bar (near badge) ──
    val barX = w * 0.62f
    val barY = h * 0.40f
    val barW = w * 0.22f
    drawRoundRect(IllHighlight.copy(0.30f), Offset(barX, barY), Size(barW, 4.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(IllSecondary.copy(0.35f), Offset(barX, barY), Size(barW * 0.65f, 4.dp.toPx()), CornerRadius(2.dp.toPx()))

    // ── Small bell icon (top-left ambient) ──
    val bellCx = w * 0.28f
    val bellCy = h * 0.35f
    // Bell body
    val bellPath = Path().apply {
        moveTo(bellCx - 8.dp.toPx(), bellCy + 4.dp.toPx())
        quadraticBezierTo(bellCx - 8.dp.toPx(), bellCy - 8.dp.toPx(), bellCx, bellCy - 10.dp.toPx())
        quadraticBezierTo(bellCx + 8.dp.toPx(), bellCy - 8.dp.toPx(), bellCx + 8.dp.toPx(), bellCy + 4.dp.toPx())
        lineTo(bellCx - 8.dp.toPx(), bellCy + 4.dp.toPx())
    }
    drawPath(bellPath, IllHighlight.copy(0.35f))
    drawPath(bellPath, IllNeutral.copy(0.30f), style = Stroke(1.5.dp.toPx()))
    // Bell clapper
    drawCircle(IllNeutral.copy(0.25f), 2.dp.toPx(), Offset(bellCx, bellCy + 7.dp.toPx()))
}

// ─── Screen 3: Split Expenses ────────────────────────────────────────────────

private fun DrawScope.drawSplitScene() {
    val w = size.width
    val h = size.height

    // ── Ambient shapes ──
    drawCircle(IllHighlight.copy(alpha = 0.18f), w * 0.30f, Offset(w * 0.50f, h * 0.20f))
    drawCircle(IllHighlight.copy(alpha = 0.12f), w * 0.15f, Offset(w * 0.12f, h * 0.55f))

    // ── Table (rounded, wide) ──
    val tableY = h * 0.50f
    val tableH = 40.dp.toPx()
    val tableW = w * 0.74f
    val tableL = (w - tableW) / 2
    drawRoundRect(IllHighlight.copy(0.30f), Offset(tableL, tableY), Size(tableW, tableH), CornerRadius(20.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.22f), Offset(tableL, tableY), Size(tableW, tableH), CornerRadius(20.dp.toPx()), style = Stroke(1.5.dp.toPx()))

    // ── Table items: plates, glasses, receipt ──
    val plateR = 9.dp.toPx()
    val plates = listOf(
        Offset(w * 0.32f, tableY + tableH * 0.35f),
        Offset(w * 0.50f, tableY + tableH * 0.55f),
        Offset(w * 0.68f, tableY + tableH * 0.35f)
    )
    plates.forEach { p ->
        drawCircle(IllHighlight.copy(0.40f), plateR, p)
        drawCircle(IllNeutral.copy(0.20f), plateR, p, style = Stroke(1.dp.toPx()))
        drawCircle(IllNeutral.copy(0.12f), plateR * 0.5f, p, style = Stroke(0.5.dp.toPx()))
    }
    // Glasses
    drawCircle(IllNeutral.copy(0.18f), 3.5.dp.toPx(), Offset(w * 0.38f, tableY + 6.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(IllNeutral.copy(0.18f), 3.5.dp.toPx(), Offset(w * 0.62f, tableY + 6.dp.toPx()), style = Stroke(1.dp.toPx()))
    // Receipt in center
    drawRoundRect(IllHighlight.copy(0.45f), Offset(w * 0.47f, tableY + 8.dp.toPx()), Size(10.dp.toPx(), 14.dp.toPx()), CornerRadius(1.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.22f), Offset(w * 0.47f, tableY + 8.dp.toPx()), Size(10.dp.toPx(), 14.dp.toPx()), CornerRadius(1.dp.toPx()), style = Stroke(1.dp.toPx()))

    // ── Person 1 (left) ──
    drawSeatedPerson(w * 0.28f, tableY - 10.dp.toPx(), tiltX = 2.dp.toPx(), scaleF = 1f)
    // ── Person 2 (center-right) ──
    drawSeatedPerson(w * 0.62f, tableY - 14.dp.toPx(), tiltX = -1.dp.toPx(), scaleF = 1.05f)
    // ── Person 3 (across, top) ──
    drawPersonAcross(w * 0.48f, tableY - 4.dp.toPx())

    // ── Phones near each person ──
    drawRoundRect(IllHighlight.copy(0.35f), Offset(w * 0.18f, tableY + tableH + 6.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.18f), Offset(w * 0.18f, tableY + tableH + 6.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))

    drawRoundRect(IllHighlight.copy(0.35f), Offset(w * 0.74f, tableY + tableH + 8.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(IllNeutral.copy(0.18f), Offset(w * 0.74f, tableY + tableH + 8.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
}

// ─── Screen 4: Security & Encryption ─────────────────────────────────────────

private fun DrawScope.drawSecurityScene() {
    val w = size.width
    val h = size.height

    // ── Ambient shapes ──
    drawCircle(IllHighlight.copy(alpha = 0.15f), w * 0.35f, Offset(w * 0.50f, h * 0.35f))
    drawCircle(IllHighlight.copy(alpha = 0.10f), w * 0.20f, Offset(w * 0.82f, h * 0.60f))

    val cx = w * 0.50f
    val shieldCy = h * 0.38f

    // ── Shield ──
    val shieldW = 60.dp.toPx()
    val shieldH = 72.dp.toPx()
    val shieldPath = Path().apply {
        moveTo(cx, shieldCy - shieldH / 2)
        quadraticBezierTo(cx + shieldW / 2 + 4.dp.toPx(), shieldCy - shieldH / 2 + 6.dp.toPx(), cx + shieldW / 2, shieldCy)
        quadraticBezierTo(cx + shieldW / 2 - 4.dp.toPx(), shieldCy + shieldH / 2 - 6.dp.toPx(), cx, shieldCy + shieldH / 2)
        quadraticBezierTo(cx - shieldW / 2 + 4.dp.toPx(), shieldCy + shieldH / 2 - 6.dp.toPx(), cx - shieldW / 2, shieldCy)
        quadraticBezierTo(cx - shieldW / 2 - 4.dp.toPx(), shieldCy - shieldH / 2 + 6.dp.toPx(), cx, shieldCy - shieldH / 2)
        close()
    }
    drawPath(shieldPath, IllHighlight.copy(0.30f))
    drawPath(shieldPath, IllSecondary.copy(0.35f), style = Stroke(2.dp.toPx()))

    // ── Lock inside shield ──
    val lockCy = shieldCy + 4.dp.toPx()
    val lockW = 16.dp.toPx()
    val lockH = 14.dp.toPx()
    drawRoundRect(IllSecondary.copy(0.30f), Offset(cx - lockW / 2, lockCy), Size(lockW, lockH), CornerRadius(3.dp.toPx()))
    drawRoundRect(IllSecondary.copy(0.40f), Offset(cx - lockW / 2, lockCy), Size(lockW, lockH), CornerRadius(3.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Lock shackle
    val shacklePath = Path().apply {
        moveTo(cx - 6.dp.toPx(), lockCy)
        quadraticBezierTo(cx - 6.dp.toPx(), lockCy - 12.dp.toPx(), cx, lockCy - 12.dp.toPx())
        quadraticBezierTo(cx + 6.dp.toPx(), lockCy - 12.dp.toPx(), cx + 6.dp.toPx(), lockCy)
    }
    drawPath(shacklePath, IllSecondary.copy(0.40f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    // Keyhole
    drawCircle(IllHighlight.copy(0.50f), 2.5.dp.toPx(), Offset(cx, lockCy + lockH * 0.35f))

    // ── Person standing beside shield (left) ──
    val personX = w * 0.26f
    val personHeadY = h * 0.30f
    val pHeadR = 16.dp.toPx()
    drawCircle(IllHighlight.copy(0.42f), pHeadR, Offset(personX, personHeadY))
    drawCircle(IllSecondary.copy(0.35f), pHeadR, Offset(personX, personHeadY), style = Stroke(1.5.dp.toPx()))
    // Body
    drawLine(IllSecondary.copy(0.22f), Offset(personX, personHeadY + pHeadR), Offset(personX, personHeadY + pHeadR + 10.dp.toPx()), 1.5.dp.toPx())
    val bodyP = Path().apply {
        moveTo(personX - 22.dp.toPx(), h * 0.58f)
        quadraticBezierTo(personX - 18.dp.toPx(), personHeadY + pHeadR + 18.dp.toPx(), personX, personHeadY + pHeadR + 12.dp.toPx())
        quadraticBezierTo(personX + 18.dp.toPx(), personHeadY + pHeadR + 18.dp.toPx(), personX + 22.dp.toPx(), h * 0.58f)
    }
    drawPath(bodyP, IllHighlight.copy(0.25f))
    drawPath(bodyP, IllSecondary.copy(0.25f), style = Stroke(1.5.dp.toPx()))
    // Arm resting confidently
    drawLine(IllSecondary.copy(0.20f), Offset(personX + 16.dp.toPx(), h * 0.42f), Offset(personX + 22.dp.toPx(), h * 0.50f), 2.dp.toPx(), cap = StrokeCap.Round)

    // ── Cloud with lock (top-right) ──
    val cloudCx = w * 0.76f
    val cloudCy = h * 0.22f
    val cloudPath = Path().apply {
        moveTo(cloudCx - 20.dp.toPx(), cloudCy + 6.dp.toPx())
        quadraticBezierTo(cloudCx - 24.dp.toPx(), cloudCy - 4.dp.toPx(), cloudCx - 12.dp.toPx(), cloudCy - 8.dp.toPx())
        quadraticBezierTo(cloudCx - 6.dp.toPx(), cloudCy - 16.dp.toPx(), cloudCx + 4.dp.toPx(), cloudCy - 10.dp.toPx())
        quadraticBezierTo(cloudCx + 16.dp.toPx(), cloudCy - 14.dp.toPx(), cloudCx + 20.dp.toPx(), cloudCy - 4.dp.toPx())
        quadraticBezierTo(cloudCx + 24.dp.toPx(), cloudCy + 2.dp.toPx(), cloudCx + 18.dp.toPx(), cloudCy + 6.dp.toPx())
        close()
    }
    drawPath(cloudPath, IllHighlight.copy(0.35f))
    drawPath(cloudPath, IllNeutral.copy(0.28f), style = Stroke(1.5.dp.toPx()))
    // Tiny lock on cloud
    drawRoundRect(IllSecondary.copy(0.30f), Offset(cloudCx - 4.dp.toPx(), cloudCy - 2.dp.toPx()), Size(8.dp.toPx(), 7.dp.toPx()), CornerRadius(1.5.dp.toPx()))

    // ── Fingerprint suggestion (bottom-right) ──
    val fpCx = w * 0.72f
    val fpCy = h * 0.58f
    for (i in 1..3) {
        val r = (8 + i * 6).dp.toPx()
        drawArc(IllNeutral.copy(0.12f + i * 0.04f), -140f, 100f, false, Offset(fpCx - r, fpCy - r), Size(r * 2, r * 2), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// Helper drawing functions
// ═════════════════════════════════════════════════════════════════════════════

private fun DrawScope.drawSeatedPerson(cx: Float, shoulderY: Float, tiltX: Float, scaleF: Float) {
    val headR = 15.dp.toPx() * scaleF
    val headCy = shoulderY - 28.dp.toPx() * scaleF

    drawCircle(IllHighlight.copy(0.42f), headR, Offset(cx + tiltX, headCy))
    drawCircle(IllSecondary.copy(0.35f), headR, Offset(cx + tiltX, headCy), style = Stroke(1.5.dp.toPx()))
    // Neck
    drawLine(IllSecondary.copy(0.18f), Offset(cx, headCy + headR), Offset(cx, shoulderY - 12.dp.toPx() * scaleF), 1.5.dp.toPx())
    // Shoulders
    val sW = 24.dp.toPx() * scaleF
    val sp = Path().apply {
        moveTo(cx - sW, shoulderY + 18.dp.toPx() * scaleF)
        quadraticBezierTo(cx - sW + 6.dp.toPx(), shoulderY, cx, shoulderY - 6.dp.toPx() * scaleF)
        quadraticBezierTo(cx + sW - 6.dp.toPx(), shoulderY, cx + sW, shoulderY + 18.dp.toPx() * scaleF)
    }
    drawPath(sp, IllHighlight.copy(0.25f))
    drawPath(sp, IllSecondary.copy(0.22f), style = Stroke(1.5.dp.toPx()))
}

private fun DrawScope.drawPersonAcross(cx: Float, baseY: Float) {
    val headR = 15.dp.toPx()
    val headCy = baseY - 50.dp.toPx()

    drawCircle(IllHighlight.copy(0.42f), headR, Offset(cx, headCy))
    drawCircle(IllSecondary.copy(0.35f), headR, Offset(cx, headCy), style = Stroke(1.5.dp.toPx()))
    drawLine(IllSecondary.copy(0.18f), Offset(cx, headCy + headR), Offset(cx, baseY - 34.dp.toPx()), 1.5.dp.toPx())
    val sW = 26.dp.toPx()
    val sp = Path().apply {
        moveTo(cx - sW, baseY - 14.dp.toPx())
        quadraticBezierTo(cx - sW + 8.dp.toPx(), baseY - 26.dp.toPx(), cx, baseY - 30.dp.toPx())
        quadraticBezierTo(cx + sW - 8.dp.toPx(), baseY - 26.dp.toPx(), cx + sW, baseY - 14.dp.toPx())
    }
    drawPath(sp, IllHighlight.copy(0.25f))
    drawPath(sp, IllSecondary.copy(0.22f), style = Stroke(1.5.dp.toPx()))
}

private fun createStarPath(cx: Float, cy: Float, innerR: Float, outerR: Float, points: Int): Path {
    val path = Path()
    val angle = Math.PI / points
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val a = i * angle - Math.PI / 2
        val x = cx + (r * kotlin.math.cos(a)).toFloat()
        val y = cy + (r * kotlin.math.sin(a)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
