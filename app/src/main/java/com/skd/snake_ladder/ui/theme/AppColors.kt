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

    // Player count chips (unselected state)
    val chipUnselectedBg: Color,
    val chipUnselectedBorder: Color,
    val chipUnselectedText: Color,
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

    cardBorder             = Color(0x22FFFFFF),
    cardBorderActive       = Color(0xFFFFD700),
    cardBorderEliminated   = Color(0x44EF5350),
    cardBorderDisconnected = Color(0x44546E7A),
    divider                = Color(0x20FFFFFF),

    textPrimary      = Color(0xFFECEFF1),
    textSecondary    = Color(0xFF78909C),
    textHint         = Color(0xFF37474F),
    textOnEliminated = Color(0xFF4A4A5A),

    accent             = Color(0xFF4FC3F7),
    timerTrack         = Color(0x20FFFFFF),
    skipDotEmpty       = Color(0x30FFFFFF),
    positionPillBg     = Color(0x14FFFFFF),
    positionPillBorder = Color(0x22FFFFFF),

    chipUnselectedBg     = Color(0x18FFFFFF),
    chipUnselectedBorder = Color(0x2AFFFFFF),
    chipUnselectedText   = Color(0x99FFFFFF),
)

// ── Light theme ────────────────────────────────────────────────────────────────
val LightAppColors = AppColors(
    isDark = false,

    bgGradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f  to Color(0xFFE8F0FE),
            0.50f to Color(0xFFDDE8F8),
            1.0f  to Color(0xFFCFDDF2)
        )
    ),
    cardGradient = Brush.linearGradient(
        listOf(Color(0xFFFFFFFF), Color(0xFFF2F6FF))
    ),
    cardGradientActive = Brush.linearGradient(
        listOf(Color(0xFFE3EFFE), Color(0xFFCEE3FF))
    ),
    cardGradientEliminated = Brush.linearGradient(
        listOf(Color(0xFFFFF3F3), Color(0xFFFFE5E5))
    ),
    cardGradientDisconnected = Brush.linearGradient(
        listOf(Color(0xFFF0F4FA), Color(0xFFE8EEF8))
    ),
    surfaceSheet = Color(0xFFFFFFFF),

    cardBorder             = Color(0xFFB8CBE8),
    cardBorderActive       = Color(0xFFFFAA00),
    cardBorderEliminated   = Color(0xFFFFB3B3),
    cardBorderDisconnected = Color(0xFFB0BEC5),
    divider                = Color(0xFFD0DCF0),

    textPrimary      = Color(0xFF0F1E3D),
    textSecondary    = Color(0xFF3A5474),
    textHint         = Color(0xFF7A93AA),
    textOnEliminated = Color(0xFFB0B8C0),

    accent             = Color(0xFF1356B4),
    timerTrack         = Color(0x28000000),
    skipDotEmpty       = Color(0xFFC0CBDA),
    positionPillBg     = Color(0x221356B4),
    positionPillBorder = Color(0x3C1356B4),

    chipUnselectedBg     = Color(0x14000000),
    chipUnselectedBorder = Color(0x2A000000),
    chipUnselectedText   = Color(0xFF4A6078),
)

// ── CompositionLocal ───────────────────────────────────────────────────────────
val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
