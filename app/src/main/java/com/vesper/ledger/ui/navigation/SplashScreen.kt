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
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
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
    // Logo tint: white on dark container, dark slate on light container
    val logoTint = if (isDark) Color.White else Color(0xFF0F172A)

    // Match system status bar & navigation bar to screen background
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
        // 1. Initial short pause (screen opens with no logo visible — 100ms)
        delay(100)

        // 2. Smoothly fade in & scale SIMULTANEOUSLY from small to final size (600ms)
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        // 3. Wait for animation to complete + brief hold (total ~1s splash)
        delay(900)

        // 4. Navigate seamlessly — logo stays visible, text fades in on WelcomeScreen
        val isAuthenticated = sharedPrefs.getBoolean("isAuthenticated", false)
        val destination = if (!isAuthenticated) Screen.AuthWelcome.route else "main_screen"
        onNavigateNext(destination)
    }

    Scaffold(
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exact identical top spacer (1.8f weight) matching WelcomeScreen vertical position
            Spacer(modifier = Modifier.weight(1.8f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Logo + Container fade/scale animation (ONLY this element animates — not the whole screen)
                Box(
                    modifier = Modifier
                        .scale(scale.value)
                        .alpha(alpha.value)
                        .size(108.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(logoBoxBg)
                        .border(
                            BorderStroke(
                                width = 1.5.dp,
                                brush = logoBorderBrush
                            ),
                            shape = RoundedCornerShape(26.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_vesper_vector_logo),
                        contentDescription = "Vesper Logo",
                        tint = logoTint,
                        modifier = Modifier.size(102.5.dp)
                    )
                }

                // Invisible placeholder matching WelcomeScreen text block height
                Spacer(modifier = Modifier.height(121.dp))
            }

            Spacer(modifier = Modifier.weight(1.2f))

            // Invisible placeholder matching WelcomeScreen bottom CTA height
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
