package com.skd.snake_ladder.core

import com.skd.snake_ladder.data.BoardConfig
import com.skd.snake_ladder.data.BoardConfigs

class GameEngine(val config: BoardConfig = BoardConfigs.configs[0]) {

    fun calculateNewPosition(current: Int, dice: Int): Int {
        var newPosition = current
        if (dice > 0) {
            newPosition += dice
            if (newPosition > 100) return current
        }
        newPosition = config.snakes[newPosition] ?: config.ladders[newPosition] ?: newPosition
        return newPosition
    }

    fun checkWinner(position: Int): Boolean = position == 100

    fun isSnakePosition(position: Int): Boolean  = config.snakes.containsKey(position)
    fun isLadderPosition(position: Int): Boolean = config.ladders.containsKey(position)
}
