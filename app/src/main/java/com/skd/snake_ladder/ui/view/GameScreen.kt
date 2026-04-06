package com.skd.snake_ladder.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.R
import com.skd.snake_ladder.domain.model.GameEvent
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.viewmodel.GameViewModel

private val PlayerBlue  = Color(0xFF1565C0)
private val PlayerPink  = Color(0xFFAD1457)
private val BgTop       = Color(0xFF0D1B2A)
private val BgBottom    = Color(0xFF1B3A5C)
private val BoardFrame  = Color(0xFF4E2B00)
private val BoardFrameLight = Color(0xFF7B4A1A)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    val turnText = when {
        state.winner != null -> stringResource(R.string.game_over)
        state.isRolling      -> stringResource(R.string.rolling)
        state.isPlayerTurn   -> when (state.gameMode) {
            GameMode.VS_COMPUTER -> stringResource(R.string.your_turn)
            GameMode.TWO_PLAYERS -> stringResource(R.string.player1_turn)
            else -> ""
        }
        else -> when (state.gameMode) {
            GameMode.VS_COMPUTER -> stringResource(R.string.computer_turn)
            GameMode.TWO_PLAYERS -> stringResource(R.string.player2_turn)
            else -> ""
        }
    }

    val isDiceEnabled = !state.isRolling &&
            (state.gameMode != GameMode.VS_COMPUTER || state.isPlayerTurn) &&
            state.winner == null

    val chipColor = when {
        state.winner != null -> Color(0xFFF9A825)
        state.isRolling      -> Color(0xFF546E7A)
        state.isPlayerTurn   -> Color(0xFF1565C0)
        else                 -> Color(0xFF78909C)
    }

    // Active snake/ladder positions for board highlight + event overlay
    val activeSnakeFrom  = if (state.lastEvent == GameEvent.SNAKE)  state.lastEventPosition else null
    val activeLadderFrom = if (state.lastEvent == GameEvent.LADDER) state.lastEventPosition else null
    // Captured outside AnimatedVisibility so exit animation still shows correct icon
    val isSnakeEvent     = state.lastEvent == GameEvent.SNAKE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val cdBack = stringResource(R.string.cd_back)
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.semantics {
                        contentDescription = cdBack
                    }
                ) {
                    Text(
                        text = "← ${stringResource(R.string.back_to_menu)}",
                        fontSize = 13.sp,
                        color = Color(0xFF90CAF9)
                    )
                }

                Text(
                    text = stringResource(R.string.game_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(88.dp))
            }

            // ── Turn indicator chip ────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = chipColor,
                shadowElevation = 4.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Text(
                    text = turnText,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Player profile cards ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                when (state.gameMode) {
                    GameMode.VS_COMPUTER -> {
                        ProfileSection(
                            name       = stringResource(R.string.player),
                            position   = state.playerPosition,
                            isActive   = state.isPlayerTurn,
                            tokenColor = PlayerBlue,
                            modifier   = Modifier.weight(1f)
                        )
                        ProfileSection(
                            name       = stringResource(R.string.computer),
                            position   = state.opponentPosition,
                            isActive   = !state.isPlayerTurn,
                            tokenColor = PlayerPink,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                    GameMode.TWO_PLAYERS -> {
                        ProfileSection(
                            name       = stringResource(R.string.player_1),
                            position   = state.playerPosition,
                            isActive   = state.isPlayerTurn,
                            tokenColor = PlayerBlue,
                            modifier   = Modifier.weight(1f)
                        )
                        ProfileSection(
                            name       = stringResource(R.string.player_2),
                            position   = state.opponentPosition,
                            isActive   = !state.isPlayerTurn,
                            tokenColor = PlayerPink,
                            modifier   = Modifier.weight(1f)
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Wooden board frame + board + event overlay ─────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(BoardFrameLight, BoardFrame, BoardFrameLight)
                        )
                    )
                    .padding(10.dp)
            ) {
                // Board
                BoardCanvas(
                    playerPositions  = listOf(
                        Pair(state.playerPosition,  PlayerBlue),
                        Pair(state.opponentPosition, PlayerPink)
                    ),
                    activeSnakeFrom  = activeSnakeFrom,
                    activeLadderFrom = activeLadderFrom
                )

                // Snake / Ladder event overlay
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedVisibility(
                        visible = state.lastEvent != null,
                        enter   = fadeIn(tween(300)),
                        exit    = fadeOut(tween(300))
                    ) {
                        EventOverlay(isSnake = isSnakeEvent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Dice ───────────────────────────────────────────────────────
            DiceSection(
                diceValue = state.diceValue,
                isRolling = state.isRolling,
                isEnabled = isDiceEnabled,
                onRoll    = { viewModel.rollDice() }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ── Winner dialog ──────────────────────────────────────────────────
        if (state.winner != null) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetGame() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) {
                        Text(stringResource(R.string.play_again))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.back_to_menu))
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.game_over),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.winner_message, state.winner ?: ""),
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ── Snake / Ladder pop-up card ──────────────────────────────────────────────
@Composable
private fun EventOverlay(isSnake: Boolean) {
    val bg    = if (isSnake) Color(0xEED32F2F) else Color(0xEE2E7D32)
    val icon  = if (isSnake) "🐍" else "🪜"
    val title = if (isSnake) "Snake!" else "Ladder!"
    val desc  = if (isSnake) "Sliding down\u2026" else "Climbing up!"

    Box(
        modifier = Modifier
            .shadow(16.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 32.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon,  fontSize = 48.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                text       = title,
                color      = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 26.sp
            )
            Text(
                text     = desc,
                color    = Color(0xCCFFFFFF),
                fontSize = 15.sp
            )
        }
    }
}
