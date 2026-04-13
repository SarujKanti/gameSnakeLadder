package com.skd.snake_ladder.domain.model

enum class GameEvent { SNAKE, LADDER }

data class GameState(
    /** One entry per player (index = player index). Empty until a mode is set. */
    val positions: List<Int> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val playerCount: Int = 2,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    val winner: String? = null,
    val gameMode: GameMode? = null,
    val lastEvent: GameEvent? = null,
    val lastEventPosition: Int = 0,
) {
    /** True when the human player's turn (index 0) — only meaningful for VS_COMPUTER. */
    val isPlayerTurn: Boolean get() = currentPlayerIndex == 0
}
