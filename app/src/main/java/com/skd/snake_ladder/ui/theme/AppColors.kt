package com.skd.snake_ladder.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── App-level semantic colour set ─────────────────────────────────────────────
data class AppColors(
    val isDark: Boolean,

    // Backgrounds
    val bgGradient: Brush,
    val cardGradient: Brush,
    val cardGradientActive: Brush,
    val cardGradientEliminated: Brush,
    val cardGradientDisconnected: Brush,
    val surfaceSheet: Color,

    // Borders
    val cardBorder: Color,
    val cardBorderActive: Color,
    val cardBorderEliminated: Color,
    val cardBorderDisconnected: Color,
    val divider: Color,

    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textOnEliminated: Color,

    // Elements
    val accent: Color,
    val timerTrack: Color,
    val skipDotEmpty: Color,
    val positionPillBg: Color,
    val positionPillBorder: Color,
)

// ── Dark theme ─────────────────────────────────────────────────────────────────
val DarkAppColors = AppColors(
    isDark = true,

    bgGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f  to Color(0xFF070D1A),
            0.45f to Color(0xFF0F1829),
            1.0f  to Color(0xFF0A1020)
        )
    ),
    cardGradient = Brush.linearGradient(
        listOf(Color(0xFF1A2540), Color(0xFF111C33))
    ),
    cardGradientActive = Brush.linearGradient(
        listOf(Color(0xFF1C3764), Color(0xFF102244))
    ),
    cardGradientEliminated = Brush.linearGradient(
        listOf(Color(0xFF1C0E0E), Color(0xFF110A0A))
    ),
    cardGradientDisconnected = Brush.linearGradient(
        listOf(Color(0xFF111820), Color(0xFF0B1018))
    ),
    surfaceSheet = Color(0xFF131F38),

    cardBorder            = Color(0x1AFFFFFF),
    cardBorderActive      = Color(0xFFFFD700),
    cardBorderEliminated  = Color(0x44EF5350),
    cardBorderDisconnected= Color(0x44546E7A),
    divider               = Color(0x20FFFFFF),

    textPrimary       = Color(0xFFECEFF1),
    textSecondary     = Color(0xFF78909C),
    textHint          = Color(0xFF37474F),
    textOnEliminated  = Color(0xFF4A4A5A),

    accent           = Color(0xFF4FC3F7),
    timerTrack       = Color(0x20FFFFFF),
    skipDotEmpty     = Color(0x25FFFFFF),
    positionPillBg   = Color(0x14FFFFFF),
    positionPillBorder = Color(0x1AFFFFFF),
)

// ── Light theme ────────────────────────────────────────────────────────────────
val LightAppColors = AppColors(
    isDark = false,

    bgGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f  to Color(0xFFEEF4FF),
            0.50f to Color(0xFFE6EEFA),
            1.0f  to Color(0xFFDDE8F5)
        )
    ),
    cardGradient = Brush.linearGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFF5F8FF))
    ),
    cardGradientActive = Brush.linearGradient(
        listOf(Color(0xFFEBF3FF), Color(0xFFDDEBFF))
    ),
    cardGradientEliminated = Brush.linearGradient(
        listOf(Color(0xFFFFF3F3), Color(0xFFFFEBEB))
    ),
    cardGradientDisconnected = Brush.linearGradient(
        listOf(Color(0xFFF5F7FA), Color(0xFFEEF0F5))
    ),
    surfaceSheet = Color(0xFFFFFFFF),

    cardBorder            = Color(0xFFDEE8F5),
    cardBorderActive      = Color(0xFFFFB700),
    cardBorderEliminated  = Color(0xFFFFCDD2),
    cardBorderDisconnected= Color(0xFFCFD8DC),
    divider               = Color(0xFFDEE5EF),

    textPrimary      = Color(0xFF1A2340),
    textSecondary    = Color(0xFF4A6078),
    textHint         = Color(0xFF90A4AE),
    textOnEliminated = Color(0xFFBDBDBD),

    accent           = Color(0xFF1565C0),
    timerTrack       = Color(0x22000000),
    skipDotEmpty     = Color(0x22000000),
    positionPillBg   = Color(0x0F1565C0),
    positionPillBorder = Color(0x22000000),
)

// ── CompositionLocal ───────────────────────────────────────────────────────────
val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
