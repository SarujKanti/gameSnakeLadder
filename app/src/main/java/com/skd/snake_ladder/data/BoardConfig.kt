package com.skd.snake_ladder.data

data class BoardConfig(
    val id: Int,
    val name: String,
    val emoji: String,
    val snakes: Map<Int, Int>,
    val ladders: Map<Int, Int>
)

object BoardConfigs {

    val configs: List<BoardConfig> = listOf(

        // ── Board 1: Classic ──────────────────────────────────────────────────
        BoardConfig(
            id      = 0,
            name    = "Classic",
            emoji   = "🎮",
            snakes  = mapOf(99 to 54, 95 to 72, 58 to 29, 52 to 21, 23 to 2),
            ladders = mapOf(6 to 24, 11 to 40, 17 to 69, 32 to 74, 60 to 85)
        ),

        // ── Board 2: Jungle ───────────────────────────────────────────────────
        BoardConfig(
            id      = 1,
            name    = "Jungle",
            emoji   = "🌴",
            snakes  = mapOf(97 to 42, 87 to 53, 73 to 22, 45 to 16, 36 to 11),
            ladders = mapOf(3 to 28, 9 to 38, 18 to 64, 26 to 78, 62 to 90)
        ),

        // ── Board 3: Desert ───────────────────────────────────────────────────
        BoardConfig(
            id      = 2,
            name    = "Desert",
            emoji   = "🏜️",
            snakes  = mapOf(98 to 68, 84 to 45, 71 to 30, 53 to 18, 27 to 5),
            ladders = mapOf(4 to 33, 12 to 48, 21 to 61, 37 to 77, 56 to 88)
        ),

        // ── Board 4: Ocean ────────────────────────────────────────────────────
        BoardConfig(
            id      = 3,
            name    = "Ocean",
            emoji   = "🌊",
            snakes  = mapOf(96 to 44, 88 to 32, 75 to 47, 57 to 15, 29 to 7),
            ladders = mapOf(2 to 19, 8 to 43, 20 to 59, 41 to 72, 64 to 93)
        ),

        // ── Board 5: Space ────────────────────────────────────────────────────
        BoardConfig(
            id      = 4,
            name    = "Space",
            emoji   = "🚀",
            snakes  = mapOf(94 to 55, 82 to 38, 67 to 29, 50 to 14, 34 to 6),
            ladders = mapOf(5 to 25, 13 to 52, 22 to 68, 40 to 80, 57 to 93)
        ),

        // ── Board 6: Volcano ──────────────────────────────────────────────────
        BoardConfig(
            id      = 5,
            name    = "Volcano",
            emoji   = "🌋",
            snakes  = mapOf(93 to 62, 89 to 39, 77 to 26, 61 to 20, 42 to 9),
            ladders = mapOf(7 to 47, 13 to 67, 28 to 73, 46 to 85, 63 to 96)
        ),

        // ── Board 7: Enchanted ────────────────────────────────────────────────
        BoardConfig(
            id      = 6,
            name    = "Enchanted",
            emoji   = "✨",
            snakes  = mapOf(91 to 58, 85 to 44, 72 to 35, 48 to 12, 30 to 3),
            ladders = mapOf(1 to 24, 10 to 41, 25 to 67, 43 to 79, 66 to 88)
        ),
    )
}
