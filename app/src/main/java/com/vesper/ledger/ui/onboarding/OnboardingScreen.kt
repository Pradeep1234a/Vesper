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

data class OnboardingPage(
    val title: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    var pageIndex by remember { mutableStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Spending becomes\npart of living",
            description = "Your purchases organize themselves quietly. No scanning, no manual entry. Just life."
        ),
        OnboardingPage(
            title = "Clarity replaces\nfinancial stress",
            description = "Understanding where your money goes happens naturally, during a calm moment at your desk."
        ),
        OnboardingPage(
            title = "Splitting feels\nlike friendship",
            description = "Sharing expenses with people you love should never feel like a transaction."
        )
    )

    val transitionEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    val bgColor = MaterialTheme.colorScheme.background
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 1. Full-screen illustration with cinematic scene transition
        AnimatedContent(
            targetState = pageIndex,
            transitionSpec = {
                (slideInHorizontally(
                    initialOffsetX = { it / 5 },
                    animationSpec = tween(450, easing = transitionEasing)
                ) + fadeIn(tween(350))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { -it / 5 },
                    animationSpec = tween(450, easing = transitionEasing)
                ) + fadeOut(tween(250)))
            },
            label = "scene"
        ) { targetPage ->
            SceneIllustration(pageIndex = targetPage)
        }

        // 2. Bottom gradient scrim for text readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.52f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            bgColor.copy(alpha = 0f),
                            bgColor.copy(alpha = 0.6f),
                            bgColor,
                            bgColor
                        )
                    )
                )
        )

        // 3. Skip — top right, fixed position
        if (pageIndex < 2) {
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

        // 4. Bottom content — text, pagination, CTA (fixed vertical position)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text crossfade (fixed-height container — no layout shift)
            Crossfade(
                targetState = pages[pageIndex],
                animationSpec = tween(350),
                label = "text"
            ) { page ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Text(
                        text = page.title,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = onBgColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = onSurfaceVar.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pagination — capsule expands for active
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val active = i == pageIndex
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

            Spacer(modifier = Modifier.height(28.dp))

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
                    if (pageIndex < 2) pageIndex++ else onFinish()
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
                    text = if (pageIndex == 2) "Get Started" else "Continue",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Illustrations — editorial scenes, not feature diagrams
// ---------------------------------------------------------------------------

@Composable
fun SceneIllustration(pageIndex: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "env")

    // Steam drift for Scene 1
    val steamPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "steam"
    )

    // Lamp glow pulse for Scene 2
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val fg = MaterialTheme.colorScheme.onBackground
    val sv = MaterialTheme.colorScheme.onSurfaceVariant
    val bg = MaterialTheme.colorScheme.background

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (pageIndex) {
            0 -> drawCafeScene(fg, sv, bg, steamPhase)
            1 -> drawDeskScene(fg, sv, bg, glowAlpha)
            2 -> drawDinnerScene(fg, sv, bg)
        }
    }
}

// ---- Scene 1: Café — after purchasing coffee ----

