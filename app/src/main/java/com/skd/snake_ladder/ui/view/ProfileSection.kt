package com.skd.snake_ladder.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    isDisconnected: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Treat disconnected like eliminated for visual purposes (dimmed, not active)
    val effectiveEliminated = isEliminated || isDisconnected

    val borderColor by animateColorAsState(
        targetValue = when {
            effectiveEliminated -> Color(0x44546E7A)
            isActive            -> Color(0xFFFFD700)
            else                -> Color(0x18FFFFFF)
        },
        animationSpec = tween(400), label = "border"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = when {
            effectiveEliminated -> 0f
            isActive            -> 14f
            else                -> 1f
        },
        animationSpec = tween(400), label = "shadow"
    )

    val timerColor = when {
        timeRemaining > 20 -> Color(0xFF43A047)
        timeRemaining > 10 -> Color(0xFFFF9800)
        else               -> Color(0xFFEF5350)
    }

    val cardBg = when {
        isDisconnected -> Brush.linearGradient(
            listOf(Color(0xFF111820), Color(0xFF0B1018))
        )
        isEliminated -> Brush.linearGradient(
            listOf(Color(0xFF1C0E0E), Color(0xFF110A0A))
        )
        isActive     -> Brush.linearGradient(
            listOf(Color(0xFF1C3764), Color(0xFF102244))
        )
        else         -> Brush.linearGradient(
            listOf(Color(0xFF1A2540), Color(0xFF0F1829))
        )
    }

    Box(modifier = modifier.padding(4.dp)) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation    = shadowElevation.dp,
                    shape        = RoundedCornerShape(14.dp),
                    ambientColor = if (isActive && !effectiveEliminated) Color(0xFFFFD700) else Color.Black,
                    spotColor    = if (isActive && !effectiveEliminated) Color(0xFFFFD700) else Color.Black
                )
                .clip(RoundedCornerShape(14.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Token dot + name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Canvas(modifier = Modifier.size(9.dp)) {
                        drawCircle(if (effectiveEliminated) Color(0xFF404040) else tokenColor)
                        drawCircle(
                            Color(0x66FFFFFF),
                            radius = size.minDimension * 0.22f,
                            center = center.copy(
                                x = center.x - size.minDimension * 0.20f,
                                y = center.y - size.minDimension * 0.20f
                            )
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text       = name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 12.sp,
                        color      = if (effectiveEliminated) Color(0xFF4A4A5A) else Color(0xFFECEFF1),
                        maxLines   = 1
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Position pill
                Box(
                    modifier = Modifier
                        .background(Color(0x14FFFFFF), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text       = if (position == 0) "Start" else "Pos $position",
                        color      = if (effectiveEliminated) Color(0xFF3A3A4A) else Color(0xFF78909C),
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(5.dp))

                // Timer bar — only for active, non-eliminated/disconnected player
                if (isActive && !effectiveEliminated) {
                    val timerFraction = timeRemaining / 30f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0x20FFFFFF), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(timerFraction)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(timerColor, timerColor.copy(alpha = 0.6f))
                                    ),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text       = "${timeRemaining}s",
                        color      = timerColor,
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                } else {
                    Spacer(Modifier.height(5.dp))
                }

                // Skip life dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    if (i < skipsUsed) Color(0xFFEF5350)
                                    else Color(0x25FFFFFF),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Badge: LEFT / OUT / YOUR TURN
                when {
                    isDisconnected -> {
                        Box(
                            modifier = Modifier
                                .background(Color(0x33546E7A), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0x55546E7A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text          = "LEFT",
                                color         = Color(0xFF78909C),
                                fontSize      = 8.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    isEliminated -> {
                        Box(
                            modifier = Modifier
                                .background(Color(0x33EF5350), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0x55EF5350), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text          = "OUT",
                                color         = Color(0xFFEF5350),
                                fontSize      = 8.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
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
                                    .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text          = stringResource(R.string.your_turn_badge),
                                    color         = Color(0xFF1A1200),
                                    fontSize      = 8.sp,
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
}
