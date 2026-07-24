package com.vesper.ledger.ui.navigation

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.vesper.ledger.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateNext: (String) -> Unit
) {
    // Big Standalone 220.dp Emblem Logo Scale & Alpha
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    
    // Squercle Box Container Alpha (fades in 0f -> 1f during exit transition)
    val containerAlpha = remember { Animatable(0f) }

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)

    // Theme-adaptive colors (identical to WelcomeScreen)
    val backgroundColor = if (isDark) Color(0xFF09090B) else Color(0xFFF8FAFC)
    val logoBoxBg = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF2A2A30), Color(0xFF141416)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)))
    }
    val logoBorderBrush = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF4A4A52), Color(0xFF222226)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
    }
    val logoTint = if (isDark) Color.White else Color(0xFF0F172A)

    // Dynamic status bar & navigation bar background matching
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity: Activity? = view.context.let {
                var ctx = it
                while (ctx is android.content.ContextWrapper) {
                    if (ctx is Activity) return@let ctx
                    ctx = ctx.baseContext
                }
                null
            }
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = backgroundColor.toArgb()
                window.navigationBarColor = backgroundColor.toArgb()
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !isDark
                windowInsetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    LaunchedEffect(key1 = true) {
        // 1. Initial short pause (screen opens clean with no logo visible — 100ms)
        delay(100)

        // 2. Entrance Phase: Big standalone 220.dp emblem logo fades in & scales up at true center (500ms)
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        // Hold big standalone 220.dp logo centered on screen (400ms)
        delay(400)

        // 3. Exit Phase: 220.dp logo scales down to 102.5.dp size (0.4659f scale)
        //    WHILE squercle box container smoothly fades in around it (500ms)!
        val targetScale = 102.5f / 220.0f // 0.4659f
        launch {
            containerAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
        logoScale.animateTo(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )

        delay(100)

        // 4. Navigate seamlessly to Welcome Screen or Main Screen
        val isAuthenticated = sharedPrefs.getBoolean("isAuthenticated", false)
        val destination = if (!isAuthenticated) Screen.AuthWelcome.route else "main_screen"
        onNavigateNext(destination)
    }

    Scaffold(
        containerColor = backgroundColor
    ) { innerPadding ->
        // Dead center screen alignment without invisible placeholders
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Hero Logo Container Box Tile (108.dp squercle container fades in during transition)
            Box(
                modifier = Modifier.size(108.dp),
                contentAlignment = Alignment.Center
            ) {
                // Squercle Container Background + Border (Fades in from 0f -> 1f during exit transition)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(containerAlpha.value)
                        .clip(RoundedCornerShape(26.dp))
                        .background(logoBoxBg)
                        .border(
                            BorderStroke(
                                width = 1.5.dp,
                                brush = logoBorderBrush
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                )

                // Big Standalone Emblem Logo (Starts 220.dp at 1.0f scale -> morphs down to 102.5.dp)
                Icon(
                    painter = painterResource(id = R.drawable.ic_vesper_vector_logo),
                    contentDescription = "Vesper Logo",
                    tint = logoTint,
                    modifier = Modifier
                        .size(220.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }
        }
    }
}
