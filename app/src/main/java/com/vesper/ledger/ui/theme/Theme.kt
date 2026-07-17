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
    primary = Slate50,            // Dynamic override
    onPrimary = Slate950,
    primaryContainer = Slate900,
    onPrimaryContainer = Slate50,
    secondary = Slate700,
    onSecondary = Slate200,
    background = VesperDarkBg,
    onBackground = VesperDarkPrimaryText,
    surface = VesperDarkSurface,
    onSurface = VesperDarkPrimaryText,
    surfaceVariant = VesperDarkSurface,
    onSurfaceVariant = VesperDarkSecondaryText,
    outline = VesperDarkOutline,
    outlineVariant = VesperDarkOutline,
    error = ExpenseRedDark,
    onError = Slate950
)

private val LightColorScheme = lightColorScheme(
    primary = Slate900,           // Dynamic override
    onPrimary = Slate50,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = Slate200,
    onSecondary = Slate800,
    background = VesperLightBg,
    onBackground = VesperLightPrimaryText,
    surface = VesperLightSurface,
    onSurface = VesperLightPrimaryText,
    surfaceVariant = VesperLightSurface,
    onSurfaceVariant = VesperLightSecondaryText,
    outline = VesperLightOutline,
    outlineVariant = VesperLightOutline,
    error = ExpenseRedLight,
    onError = Slate50
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
