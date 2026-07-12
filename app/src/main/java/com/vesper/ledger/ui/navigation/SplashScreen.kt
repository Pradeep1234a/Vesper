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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isFirstLaunch: Boolean,
    isSessionActive: Boolean,
    onNavigateNext: (String) -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1000)
        
        if (isFirstLaunch) {
            onNavigateNext("onboarding")
        } else if (isSessionActive) {
            onNavigateNext("main_screen")
        } else {
            onNavigateNext("auth")
        }
    }

    val onBgColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Canvas(
                modifier = Modifier
                    .size(120.dp)
                    .padding(16.dp)
            ) {
                val width = size.width
                val height = size.height

                // Monochromatic background circle based on theme
                drawCircle(
                    color = onBgColor.copy(alpha = 0.06f),
                    radius = width / 2f
                )

                // 1. Balance post and base
                drawLine(
                    color = onBgColor,
                    start = Offset(width / 2f, height * 0.65f),
                    end = Offset(width / 2f, height * 0.85f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = onBgColor,
                    start = Offset(width * 0.35f, height * 0.85f),
                    end = Offset(width * 0.65f, height * 0.85f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 2. V-shaped balance beam
                drawLine(
                    color = onBgColor,
                    start = Offset(width * 0.25f, height * 0.35f),
                    end = Offset(width / 2f, height * 0.65f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = onBgColor,
                    start = Offset(width / 2f, height * 0.65f),
                    end = Offset(width * 0.75f, height * 0.35f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 3. Monochromatic Pivot indicator
                drawCircle(
                    color = onBgColor,
                    radius = 6.dp.toPx(),
                    center = Offset(width / 2f, height * 0.35f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "VESPER",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LEDGER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 8.sp
            )
        }
    }
}
