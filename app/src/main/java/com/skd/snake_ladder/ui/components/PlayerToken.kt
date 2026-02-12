package com.skd.snake_ladder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun PlayerToken(
    modifier: Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        drawCircle(color = color)
    }
}
