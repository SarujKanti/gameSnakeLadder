package com.skd.snake_ladder.domain.model

import androidx.compose.ui.graphics.Color

data class Player(
    val id: Int,
    val name: String,
    val position: Int = 0,
    val color: Color
)
