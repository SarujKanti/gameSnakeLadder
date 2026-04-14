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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skd.snake_ladder.R
import com.skd.snake_ladder.domain.model.GameMode

// Token colours — one per possible player slot (indices 0–5)
internal val PLAYER_COLORS = listOf(
    Color(0xFF2196F3), // 1 – sky blue
    Color(0xFFE91E63), // 2 – pink
    Color(0xFF4CAF50), // 3 – green
    Color(0xFFFF9800), // 4 – amber
    Color(0xFF9C27B0), // 5 – purple
    Color(0xFF00BCD4), // 6 – cyan
)

internal val AppBgGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f to Color(0xFF070D1A),
        0.45f to Color(0xFF0F1829),
        1.0f to Color(0xFF0A1020)
    )
)

@Composable
fun ModeSelectionScreen(
    onModeSelected: (GameMode, Int) -> Unit
) {
    var multiExpanded by remember { mutableStateOf(false) }
    var selectedCount by remember { mutableStateOf(2) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🐍", fontSize = 48.sp)
                Text("🪜", fontSize = 48.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Gradient title text
            Text(
                text = stringResource(R.string.game_title),
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC), Color(0xFFFFD700))
                    ),
                    fontSize   = 38.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text      = stringResource(R.string.game_subtitle),
                fontSize  = 14.sp,
                color     = Color(0xFF607D8B),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // Decorative divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0x44FFD700))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .size(5.dp)
                        .background(Color(0xFFFFD700), CircleShape)
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0x44FFD700), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text       = stringResource(R.string.select_mode),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color(0xFF546E7A),
                letterSpacing = 1.5.sp,
                textAlign  = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // ── VS Computer card ──────────────────────────────────────────
            ModeCard(
                onClick = { onModeSelected(GameMode.VS_COMPUTER, 2) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = stringResource(R.string.mode_vs_computer),
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFFECEFF1)
                        )
                        Text(
                            text     = "Challenge the AI",
                            fontSize = 13.sp,
                            color    = Color(0xFF546E7A)
                        )
                    }
                    Text(
                        text     = "›",
                        fontSize = 22.sp,
                        color    = Color(0xFF4FC3F7),
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Multiplayer card ──────────────────────────────────────────
            ModeCard(onClick = { multiExpanded = !multiExpanded }) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6A1B9A), Color(0xFF4A148C))
                                    ),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👥", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = stringResource(R.string.mode_multiplayer),
                                fontSize   = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFFECEFF1)
                            )
                            Text(
                                text     = "2 – 6 players",
                                fontSize = 13.sp,
                                color    = Color(0xFF546E7A)
                            )
                        }
                        Text(
                            text     = if (multiExpanded) "⌃" else "⌄",
                            fontSize = 18.sp,
                            color    = Color(0xFF4FC3F7)
                        )
                    }

                    // Expandable section
                    AnimatedVisibility(
                        visible = multiExpanded,
                        enter   = expandVertically(tween(260)) + fadeIn(tween(260)),
                        exit    = shrinkVertically(tween(200)) + fadeOut(tween(200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x0AFFFFFF))
                                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(
                                color     = Color(0x15FFFFFF),
                                thickness = 1.dp
                            )
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text       = stringResource(R.string.select_players),
                                fontSize   = 12.sp,
                                color      = Color(0xFF546E7A),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )

                            Spacer(Modifier.height(14.dp))

                            // Number chips 2–6
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

                            Spacer(Modifier.height(10.dp))

                            // Player colour preview dots
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment     = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                repeat(selectedCount) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(10.dp)
                                            .background(PLAYER_COLORS[idx], CircleShape)
                                    )
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // Start button with gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(6.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1976D2), Color(0xFF1565C0), Color(0xFF0D47A1))
                                        )
                                    )
                                    .clickable { onModeSelected(GameMode.MULTI_PLAYER, selectedCount) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = stringResource(R.string.start_game, selectedCount),
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text      = stringResource(R.string.tagline),
                fontSize  = 12.sp,
                color     = Color(0xFF37474F),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ModeCard(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A2540), Color(0xFF111C33))
                )
            )
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        content()
    }
}

@Composable
private fun PlayerCountChip(
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val bg     = if (selected) color          else Color(0x18FFFFFF)
    val border = if (selected) color          else Color(0x2AFFFFFF)
    val text   = if (selected) Color.White    else Color(0x88FFFFFF)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.5.dp, border, CircleShape)
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
