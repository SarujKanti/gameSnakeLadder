package com.skd.snake_ladder.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R

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
    val borderColor by animateColorAsState(
        targetValue = when {
            isEliminated -> Color(0x55EF5350)
            isActive     -> Color(0xFFFFD700)
            else         -> Color.Transparent
        },
        animationSpec = tween(400), label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isEliminated -> Color(0xFF1A0F0F)
            isActive     -> Color(0xFF1A237E)
            else         -> Color(0xFF1E2A40)
        },
        animationSpec = tween(400), label = "bg"
    )

    val timerColor = when {
        timeRemaining > 20 -> Color(0xFF4CAF50)
        timeRemaining > 10 -> Color(0xFFFFA726)
        else               -> Color(0xFFEF5350)
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {

            // Token dot + name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(if (isEliminated) Color.Gray else tokenColor)
                    drawCircle(
                        Color(0x55FFFFFF),
                        radius = size.minDimension * 0.22f,
                        center = center.copy(
                            x = center.x - size.minDimension * 0.18f,
                            y = center.y - size.minDimension * 0.18f
                        )
                    )
                }
                Spacer(Modifier.width(5.dp))
                Text(
                    text       = name,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp,
                    color      = if (isEliminated) Color(0xFF666666) else Color.White,
                    maxLines   = 1
                )
            }

            Spacer(Modifier.height(4.dp))

            // Position pill
            Box(
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text       = "${stringResource(R.string.position_label)} $position",
                    color      = if (isEliminated) Color(0xFF555555) else Color(0xFFB0BEC5),
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(4.dp))

            // Timer pill — only when it's this player's turn
            if (isActive && !isEliminated) {
                Box(
                    modifier = Modifier
                        .background(timerColor.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = "⏱ ${timeRemaining}s",
                        color      = timerColor,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // Skip dots (3 slots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (i < skipsUsed) Color(0xFFEF5350) else Color(0x33FFFFFF),
                                shape = CircleShape
                            )
                    )
                }
            }

            // "YOUR TURN" badge / "OUT" badge
            when {
                isEliminated -> {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEF5350).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = "OUT",
                            color      = Color(0xFFEF5350),
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
                else -> {
                    AnimatedVisibility(
                        visible = isActive,
                        enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                        exit    = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .background(Color(0xFFFFD700), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text          = stringResource(R.string.your_turn_badge),
                                color         = Color(0xFF1A1A1A),
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
