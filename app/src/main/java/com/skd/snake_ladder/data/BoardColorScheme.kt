package com.skd.snake_ladder.data

import androidx.compose.ui.graphics.Color

data class BoardColorScheme(
    val id: Int,
    val name: String,
    val emoji: String,
    val cellLight: Color,
    val cellDark: Color,
    val gridLine: Color,
    val numberBg: Color,
    val numberText: Color,
)

object BoardColorSchemes {
    val schemes: List<BoardColorScheme> = listOf(
        BoardColorScheme(
            id = 0, name = "Classic", emoji = "🏛️",
            cellLight  = Color(0xFFFFF9C4),
            cellDark   = Color(0xFFB3E5FC),
            gridLine   = Color(0xFFBDBDBD),
            numberBg   = Color(0xBBFFFFFF),
            numberText = Color(0xFF111111),
        ),
        BoardColorScheme(
            id = 1, name = "Forest", emoji = "🌿",
            cellLight  = Color(0xFFDCEDC8),
            cellDark   = Color(0xFFA5D6A7),
            gridLine   = Color(0xFF81C784),
            numberBg   = Color(0xCCFFFFFF),
            numberText = Color(0xFF1B5E20),
        ),
        BoardColorScheme(
            id = 2, name = "Sunset", emoji = "🌅",
            cellLight  = Color(0xFFFFCCBC),
            cellDark   = Color(0xFFF48FB1),
            gridLine   = Color(0xFFE57373),
            numberBg   = Color(0xCCFFFFFF),
            numberText = Color(0xFFBF360C),
        ),
        BoardColorScheme(
            id = 3, name = "Ocean", emoji = "🌊",
            cellLight  = Color(0xFFE0F7FA),
            cellDark   = Color(0xFFB2DFDB),
            gridLine   = Color(0xFF4DB6AC),
            numberBg   = Color(0xCCFFFFFF),
            numberText = Color(0xFF004D40),
        ),
        BoardColorScheme(
            id = 4, name = "Royal", emoji = "👑",
            cellLight  = Color(0xFFEDE7F6),
            cellDark   = Color(0xFFCE93D8),
            gridLine   = Color(0xFFAB47BC),
            numberBg   = Color(0xCCFFFFFF),
            numberText = Color(0xFF4A148C),
        ),
        BoardColorScheme(
            id = 5, name = "Midnight", emoji = "🌙",
            cellLight  = Color(0xFF1A237E),
            cellDark   = Color(0xFF283593),
            gridLine   = Color(0xFF3949AB),
            numberBg   = Color(0x33FFFFFF),
            numberText = Color(0xFFE8EAF6),
        ),
        BoardColorScheme(
            id = 6, name = "Candy", emoji = "🍭",
            cellLight  = Color(0xFFE8F5E9),
            cellDark   = Color(0xFFFFCDD2),
            gridLine   = Color(0xFFEF9A9A),
            numberBg   = Color(0xCCFFFFFF),
            numberText = Color(0xFF880E4F),
        ),
    )
}
