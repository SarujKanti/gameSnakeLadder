package com.skd.snake_ladder.domain.usecase

import kotlin.random.Random

class RollDiceUseCase {

    fun roll(): Int {
        return Random.nextInt(1, 7)
    }
}