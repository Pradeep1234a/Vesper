package com.vesper.ledger.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isFirstLaunch: Boolean,
    onNavigateNext: (String) -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Animate scale and fade in
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        // Keep splash showing for another 1 second
        delay(1000)
        
        // Navigate to the next screen based on setup preference
        if (isFirstLaunch) {
            onNavigateNext("onboarding")
        } else {
            onNavigateNext("main_screen")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // slate-900
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // High fidelity custom Canvas drawing of the Vesper Ledger scale logo
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
            ) {
                val width = size.width
                val height = size.height

                // Slate accent background glow circle
                drawCircle(
                    color = Color(0x1F10B981), // Emerald translucent accent
                    radius = width / 2f
                )

                // 1. Balance post and base
                // Vertical support post: center to bottom base
                drawLine(
                    color = Color.White,
                    start = Offset(width / 2f, height * 0.65f),
                    end = Offset(width / 2f, height * 0.85f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Bottom horizontal base bar
                drawLine(
                    color = Color.White,
                    start = Offset(width * 0.35f, height * 0.85f),
                    end = Offset(width * 0.65f, height * 0.85f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. V-shaped balance beam
                drawLine(
                    color = Color.White,
                    start = Offset(width * 0.25f, height * 0.35f),
                    end = Offset(width / 2f, height * 0.65f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(width / 2f, height * 0.65f),
                    end = Offset(width * 0.75f, height * 0.35f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 3. Emerald Pivot indicator (wealth & balance accent)
                drawCircle(
                    color = Color(0xFF10B981), // Emerald green
                    radius = 6.dp.toPx(),
                    center = Offset(width / 2f, height * 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VESPER",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LEDGER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8), // slate-400
                letterSpacing = 8.sp
            )
        }
    }
}
