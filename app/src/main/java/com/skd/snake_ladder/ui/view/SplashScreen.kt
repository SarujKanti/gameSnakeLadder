package com.skd.snake_ladder.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val colors = LocalAppColors.current

    val alpha  = remember { Animatable(0f) }
    val scale  = remember { Animatable(0.72f) }
    val alpha2 = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade-in + scale-up the logo
        alpha.animateTo(1f,  tween(700, easing = FastOutSlowInEasing))
        scale.animateTo(1f,  tween(700, easing = FastOutSlowInEasing))
        delay(300)
        // Fade in the tagline
        alpha2.animateTo(1f, tween(500))
        // Hold then navigate
        delay(900)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgGradient)
    ) {
        // ── Centre logo ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎲🐍", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text          = "Snake & Ladder",
                fontSize      = 30.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = colors.textPrimary,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text          = "Roll · Climb · Win",
                fontSize      = 14.sp,
                color         = colors.accent,
                fontWeight    = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }

        // ── Bottom tagline ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .alpha(alpha2.value)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, colors.divider, Color.Transparent)
                            )
                        )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text          = "Powered by SKD",
                    fontSize      = 12.sp,
                    color         = colors.textSecondary,
                    fontWeight    = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
