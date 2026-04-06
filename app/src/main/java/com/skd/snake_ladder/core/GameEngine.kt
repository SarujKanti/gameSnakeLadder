package com.skd.snake_ladder.core

import com.skd.snake_ladder.data.SnakeLadderConfig

class GameEngine {
    fun calculateNewPosition(current: Int, dice: Int): Int {

        var newPosition = current

        if (dice > 0) {
            newPosition += dice
            if (newPosition > 100) return current
        }

        newPosition = SnakeLadderConfig.snakes[newPosition]
            ?: SnakeLadderConfig.ladders[newPosition]
                    ?: newPosition

        return newPosition
    }

    fun checkWinner(position: Int): Boolean = position == 100

    fun isSnakePosition(position: Int): Boolean = SnakeLadderConfig.snakes.containsKey(position)
    fun isLadderPosition(position: Int): Boolean = SnakeLadderConfig.ladders.containsKey(position)
}