package com.skd.snake_ladder.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFFD700) else Color.Transparent,
        animationSpec = tween(400),
        label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFF1A237E) else Color(0xFF2D3748),
        animationSpec = tween(400),
        label = "bg"
    )

    Box(
        modifier = modifier
            .padding(4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Token dot + player name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(tokenColor)
                    drawCircle(Color(0x55FFFFFF), radius = size.minDimension * 0.22f,
                        center = center.copy(x = center.x - size.minDimension * 0.18f,
                                            y = center.y - size.minDimension * 0.18f))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Position pill
            Box(
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.position_label)} $position",
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // "YOUR TURN" badge — animated in/out
            AnimatedVisibility(
                visible = isActive,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
                exit  = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .background(Color(0xFFFFD700), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.your_turn_badge),
                        color = Color(0xFF1A1A1A),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
