package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class YirenjuColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accent2: Color,
    val danger: Color,
    val step: Color,
    val sleep: Color,
    val heart: Color,
    val dist: Color,
    val kcal: Color,
    val ok: Color,
    val navBg: Color,
    val navPill: Color,
    val isDark: Boolean
)

val DarkPalette = YirenjuColors(
    bg = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    surface2 = Color(0xFF2C2C2E),
    surface3 = Color(0xFF3A3A3C),
    border = Color(0xFF38383A),
    text = Color(0xFFFFFFFF),
    muted = Color(0xFF98989E),
    accent = Color(0xFF0A84FF),
    accent2 = Color(0xFF5E5CE6),
    danger = Color(0xFFFF453A),
    step = Color(0xFF0A84FF),
    sleep = Color(0xFF5E5CE6),
    heart = Color(0xFFFF453A),
    dist = Color(0xFF64D2FF),
    kcal = Color(0xFFFF9F0A),
    ok = Color(0xFF30D158),
    navBg = Color(0xFF111114),
    navPill = Color(0xFF0A2E52),
    isDark = true
)

val LightPalette = YirenjuColors(
    bg = Color(0xFFF2F2F7),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFE5E5EA),
    surface3 = Color(0xFFD1D1D6),
    border = Color(0xFFDCDCE2),
    text = Color(0xFF000000),
    muted = Color(0xFF8E8E93),
    accent = Color(0xFF007AFF),
    accent2 = Color(0xFF5856D6),
    danger = Color(0xFFFF3B30),
    step = Color(0xFF007AFF),
    sleep = Color(0xFF5856D6),
    heart = Color(0xFFFF3B30),
    dist = Color(0xFF007AFF),
    kcal = Color(0xFFFF9500),
    ok = Color(0xFF34C759),
    navBg = Color(0xFFF9F9FB),
    navPill = Color(0xFFD6E8FF),
    isDark = false
)

val LocalYirenjuColors = staticCompositionLocalOf { DarkPalette }

// Global convenience accessor
object YirenjuTheme {
    val colors: YirenjuColors
        @Composable
        get() = LocalYirenjuColors.current
}
