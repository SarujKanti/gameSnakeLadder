package com.skd.snake_ladder.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SkipUsedColor = Color(0xFFEF5350)    // red dot = skip used
private val SkipFreeColor = Color(0x33FFFFFF)    // faint = skip available

@Composable
fun ProfileSection(
    name: String,
    position: Int,
    isActive: Boolean,
    tokenColor: Color,
    timeRemaining: Int = 30,
    skipsUsed: Int = 0,
    isEliminated: Boolean = false,
    modifier: Modifier = Modifier
) {
    // ── Animated card colors ──────────────────────────────────────────────
    val borderColor by animateColorAsState(
        targetValue = when {
            isEliminated -> Color(0xFFEF5350)
            isActive     -> Color(0xFFFFD700)
            else         -> Color.Transparent
        },
        animationSpec = tween(350), label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isEliminated -> Color(0xFF1A0F0F)
            isActive     -> Color(0xFF1A237E)
            else         -> Color(0xFF1E2A40)
        },
        animationSpec = tween(350), label = "bg"
    )

    // Timer color: green → amber → red
    val timerColor = when {
        timeRemaining > 20 -> Color(0xFF4CAF50)
        timeRemaining > 10 -> Color(0xFFFFA726)
        else               -> Color(0xFFEF5350)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)          // square
            .padding(3.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Name row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Canvas(Modifier.size(7.dp)) {
                    drawCircle(if (isEliminated) Color.Gray else tokenColor)
                }
                Spacer(Modifier.width(3.dp))
                Text(
                    text       = name,
                    color      = if (isEliminated) Color(0xFF666666) else Color.White,
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    textAlign  = TextAlign.Center
                )
            }

            // ── Position ──────────────────────────────────────────────────
            Text(
                text       = "$position",
                color      = if (isEliminated) Color(0xFF555555) else Color(0xFFCFD8DC),
                fontSize   = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign  = TextAlign.Center
            )

            // ── Timer / status ────────────────────────────────────────────
            when {
                isEliminated -> {
                    Box(
                        modifier = Modifier
                            .background(SkipUsedColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "OUT",
                            color      = SkipUsedColor,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                isActive -> {
                    Box(
                        modifier = Modifier
                            .background(timerColor.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "${timeRemaining}s",
                            color      = timerColor,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> Spacer(Modifier.height(18.dp))
            }

            // ── Skip dots (3 max) ─────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (i < skipsUsed) SkipUsedColor else SkipFreeColor,
                                shape = CircleShape
                            )
                    )
                }
            }

            // ── "YOUR TURN" badge ─────────────────────────────────────────
            if (isActive && !isEliminated) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text       = "YOUR TURN",
                        color      = Color(0xFF1A1A1A),
                        fontSize   = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}
