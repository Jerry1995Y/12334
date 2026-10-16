package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun YirenjuTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "light" -> false
        "system" -> isSystemDark
        else -> true // default to OLED dark mode
    }

    val palette = if (isDark) DarkPalette else LightPalette

    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = palette.accent,
            secondary = palette.accent2,
            background = palette.bg,
            surface = palette.surface,
            surfaceVariant = palette.surface2,
            outline = palette.border,
            onPrimary = palette.text,
            onBackground = palette.text,
            onSurface = palette.text,
            onSurfaceVariant = palette.muted
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            secondary = palette.accent2,
            background = palette.bg,
            surface = palette.surface,
            surfaceVariant = palette.surface2,
            outline = palette.border,
            onPrimary = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
            onSurfaceVariant = palette.muted
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In light mode, status/nav icons should be dark so they are clearly legible
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalYirenjuColors provides palette) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
