package com.skd.snake_ladder.domain.model

enum class GameEvent { SNAKE, LADDER }

data class GameState(
    val positions: List<Int> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val playerCount: Int = 2,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val winner: String? = null,
    val gameMode: GameMode? = null,
    val lastEvent: GameEvent? = null,
    val lastEventPosition: Int = 0,

    // ── Skip / timer fields ──────────────────────────────────────────────
    /** How many times each player has been auto-skipped (max 3). */
    val skipCounts: List<Int> = emptyList(),
    /** Indices of players who used all 3 skips and are now eliminated. */
    val eliminatedPlayers: Set<Int> = emptySet(),
    /** Seconds remaining for the current player's turn (counts down from 30). */
    val timeRemaining: Int = 30,
) {
    val isPlayerTurn: Boolean get() = currentPlayerIndex == 0
}
