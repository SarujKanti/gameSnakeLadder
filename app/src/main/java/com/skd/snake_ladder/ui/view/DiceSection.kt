package com.skd.snake_ladder.ui.view

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R
import com.skd.snake_ladder.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun DiceSection(
    diceValue: Int,
    isRolling: Boolean,
    isEnabled: Boolean = !isRolling,
    /** Override the label shown when the dice is disabled (e.g. "Opponent's Turn"). */
    waitingLabel: String? = null,
    onRoll: () -> Unit
) {
    // ── Rotation animation (spin on tap OR when isRolling from Firebase) ─
    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dice_rotation"
    )

    // ── Bounce animation (spring when roll settles) ──────────────────────
    var scaleTarget by remember { mutableFloatStateOf(1f) }
    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "dice_scale"
    )

    // Trigger spin whenever isRolling becomes true (covers both own tap and
    // opponent roll seen via Firebase). Settle-bounce plays when it stops.
    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Keep advancing rotation target every 250 ms — the 600 ms tween
            // can't keep up, so the dice appears to spin continuously.
            while (true) {
                rotationTarget += 360f
                delay(250)
            }
        } else {
            scaleTarget = 1.20f
            delay(120)
            scaleTarget = 1f
        }
    }

    val colors = LocalAppColors.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // ── Dice box ────────────────────────────────────────────────────
        val elevation = if (isEnabled) 12.dp else 4.dp

        Canvas(
            modifier = Modifier
                .size(72.dp)
                .shadow(elevation, RoundedCornerShape(16.dp), clip = false)
                .graphicsLayer {
                    rotationZ = rotation
                    scaleX    = scale
                    scaleY    = scale
                }
                .clickable(
                    enabled             = isEnabled,
                    indication          = null,
                    interactionSource   = remember { MutableInteractionSource() }
                ) {
                    rotationTarget += 720f
                    onRoll()
                }
        ) {
            val cr = size.width * 0.18f   // corner radius

            // ── Face gradient (ivory-cream with depth) ───────────────────
            val faceColors = if (isEnabled)
                listOf(Color(0xFFF8F6EE), Color(0xFFE2DDD0))
            else
                listOf(Color(0xFFC8C8C8), Color(0xFFAAAAAA))

            drawRoundRect(
                brush        = Brush.linearGradient(
                    colors = faceColors,
                    start  = Offset(0f, 0f),
                    end    = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(cr, cr)
            )

            // ── Top-left highlight streak ────────────────────────────────
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0x44FFFFFF), Color(0x00FFFFFF)),
                    start  = Offset(0f, 0f),
                    end    = Offset(size.width * 0.55f, size.height * 0.55f)
                ),
                cornerRadius = CornerRadius(cr, cr)
            )

            // ── Edge border ──────────────────────────────────────────────
            drawRoundRect(
                color        = if (isEnabled) Color(0xFFBBBBB0) else Color(0xFF909090),
                cornerRadius = CornerRadius(cr, cr),
                style        = Stroke(width = 1.8f)
            )

            // ── Pips ─────────────────────────────────────────────────────
            val dotR = size.width / 10.5f
            val cX   = size.width  / 2f
            val cY   = size.height / 2f
            val l    = size.width  * 0.275f
            val r    = size.width  * 0.725f
            val t    = size.height * 0.275f
            val b    = size.height * 0.725f

            val pipColor = if (isEnabled) Color(0xFF1A1A1A) else Color(0xFF555555)

            fun pip(x: Float, y: Float) {
                // Concave shadow (makes pip look recessed)
                drawCircle(Color(0x28000000), radius = dotR + 1f,   center = Offset(x + 0.8f, y + 0.8f), style = Fill)
                // Pip body
                drawCircle(pipColor,          radius = dotR,         center = Offset(x, y))
                // Catch-light
                drawCircle(Color(0x44FFFFFF), radius = dotR * 0.28f, center = Offset(x - dotR * 0.22f, y - dotR * 0.22f))
            }

            when (diceValue) {
                1 -> pip(cX, cY)
                2 -> { pip(l, t); pip(r, b) }
                3 -> { pip(l, t); pip(cX, cY); pip(r, b) }
                4 -> { pip(l, t); pip(r, t); pip(l, b); pip(r, b) }
                5 -> { pip(l, t); pip(r, t); pip(cX, cY); pip(l, b); pip(r, b) }
                6 -> { pip(l, t); pip(l, cY); pip(l, b); pip(r, t); pip(r, cY); pip(r, b) }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when {
                isRolling  -> stringResource(R.string.rolling)
                !isEnabled -> waitingLabel ?: stringResource(R.string.computer_turn)
                else       -> stringResource(R.string.tap_to_roll)
            },
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = if (isEnabled) colors.accent else colors.textSecondary
        )
    }
}
