package com.vesper.ledger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1E1E1E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF1E1E1E),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF0A0A0A),
    onSurfaceVariant = Color(0xFF8A8A8A),
    outline = Color(0xFF1E1E1E),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFFE5E5E5),
    onSecondary = Color(0xFF111111),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF7F7F7),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE5E5E5),
    error = Color(0xFFEF4444),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun VesperLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: String = "rose",
    content: @Composable () -> Unit
) {
    val primaryColor = when (accentColor) {
        "emerald" -> Color(0xFF10B981)
        "blue" -> Color(0xFF3B82F6)
        "purple" -> Color(0xFF8B5CF6)
        "orange" -> Color(0xFFF97316)
        else -> Color(0xFFF43F5E) // rose
    }

    val onPrimaryColor = if (darkTheme) Color(0xFF09090B) else Color(0xFFFAFAFA)

    val baseColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseColorScheme.copy(
        primary = primaryColor,
        onPrimary = onPrimaryColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.let {
                var ctx = it
                while (ctx is android.content.ContextWrapper) {
                    if (ctx is Activity) return@let ctx
                    ctx = ctx.baseContext
                }
                null
            } as? Activity
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                
                val windowInsetsController = WindowCompat.getInsetsController(window, view)
                windowInsetsController.isAppearanceLightStatusBars = !darkTheme
                windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
