package com.vesper.ledger.ui.navigation

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
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
    // Large Standalone Emblem Logo Scale & Alpha
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }

    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val sharedPrefs = context.getSharedPreferences("vesper_settings", Context.MODE_PRIVATE)

    // Theme-adaptive colors
    val backgroundColor = if (isDark) Color(0xFF09090B) else Color(0xFFF8FAFC)
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
        // 1. Initial short pause (100ms)
        delay(100)

        // 2. Entrance: Large standalone emblem logo scales up & fades in at true center (500ms)
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

        // 3. Hold large standalone logo (500ms)
        delay(500)

        // 4. Exit: Clean fade out & scale transition directly to destination screen (400ms, no container)
        launch {
            logoAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        logoScale.animateTo(
            targetValue = 0.85f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        delay(100)

        // 5. Navigate to Welcome Screen or Main Screen
        val isAuthenticated = sharedPrefs.getBoolean("isAuthenticated", false)
        val destination = if (!isAuthenticated) Screen.AuthWelcome.route else "main_screen"
        onNavigateNext(destination)
    }

    Scaffold(
        containerColor = backgroundColor
    ) { innerPadding ->
        // Dead center screen alignment, pure standalone emblem logo (no container)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_vesper_vector_logo),
                contentDescription = "Vesper Logo",
                tint = logoTint,
                modifier = Modifier
                    .size(240.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )
        }
    }
}
