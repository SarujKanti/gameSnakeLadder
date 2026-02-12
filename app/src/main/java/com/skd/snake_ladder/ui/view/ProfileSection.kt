package com.skd.snake_ladder.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileSection(
    name: String,
    position: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier   // 👈 Add this
) {

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive)
            Color(0xFF4CAF50)
        else
            Color(0xFFE0E0E0),
        animationSpec = tween(400),
        label = ""
    )

    Column(
        modifier = modifier
            .padding(8.dp)
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else Color.Black
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Position: $position",
            color = if (isActive) Color.White else Color.Black
        )
    }
}
