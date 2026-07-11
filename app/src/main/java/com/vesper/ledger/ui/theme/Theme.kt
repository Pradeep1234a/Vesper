package com.vesper.ledger.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Slate50,            // White/light primary text/accent in dark mode
    onPrimary = Slate950,
    primaryContainer = Slate900,
    onPrimaryContainer = Slate50,
    secondary = Slate700,
    onSecondary = Slate200,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate950,
    onSurface = Slate50,
    surfaceVariant = Slate900,
    onSurfaceVariant = Slate400,
    outline = Slate800,
    error = ExpenseRedDark,
    onError = Slate950
)

private val LightColorScheme = lightColorScheme(
    primary = Slate900,           // Dark primary text/accent in light mode
    onPrimary = Slate50,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = Slate200,
    onSecondary = Slate800,
    background = Slate50,
    onBackground = Slate950,
    surface = Slate50,
    onSurface = Slate950,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    error = ExpenseRedLight,
    onError = Slate50
)

@Composable
fun VesperLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
