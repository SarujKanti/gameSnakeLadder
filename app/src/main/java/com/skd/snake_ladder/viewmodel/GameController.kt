package com.skd.snake_ladder.viewmodel

import com.skd.snake_ladder.data.BoardConfig
import com.skd.snake_ladder.data.BoardConfigs
import com.skd.snake_ladder.domain.model.GameState
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared interface implemented by both [GameViewModel] (local) and
 * [com.skd.snake_ladder.online.OnlineGameViewModel] (online).
 * [GameScreen] only depends on this interface.
 */
interface GameController {
    val state: StateFlow<GameState>

    /** Active board configuration — drives BoardCanvas rendering. */
    val boardConfig: BoardConfig get() = BoardConfigs.configs[0]

    fun rollDice()
    fun restartGame()
}
