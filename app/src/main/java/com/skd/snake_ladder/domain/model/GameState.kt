package com.skd.snake_ladder.domain.model

data class GameState(
    val playerPosition: Int = 0,
    val opponentPosition: Int = 0,
    val diceValue: Int = 1,
    val isPlayerTurn: Boolean = true,
    val isRolling: Boolean = false,
    val winner: String? = null,
    val gameMode: GameMode? = null,
)