private fun DrawScope.drawCafeScene(
    fg: Color, sv: Color, bg: Color, steamPhase: Float
) {
    val w = size.width
    val h = size.height
    val tableY = h * 0.48f

    // --- Window (upper-left, partially cropped at top) ---
    val winL = w * 0.06f
    val winT = h * 0.04f
    val winW = w * 0.40f
    val winH = h * 0.38f
    drawRect(sv.copy(alpha = 0.06f), Offset(winL, winT), Size(winW, winH))
    drawRect(sv.copy(alpha = 0.15f), Offset(winL, winT), Size(winW, winH), style = Stroke(1.5.dp.toPx()))
    // Panes
    drawLine(sv.copy(0.10f), Offset(winL + winW / 2, winT), Offset(winL + winW / 2, winT + winH), 1.dp.toPx())
    drawLine(sv.copy(0.10f), Offset(winL, winT + winH * 0.45f), Offset(winL + winW, winT + winH * 0.45f), 1.dp.toPx())

    // --- Table surface ---
    drawLine(sv.copy(0.25f), Offset(0f, tableY), Offset(w, tableY), 2.dp.toPx())
    // Table fill below the line
    drawRect(sv.copy(alpha = 0.03f), Offset(0f, tableY), Size(w, h * 0.08f))

    // --- Person sitting, right of center, looking away ---
    val px = w * 0.66f
    val headCy = tableY - 85.dp.toPx()
    val headR = 20.dp.toPx()

    // Head (slightly tilted — offset center)
    drawCircle(sv.copy(0.10f), headR, Offset(px + 2.dp.toPx(), headCy))
    drawCircle(sv.copy(0.30f), headR, Offset(px + 2.dp.toPx(), headCy), style = Stroke(1.5.dp.toPx()))

    // Hair suggestion
    val hairPath = Path().apply {
        moveTo(px - 14.dp.toPx(), headCy - 12.dp.toPx())
        quadraticBezierTo(px + 2.dp.toPx(), headCy - headR - 6.dp.toPx(), px + 18.dp.toPx(), headCy - 10.dp.toPx())
    }
    drawPath(hairPath, sv.copy(0.20f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // Neck
    drawLine(sv.copy(0.20f), Offset(px + 1.dp.toPx(), headCy + headR), Offset(px, headCy + headR + 10.dp.toPx()), 1.5.dp.toPx())

    // Torso / shoulders
    val torsoPath = Path().apply {
        moveTo(px - 36.dp.toPx(), tableY - 6.dp.toPx())
        quadraticBezierTo(px - 30.dp.toPx(), headCy + headR + 18.dp.toPx(), px, headCy + headR + 12.dp.toPx())
        quadraticBezierTo(px + 30.dp.toPx(), headCy + headR + 18.dp.toPx(), px + 38.dp.toPx(), tableY - 6.dp.toPx())
    }
    drawPath(torsoPath, sv.copy(0.06f))
    drawPath(torsoPath, sv.copy(0.25f), style = Stroke(1.5.dp.toPx()))

    // Arm resting on table toward objects
    val armPath = Path().apply {
        moveTo(px - 30.dp.toPx(), tableY - 10.dp.toPx())
        quadraticBezierTo(px - 50.dp.toPx(), tableY - 6.dp.toPx(), px - 65.dp.toPx(), tableY - 3.dp.toPx())
    }
    drawPath(armPath, sv.copy(0.22f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // --- Coffee cup ---
    val cupCx = w * 0.38f
    val cupW = 14.dp.toPx()
    val cupH = 16.dp.toPx()
    val cupTop = tableY - cupH
    drawRoundRect(sv.copy(0.08f), Offset(cupCx - cupW / 2, cupTop), Size(cupW, cupH), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.28f), Offset(cupCx - cupW / 2, cupTop), Size(cupW, cupH), CornerRadius(2.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Handle (small arc on right side)
    val handlePath = Path().apply {
        moveTo(cupCx + cupW / 2, cupTop + 3.dp.toPx())
        quadraticBezierTo(cupCx + cupW / 2 + 7.dp.toPx(), cupTop + cupH / 2, cupCx + cupW / 2, cupTop + cupH - 3.dp.toPx())
    }
    drawPath(handlePath, sv.copy(0.25f), style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round))

    // Steam wisps — life in the scene
    for (i in 0..2) {
        val phase = (steamPhase + i * 0.33f) % 1f
        val alpha = (1f - phase) * 0.12f
        val yOff = cupTop - 6.dp.toPx() - phase * 28.dp.toPx()
        val xWobble = kotlin.math.sin((phase + i) * 3.14f) * 3.dp.toPx()
        if (alpha > 0.01f) {
            drawCircle(sv.copy(alpha), 2.dp.toPx(), Offset(cupCx + xWobble + (i - 1) * 3.dp.toPx(), yOff))
        }
    }

    // --- Receipt (folded, on table) ---
    val rcptX = w * 0.48f
    val rcptW = 12.dp.toPx()
    val rcptH = 18.dp.toPx()
    drawRoundRect(sv.copy(0.07f), Offset(rcptX, tableY - rcptH + 2.dp.toPx()), Size(rcptW, rcptH), CornerRadius(1.dp.toPx()))
    drawRoundRect(sv.copy(0.18f), Offset(rcptX, tableY - rcptH + 2.dp.toPx()), Size(rcptW, rcptH), CornerRadius(1.dp.toPx()), style = Stroke(1.dp.toPx()))
    // Tiny text lines on receipt
    for (i in 0..3) {
        val ly = tableY - rcptH + 5.dp.toPx() + i * 3.5.dp.toPx()
        val lw = if (i == 0) 6.dp.toPx() else if (i == 3) 4.dp.toPx() else 8.dp.toPx()
        drawLine(sv.copy(0.12f), Offset(rcptX + 2.dp.toPx(), ly), Offset(rcptX + 2.dp.toPx() + lw, ly), 1.dp.toPx())
    }

    // --- Phone lying flat on table ---
    val phX = w * 0.28f
    val phW = 18.dp.toPx()
    val phH = 10.dp.toPx()
    drawRoundRect(sv.copy(0.06f), Offset(phX, tableY - phH + 1.dp.toPx()), Size(phW, phH), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.20f), Offset(phX, tableY - phH + 1.dp.toPx()), Size(phW, phH), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
    // Screen glint
    drawLine(sv.copy(0.08f), Offset(phX + 3.dp.toPx(), tableY - phH + 3.dp.toPx()), Offset(phX + phW - 3.dp.toPx(), tableY - phH + 3.dp.toPx()), 1.dp.toPx())

    // --- Chair leg suggestion (right side, partially cropped) ---
    drawLine(sv.copy(0.08f), Offset(w * 0.82f, tableY + 4.dp.toPx()), Offset(w * 0.85f, h * 0.62f), 2.dp.toPx())
}

// ---- Scene 2: Evening desk — reflecting on finances ----

private fun DrawScope.drawDeskScene(
    fg: Color, sv: Color, bg: Color, glowAlpha: Float
) {
    val w = size.width
    val h = size.height
    val deskY = h * 0.48f

    // --- Desk surface ---
    drawLine(sv.copy(0.25f), Offset(0f, deskY), Offset(w, deskY), 2.dp.toPx())
    drawRect(sv.copy(alpha = 0.03f), Offset(0f, deskY), Size(w, h * 0.08f))

    // --- Desk lamp (right side, partially cropped) ---
    val lampBaseX = w * 0.78f
    // Base on desk
    drawRoundRect(sv.copy(0.15f), Offset(lampBaseX - 10.dp.toPx(), deskY - 6.dp.toPx()), Size(20.dp.toPx(), 6.dp.toPx()), CornerRadius(2.dp.toPx()))
    // Vertical stand
    drawLine(sv.copy(0.22f), Offset(lampBaseX, deskY - 6.dp.toPx()), Offset(lampBaseX, deskY - 50.dp.toPx()), 2.dp.toPx())
    // Angled arm
    drawLine(sv.copy(0.22f), Offset(lampBaseX, deskY - 50.dp.toPx()), Offset(lampBaseX - 28.dp.toPx(), deskY - 70.dp.toPx()), 2.dp.toPx())
    // Shade
    val shadePath = Path().apply {
        moveTo(lampBaseX - 42.dp.toPx(), deskY - 65.dp.toPx())
        lineTo(lampBaseX - 28.dp.toPx(), deskY - 72.dp.toPx())
        lineTo(lampBaseX - 14.dp.toPx(), deskY - 65.dp.toPx())
    }
    drawPath(shadePath, sv.copy(0.20f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    // Warm glow (subtle pulsing circle)
    drawCircle(sv.copy(glowAlpha), 40.dp.toPx(), Offset(lampBaseX - 28.dp.toPx(), deskY - 40.dp.toPx()))

    // --- Person sitting, left of center, reflective pose ---
    val px = w * 0.36f
    val headCy = deskY - 80.dp.toPx()
    val headR = 20.dp.toPx()

    drawCircle(sv.copy(0.10f), headR, Offset(px, headCy))
    drawCircle(sv.copy(0.30f), headR, Offset(px, headCy), style = Stroke(1.5.dp.toPx()))

    // Hair
    val hairP = Path().apply {
        moveTo(px - 16.dp.toPx(), headCy - 8.dp.toPx())
        quadraticBezierTo(px, headCy - headR - 5.dp.toPx(), px + 16.dp.toPx(), headCy - 10.dp.toPx())
    }
    drawPath(hairP, sv.copy(0.18f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // Neck
    drawLine(sv.copy(0.18f), Offset(px, headCy + headR), Offset(px + 1.dp.toPx(), headCy + headR + 10.dp.toPx()), 1.5.dp.toPx())

    // Torso
    val torso = Path().apply {
        moveTo(px - 34.dp.toPx(), deskY - 6.dp.toPx())
        quadraticBezierTo(px - 28.dp.toPx(), headCy + headR + 16.dp.toPx(), px, headCy + headR + 12.dp.toPx())
        quadraticBezierTo(px + 28.dp.toPx(), headCy + headR + 16.dp.toPx(), px + 34.dp.toPx(), deskY - 6.dp.toPx())
    }
    drawPath(torso, sv.copy(0.06f))
    drawPath(torso, sv.copy(0.25f), style = Stroke(1.5.dp.toPx()))

    // Hand near chin (thinking gesture)
    val handPath = Path().apply {
        moveTo(px + 28.dp.toPx(), deskY - 12.dp.toPx())
        quadraticBezierTo(px + 22.dp.toPx(), headCy + headR + 4.dp.toPx(), px + 10.dp.toPx(), headCy + headR - 2.dp.toPx())
    }
    drawPath(handPath, sv.copy(0.20f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

    // --- Open notebook on desk ---
    val nbCx = w * 0.54f
    val nbW = 30.dp.toPx()
    val nbH = 20.dp.toPx()
    // Left page
    drawRoundRect(sv.copy(0.06f), Offset(nbCx - nbW, deskY - nbH + 2.dp.toPx()), Size(nbW, nbH), CornerRadius(1.dp.toPx()))
    drawRoundRect(sv.copy(0.15f), Offset(nbCx - nbW, deskY - nbH + 2.dp.toPx()), Size(nbW, nbH), CornerRadius(1.dp.toPx()), style = Stroke(1.dp.toPx()))
    // Right page
    drawRoundRect(sv.copy(0.06f), Offset(nbCx, deskY - nbH + 2.dp.toPx()), Size(nbW, nbH), CornerRadius(1.dp.toPx()))
    drawRoundRect(sv.copy(0.15f), Offset(nbCx, deskY - nbH + 2.dp.toPx()), Size(nbW, nbH), CornerRadius(1.dp.toPx()), style = Stroke(1.dp.toPx()))
    // Spine
    drawLine(sv.copy(0.20f), Offset(nbCx, deskY - nbH + 2.dp.toPx()), Offset(nbCx, deskY + 2.dp.toPx()), 1.dp.toPx())
    // Text lines on right page
    for (i in 0..3) {
        val ly = deskY - nbH + 6.dp.toPx() + i * 4.dp.toPx()
        drawLine(sv.copy(0.08f), Offset(nbCx + 3.dp.toPx(), ly), Offset(nbCx + nbW - 4.dp.toPx(), ly), 1.dp.toPx())
    }

    // --- Coffee mug ---
    val mugX = w * 0.66f
    val mugW = 12.dp.toPx()
    val mugH = 14.dp.toPx()
    val mugTop = deskY - mugH
    drawRoundRect(sv.copy(0.08f), Offset(mugX, mugTop), Size(mugW, mugH), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.22f), Offset(mugX, mugTop), Size(mugW, mugH), CornerRadius(2.dp.toPx()), style = Stroke(1.5.dp.toPx()))

    // --- Phone flat on desk ---
    val phX = w * 0.46f
    val phW = 16.dp.toPx()
    val phH = 9.dp.toPx()
    drawRoundRect(sv.copy(0.06f), Offset(phX, deskY - phH + 1.dp.toPx()), Size(phW, phH), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.18f), Offset(phX, deskY - phH + 1.dp.toPx()), Size(phW, phH), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
}

// ---- Scene 3: Dinner with friends — splitting the bill ----

private fun DrawScope.drawDinnerScene(
    fg: Color, sv: Color, bg: Color
) {
    val w = size.width
    val h = size.height
    val tableY = h * 0.48f

    // --- Table (rounded, large, slightly overhead perspective) ---
    val tableW = w * 0.72f
    val tableH = 50.dp.toPx()
    val tableLeft = (w - tableW) / 2
    drawRoundRect(sv.copy(0.06f), Offset(tableLeft, tableY - tableH / 2), Size(tableW, tableH), CornerRadius(25.dp.toPx()))
    drawRoundRect(sv.copy(0.18f), Offset(tableLeft, tableY - tableH / 2), Size(tableW, tableH), CornerRadius(25.dp.toPx()), style = Stroke(1.5.dp.toPx()))

    // --- Plates (circles on table) ---
    val plateR = 10.dp.toPx()
    val plate1 = Offset(w * 0.34f, tableY - 4.dp.toPx())
    val plate2 = Offset(w * 0.66f, tableY - 4.dp.toPx())
    val plate3 = Offset(w * 0.50f, tableY + 6.dp.toPx())
    for (plate in listOf(plate1, plate2, plate3)) {
        drawCircle(sv.copy(0.05f), plateR, plate)
        drawCircle(sv.copy(0.15f), plateR, plate, style = Stroke(1.dp.toPx()))
        drawCircle(sv.copy(0.10f), plateR * 0.5f, plate, style = Stroke(0.5.dp.toPx()))
    }

    // --- Glasses ---
    val glassR = 3.5.dp.toPx()
    drawCircle(sv.copy(0.12f), glassR, Offset(w * 0.40f, tableY - 12.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(sv.copy(0.12f), glassR, Offset(w * 0.60f, tableY - 12.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawCircle(sv.copy(0.12f), glassR, Offset(w * 0.52f, tableY + 14.dp.toPx()), style = Stroke(1.dp.toPx()))

    // --- Receipt in center of table ---
    val rcptW = 10.dp.toPx()
    val rcptH = 14.dp.toPx()
    drawRoundRect(sv.copy(0.10f), Offset(w * 0.49f, tableY - rcptH / 2), Size(rcptW, rcptH), CornerRadius(1.dp.toPx()))
    drawRoundRect(sv.copy(0.20f), Offset(w * 0.49f, tableY - rcptH / 2), Size(rcptW, rcptH), CornerRadius(1.dp.toPx()), style = Stroke(1.dp.toPx()))

    // --- Person 1 (bottom-left) ---
    drawPersonSitting(sv, w * 0.28f, tableY + 40.dp.toPx(), facingRight = true)

    // --- Person 2 (bottom-right) ---
    drawPersonSitting(sv, w * 0.72f, tableY + 40.dp.toPx(), facingRight = false)

    // --- Person 3 (across the table, top-center) ---
    drawPersonAcross(sv, w * 0.50f, tableY - 50.dp.toPx())

    // --- Phones resting near each person ---
    // Phone near Person 1
    val ph1X = w * 0.22f
    drawRoundRect(sv.copy(0.06f), Offset(ph1X, tableY + 28.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.15f), Offset(ph1X, tableY + 28.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))

    // Phone near Person 2
    val ph2X = w * 0.76f
    drawRoundRect(sv.copy(0.06f), Offset(ph2X, tableY + 30.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()))
    drawRoundRect(sv.copy(0.15f), Offset(ph2X, tableY + 30.dp.toPx()), Size(10.dp.toPx(), 16.dp.toPx()), CornerRadius(2.dp.toPx()), style = Stroke(1.dp.toPx()))
}

// --- Helper: Draw a person sitting (bottom half of scene, facing table) ---
private fun DrawScope.drawPersonSitting(sv: Color, cx: Float, shoulderY: Float, facingRight: Boolean) {
    val headR = 16.dp.toPx()
    val headCy = shoulderY - 30.dp.toPx()
    val xTilt = if (facingRight) 2.dp.toPx() else -2.dp.toPx()

    // Head
    drawCircle(sv.copy(0.08f), headR, Offset(cx + xTilt, headCy))
    drawCircle(sv.copy(0.25f), headR, Offset(cx + xTilt, headCy), style = Stroke(1.5.dp.toPx()))

    // Neck
    drawLine(sv.copy(0.15f), Offset(cx, headCy + headR), Offset(cx, shoulderY - 14.dp.toPx()), 1.5.dp.toPx())

    // Shoulders
    val sW = 28.dp.toPx()
    val shoulderPath = Path().apply {
        moveTo(cx - sW, shoulderY + 20.dp.toPx())
        quadraticBezierTo(cx - sW + 6.dp.toPx(), shoulderY, cx, shoulderY - 8.dp.toPx())
        quadraticBezierTo(cx + sW - 6.dp.toPx(), shoulderY, cx + sW, shoulderY + 20.dp.toPx())
    }
    drawPath(shoulderPath, sv.copy(0.05f))
    drawPath(shoulderPath, sv.copy(0.20f), style = Stroke(1.5.dp.toPx()))
}

// --- Helper: Draw person across the table (top of scene) ---
private fun DrawScope.drawPersonAcross(sv: Color, cx: Float, baseY: Float) {
    val headR = 16.dp.toPx()
    val headCy = baseY - 28.dp.toPx()

    // Head
    drawCircle(sv.copy(0.08f), headR, Offset(cx, headCy))
    drawCircle(sv.copy(0.25f), headR, Offset(cx, headCy), style = Stroke(1.5.dp.toPx()))

    // Neck
    drawLine(sv.copy(0.15f), Offset(cx, headCy + headR), Offset(cx, baseY - 12.dp.toPx()), 1.5.dp.toPx())

    // Shoulders (seen from front)
    val sW = 30.dp.toPx()
    val shoulderPath = Path().apply {
        moveTo(cx - sW, baseY + 10.dp.toPx())
        quadraticBezierTo(cx - sW + 8.dp.toPx(), baseY - 4.dp.toPx(), cx, baseY - 8.dp.toPx())
        quadraticBezierTo(cx + sW - 8.dp.toPx(), baseY - 4.dp.toPx(), cx + sW, baseY + 10.dp.toPx())
    }
    drawPath(shoulderPath, sv.copy(0.05f))
    drawPath(shoulderPath, sv.copy(0.20f), style = Stroke(1.5.dp.toPx()))
}
