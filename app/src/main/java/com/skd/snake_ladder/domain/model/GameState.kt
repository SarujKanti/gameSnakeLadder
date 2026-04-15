package com.skd.snake_ladder.domain.model

enum class GameEvent { SNAKE, LADDER }

data class GameState(
    val positions: List<Int> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val playerCount: Int = 2,
    val diceValue: Int = 1,
    val isRolling: Boolean = false,
    /**
     * True while the token is animating step-by-step after the dice has settled.
     * Local-only — never written to Firebase.
     */
    val isMoving: Boolean = false,
    val winner: String? = null,
    val gameMode: GameMode? = null,
    val lastEvent: GameEvent? = null,
    val lastEventPosition: Int = 0,

    // ── Skip / timer fields ──────────────────────────────────────────────
    /** How many times each player has been auto-skipped (max 3). */
    val skipCounts: List<Int> = emptyList(),
    /** Indices of players who used all 3 skips and are now eliminated. */
    val eliminatedPlayers: Set<Int> = emptySet(),
    /** Indices of online players who disconnected mid-game. */
    val disconnectedPlayers: Set<Int> = emptySet(),
    /** Seconds remaining for the current player's turn (counts down from 30). */
    val timeRemaining: Int = 30,
    /** Custom names entered by players before the game starts. */
    val playerNames: List<String> = emptyList(),
    /**
     * The index that represents "this device's player".
     * Local modes: always 0. Online: set per device so each player knows whose turn it is.
     */
    val myPlayerIndex: Int = 0,
) {
    /** True when it is this device's player's turn to act. */
    val isPlayerTurn: Boolean get() = currentPlayerIndex == myPlayerIndex
}
