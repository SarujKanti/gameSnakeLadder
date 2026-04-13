package com.skd.snake_ladder.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R
import com.skd.snake_ladder.domain.model.GameMode

// Token colours — one per possible player slot (indices 0–5)
internal val PLAYER_COLORS = listOf(
    Color(0xFF1565C0), // 1 – blue
    Color(0xFFAD1457), // 2 – pink
    Color(0xFF2E7D32), // 3 – green
    Color(0xFFE65100), // 4 – orange
    Color(0xFF6A1B9A), // 5 – purple
    Color(0xFF00695C), // 6 – teal
)

private val BgTop    = Color(0xFF0D1B2A)
private val BgBottom = Color(0xFF1B3A5C)

@Composable
fun ModeSelectionScreen(
    onModeSelected: (GameMode, Int) -> Unit
) {
    var multiExpanded by remember { mutableStateOf(false) }
    var selectedCount by remember { mutableStateOf(2) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🪜", fontSize = 52.sp)
                Text("🐍", fontSize = 52.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text       = stringResource(R.string.game_title),
                fontSize   = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.White,
                textAlign  = TextAlign.Center
            )
            Text(
                text      = stringResource(R.string.game_subtitle),
                fontSize  = 15.sp,
                color     = Color(0xFFBBDEFB),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text       = stringResource(R.string.select_mode),
                fontSize   = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFFE3F2FD),
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ── VS Computer ───────────────────────────────────────────────
            ModeButton(
                icon      = "🤖",
                label     = stringResource(R.string.mode_vs_computer),
                bgColor   = Color.White,
                textColor = Color(0xFF0D47A1),
                onClick   = { onModeSelected(GameMode.VS_COMPUTER, 2) }
            )

            Spacer(Modifier.height(14.dp))

            // ── Multiplayer card ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF1B3A5C))
            ) {
                // Header row — tap to expand / collapse
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { multiExpanded = !multiExpanded }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("👥", fontSize = 24.sp)
                        Text(
                            text       = stringResource(R.string.mode_multiplayer),
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                    Text(
                        text     = if (multiExpanded) "▲" else "▼",
                        color    = Color(0xFF90CAF9),
                        fontSize = 14.sp
                    )
                }

                // Expandable player-count picker
                AnimatedVisibility(
                    visible = multiExpanded,
                    enter   = expandVertically(tween(260)) + fadeIn(tween(260)),
                    exit    = shrinkVertically(tween(200)) + fadeOut(tween(200))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider(color = Color(0x33FFFFFF), thickness = 1.dp)
                        Spacer(Modifier.height(14.dp))

                        Text(
                            text       = stringResource(R.string.select_players),
                            fontSize   = 14.sp,
                            color      = Color(0xFFB0BEC5),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(12.dp))

                        // Number chips: 2 – 6
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (2..6).forEach { count ->
                                PlayerCountChip(
                                    count    = count,
                                    selected = count == selectedCount,
                                    color    = PLAYER_COLORS[count - 1],
                                    onClick  = { selectedCount = count }
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Colour preview dots for selected player count
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            repeat(selectedCount) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(PLAYER_COLORS[idx], CircleShape)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick   = { onModeSelected(GameMode.MULTI_PLAYER, selectedCount) },
                            modifier  = Modifier.fillMaxWidth().height(52.dp),
                            shape     = RoundedCornerShape(14.dp),
                            colors    = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Text(
                                text       = stringResource(R.string.start_game, selectedCount),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text      = stringResource(R.string.tagline),
                fontSize  = 13.sp,
                color     = Color(0xFF90CAF9),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ModeButton(
    icon: String,
    label: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth().height(60.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = ButtonDefaults.buttonColors(containerColor = bgColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text       = "$icon  $label",
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = textColor
        )
    }
}

@Composable
private fun PlayerCountChip(
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val bg     = if (selected) color           else Color(0x22FFFFFF)
    val border = if (selected) color           else Color(0x44FFFFFF)
    val text   = if (selected) Color.White     else Color(0xAAFFFFFF)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(2.dp, border, CircleShape)
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "$count",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = text
        )
    }
}
