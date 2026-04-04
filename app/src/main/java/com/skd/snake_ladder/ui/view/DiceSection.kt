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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R

@Composable
fun DiceSection(
    diceValue: Int,
    isRolling: Boolean,
    isEnabled: Boolean = !isRolling,
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
                .background(
                    if (isEnabled) Color(0xFF1565C0) else Color(0xFF90A4AE),
                    RoundedCornerShape(20.dp)
                )
                .clickable(enabled = isEnabled) {
                    rotationTarget += 720f
                    onRoll()
                }
                .semantics {
                    contentDescription = "Dice showing $diceValue. ${if (isEnabled) "Tap to roll" else "Not your turn"}"
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

                val dotColor = Color.White
                fun drawDotWhite(x: Float, y: Float) {
                    drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y), style = Fill)
                }

                when (diceValue) {
                    1 -> drawDotWhite(center, middle)
                    2 -> { drawDotWhite(left, top); drawDotWhite(right, bottom) }
                    3 -> { drawDotWhite(center, middle); drawDotWhite(left, top); drawDotWhite(right, bottom) }
                    4 -> { drawDotWhite(left, top); drawDotWhite(right, top); drawDotWhite(left, bottom); drawDotWhite(right, bottom) }
                    5 -> { drawDotWhite(center, middle); drawDotWhite(left, top); drawDotWhite(right, top); drawDotWhite(left, bottom); drawDotWhite(right, bottom) }
                    6 -> { drawDotWhite(left, top); drawDotWhite(left, middle); drawDotWhite(left, bottom); drawDotWhite(right, top); drawDotWhite(right, middle); drawDotWhite(right, bottom) }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                isRolling -> stringResource(R.string.rolling)
                !isEnabled -> stringResource(R.string.computer_turn)
                else -> stringResource(R.string.tap_to_roll)
            },
            fontSize = 15.sp,
            color = Color(0xFF37474F)
        )
    }
}
