package com.skd.snake_ladder.ui.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
fun DiceSection(
    diceValue: Int,
    isRolling: Boolean,
    onRoll: () -> Unit
) {

    var rotationTarget by remember { mutableStateOf(0f) }

    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(
            durationMillis = 600,
            easing = FastOutSlowInEasing
        ),
        label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(110.dp)
                .rotate(rotation)
                .background(Color(0xFF87CEEB), RoundedCornerShape(20.dp)) // 🌤 Sky Blue
                .clickable(enabled = !isRolling) {
                    rotationTarget += 720f   // 2 full spins
                    onRoll()
                },
            contentAlignment = Alignment.Center
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {

                val dotRadius = size.width / 12
                val center = size.width / 2
                val left = size.width * 0.25f
                val right = size.width * 0.75f
                val top = size.height * 0.25f
                val bottom = size.height * 0.75f
                val middle = size.height / 2

                fun drawDot(x: Float, y: Float) {
                    drawCircle(
                        color = Color.Black,
                        radius = dotRadius,
                        center = Offset(x, y),
                        style = Fill
                    )
                }

                when (diceValue) {
                    1 -> drawDot(center, middle)

                    2 -> {
                        drawDot(left, top)
                        drawDot(right, bottom)
                    }

                    3 -> {
                        drawDot(center, middle)
                        drawDot(left, top)
                        drawDot(right, bottom)
                    }

                    4 -> {
                        drawDot(left, top)
                        drawDot(right, top)
                        drawDot(left, bottom)
                        drawDot(right, bottom)
                    }

                    5 -> {
                        drawDot(center, middle)
                        drawDot(left, top)
                        drawDot(right, top)
                        drawDot(left, bottom)
                        drawDot(right, bottom)
                    }

                    6 -> {
                        drawDot(left, top)
                        drawDot(left, middle)
                        drawDot(left, bottom)

                        drawDot(right, top)
                        drawDot(right, middle)
                        drawDot(right, bottom)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (isRolling) "Rolling..." else "Tap Dice 🎲"
        )
    }
}
