package com.skd.snake_ladder.ui.view

import androidx.activity.compose.BackHandler
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

private val AppBg = Color(0xFF141C2E)
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

private val BoardFrame      = Color(0xFF4E2B00)
private val BoardFrameLight = Color(0xFF7B4A1A)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    // ── Derived state ──────────────────────────────────────────────────────
    val turnText = when {
        state.winner != null -> stringResource(R.string.game_over)
        state.isRolling      -> stringResource(R.string.rolling)
        state.gameMode == GameMode.VS_COMPUTER ->
            if (state.isPlayerTurn) stringResource(R.string.your_turn)
            else                    stringResource(R.string.computer_turn)
        else -> "Player ${state.currentPlayerIndex + 1}'s Turn"
    }

    val isDiceEnabled = !state.isRolling &&
            state.winner == null &&
            (state.gameMode != GameMode.VS_COMPUTER || state.isPlayerTurn)

    val chipColor = when {
        state.winner != null -> Color(0xFFF9A825)
        state.isRolling      -> Color(0xFF546E7A)
        else -> PLAYER_COLORS.getOrElse(state.currentPlayerIndex) { Color(0xFF1565C0) }
    }

    val activeSnakeFrom  = if (state.lastEvent == GameEvent.SNAKE)  state.lastEventPosition else null
    val activeLadderFrom = if (state.lastEvent == GameEvent.LADDER) state.lastEventPosition else null

    val playerNames: List<String> = List(state.playerCount) { idx ->
        when {
            state.gameMode == GameMode.VS_COMPUTER && idx == 0 -> stringResource(R.string.player)
            state.gameMode == GameMode.VS_COMPUTER             -> stringResource(R.string.computer)
            else -> "P${idx + 1}"
        }
    }

    // Split players into top / bottom halves (ceil/floor of N/2)
    // 2→1+1  3→2+1  4→2+2  5→3+2  6→3+3
    val topCount    = (state.playerCount + 1) / 2
    val topIndices    = (0 until topCount)
    val bottomIndices = (topCount until state.playerCount)

    // ── Layout ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val cdBack = stringResource(R.string.cd_back)
                TextButton(
                    onClick  = onBack,
                    modifier = Modifier.semantics { contentDescription = cdBack }
                ) {
                    Text(
                        text     = "← ${stringResource(R.string.back_to_menu)}",
                        fontSize = 13.sp,
                        color    = Color(0xFF90CAF9)
                    )
                }
                Text(
                    text       = stringResource(R.string.game_title),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = Color.White
                )
                Spacer(modifier = Modifier.width(88.dp))
            }

            // ── Turn chip ─────────────────────────────────────────────────
            Surface(
                shape           = RoundedCornerShape(20.dp),
                color           = chipColor,
                shadowElevation = 4.dp,
                modifier        = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text       = turnText,
                    modifier   = Modifier.padding(horizontal = 22.dp, vertical = 7.dp),
                    color      = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Top player row ────────────────────────────────────────────
            PlayerRow(
                indices      = topIndices,
                state        = state,
                playerNames  = playerNames
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Board ─────────────────────────────────────────────────────
            BoardWithOverlay(
                playerPositions  = state.positions.mapIndexed { idx, pos ->
                    Pair(pos, PLAYER_COLORS.getOrElse(idx) { Color.Gray })
                },
                activeSnakeFrom  = activeSnakeFrom,
                activeLadderFrom = activeLadderFrom
            )

            // ── Bottom player row ─────────────────────────────────────────
            if (bottomIndices.any()) {
                Spacer(modifier = Modifier.height(6.dp))
                PlayerRow(
                    indices     = bottomIndices,
                    state       = state,
                    playerNames = playerNames
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Dice ──────────────────────────────────────────────────────
            DiceSection(
                diceValue = state.diceValue,
                isRolling = state.isRolling,
                isEnabled = isDiceEnabled,
                onRoll    = { viewModel.rollDice() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Winner dialog ─────────────────────────────────────────────────
        if (state.winner != null) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = { viewModel.restartGame() },
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) { Text(stringResource(R.string.play_again)) }
                },
                dismissButton = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back_to_menu)) }
                },
                title = {
                    Text(
                        text       = stringResource(R.string.game_over),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp
                    )
                },
                text = {
                    Text(
                        text      = stringResource(R.string.winner_message, state.winner ?: ""),
                        fontSize  = 18.sp,
                        textAlign = TextAlign.Center
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

// ── Equal-width player card row ───────────────────────────────────────────────
@Composable
private fun PlayerRow(
    indices: IntRange,
    state: com.skd.snake_ladder.domain.model.GameState,
    playerNames: List<String>
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        indices.forEach { idx ->
            ProfileSection(
                name       = playerNames.getOrElse(idx) { "P${idx + 1}" },
                position   = state.positions.getOrElse(idx) { 0 },
                isActive   = state.currentPlayerIndex == idx && state.winner == null,
                tokenColor = PLAYER_COLORS.getOrElse(idx) { Color.Gray },
                modifier   = Modifier.weight(1f)
            )
        }
    }
}

// ── Board frame + canvas ──────────────────────────────────────────────────────
@Composable
private fun BoardWithOverlay(
    playerPositions: List<Pair<Int, Color>>,
    activeSnakeFrom: Int?,
    activeLadderFrom: Int?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(BoardFrameLight, BoardFrame, BoardFrameLight)))
            .padding(10.dp)
    ) {
        BoardCanvas(
            playerPositions  = playerPositions,
            activeSnakeFrom  = activeSnakeFrom,
            activeLadderFrom = activeLadderFrom
        )
    }
}
