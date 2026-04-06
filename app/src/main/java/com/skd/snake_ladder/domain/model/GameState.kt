package com.skd.snake_ladder.domain.model

enum class GameEvent { SNAKE, LADDER }

data class GameState(
    val playerPosition: Int = 0,
    val opponentPosition: Int = 0,
    val diceValue: Int = 1,
    val isPlayerTurn: Boolean = true,
    val isRolling: Boolean = false,
    val winner: String? = null,
    val gameMode: GameMode? = null,
    val lastEvent: GameEvent? = null,
    val lastEventPosition: Int = 0,   // snake-head or ladder-bottom position
)
