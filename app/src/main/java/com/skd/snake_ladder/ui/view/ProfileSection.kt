package com.skd.snake_ladder.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R
import com.skd.snake_ladder.ui.theme.LocalAppColors

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
    val colors = LocalAppColors.current
    val effectiveEliminated = isEliminated || isDisconnected

    val borderColor by animateColorAsState(
        targetValue = when {
            effectiveEliminated -> colors.cardBorderDisconnected
            isActive            -> colors.cardBorderActive
            else                -> colors.cardBorder
        },
        animationSpec = tween(400), label = "border"
    )

    val borderWidth by animateFloatAsState(
        targetValue = if (isActive && !effectiveEliminated) 1.8f else 1f,
        animationSpec = tween(300), label = "borderWidth"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = when {
            effectiveEliminated -> 0f
            isActive            -> 5f
            else                -> 2f
        },
        animationSpec = tween(400), label = "shadow"
    )

    val timerColor = when {
        timeRemaining > 20 -> Color(0xFF43A047)
        timeRemaining > 10 -> Color(0xFFFF9800)
        else               -> Color(0xFFEF5350)
    }

    val cardBg = when {
        isDisconnected -> colors.cardGradientDisconnected
        isEliminated   -> colors.cardGradientEliminated
        isActive       -> colors.cardGradientActive
        else           -> colors.cardGradient
    }

    Box(modifier = modifier.padding(horizontal = 4.dp, vertical = 3.dp)) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation    = shadowElevation.dp,
                    shape        = RoundedCornerShape(12.dp),
                    ambientColor = if (isActive && !effectiveEliminated)
                        colors.cardBorderActive.copy(alpha = 0.25f) else Color.Black,
                    spotColor    = if (isActive && !effectiveEliminated)
                        colors.cardBorderActive.copy(alpha = 0.15f) else Color.Black
                )
                .clip(RoundedCornerShape(12.dp))
                .background(cardBg)
                .border(borderWidth.dp, borderColor, RoundedCornerShape(12.dp))
        ) {
            // Active accent bar along the top edge
            if (isActive && !effectiveEliminated) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    colors.cardBorderActive,
                                    colors.cardBorderActive.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            ) {

                // ── Row 1: token • name • position ──────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(if (effectiveEliminated) Color(0xFF808080) else tokenColor)
                        drawCircle(
                            Color(0x55FFFFFF),
                            radius = size.minDimension * 0.25f,
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
                        fontSize   = 12.sp,
                        color      = if (effectiveEliminated) colors.textOnEliminated else colors.textPrimary,
                        maxLines   = 1
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = if (position == 0) "Start" else "#$position",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color      = if (effectiveEliminated) colors.textHint else colors.textSecondary
                    )
                }

                Spacer(Modifier.height(5.dp))

                // ── Row 2: timer bar (fixed height, empty for inactive) ──────
                // Fixed 14dp height for both active and inactive keeps card
                // heights equal regardless of turn state.
                Box(
                    modifier        = Modifier.fillMaxWidth().height(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isActive && !effectiveEliminated) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            // Timer track + fill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(colors.timerTrack)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(timeRemaining / 30f)
                                        .fillMaxHeight()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(timerColor, timerColor.copy(alpha = 0.5f))
                                            ),
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                            Text(
                                text       = "${timeRemaining}s",
                                color      = timerColor,
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Row 3: skip dots • badge ─────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Skip life dots
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (i < skipsUsed) Color(0xFFEF5350)
                                        else colors.skipDotEmpty,
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Status badge (right side)
                    when {
                        isDisconnected -> SmallBadge(
                            text  = "LEFT",
                            bg    = Color(0x33546E7A),
                            border = Color(0x55546E7A),
                            color = Color(0xFF78909C)
                        )
                        isEliminated -> SmallBadge(
                            text  = "OUT",
                            bg    = Color(0x33EF5350),
                            border = Color(0x55EF5350),
                            color = Color(0xFFEF5350)
                        )
                        isActive -> SmallBadge(
                            text  = stringResource(R.string.your_turn_badge),
                            bg    = Color(0xFFFFD700),
                            border = Color.Transparent,
                            color = Color(0xFF1A1200)
                        )
                        else -> Spacer(Modifier.width(1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallBadge(text: String, bg: Color, border: Color, color: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .border(0.5.dp, border, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text          = text,
            color         = color,
            fontSize      = 7.sp,
            fontWeight    = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}
