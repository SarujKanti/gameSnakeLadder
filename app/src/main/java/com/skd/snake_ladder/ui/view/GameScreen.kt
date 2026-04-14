package com.skd.snake_ladder.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.skd.snake_ladder.viewmodel.GameController

private val BoardFrameGradient = Brush.linearGradient(
    listOf(
        Color(0xFF6D4C41),
        Color(0xFF4E342E),
        Color(0xFF3E2723),
        Color(0xFF4E342E),
        Color(0xFF6D4C41),
    )
)
private val BoardFrameBorder = Color(0xFF8D6E63)

@Composable
fun GameScreen(
    controller: GameController,
    onBack: () -> Unit = {}
) {
    val state by controller.state.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    val turnText = when {
        state.winner != null -> stringResource(R.string.game_over)
        state.isRolling      -> stringResource(R.string.rolling)
        state.gameMode == GameMode.VS_COMPUTER ->
            if (state.isPlayerTurn) stringResource(R.string.your_turn)
            else                    stringResource(R.string.computer_turn)
        state.gameMode == GameMode.ONLINE ->
            if (state.isPlayerTurn) "Your Turn"
            else {
                val name = state.playerNames.getOrNull(state.currentPlayerIndex)
                    ?.trim()?.takeIf { it.isNotBlank() }
                    ?: "Player ${state.currentPlayerIndex + 1}"
                "$name's Turn"
            }
        else -> {
            val name = state.playerNames.getOrNull(state.currentPlayerIndex)
                ?.trim()?.takeIf { it.isNotBlank() }
                ?: "Player ${state.currentPlayerIndex + 1}"
            "$name's Turn"
        }
    }

    val isDiceEnabled = !state.isRolling &&
            state.winner == null &&
            (state.gameMode != GameMode.VS_COMPUTER || state.isPlayerTurn) &&
            (state.gameMode != GameMode.ONLINE || state.isPlayerTurn) &&
            state.currentPlayerIndex !in state.eliminatedPlayers

    val chipAccent = when {
        state.winner != null -> Color(0xFFFFD700)
        state.isRolling      -> Color(0xFF546E7A)
        else -> PLAYER_COLORS.getOrElse(state.currentPlayerIndex) { Color(0xFF1565C0) }
    }

    val activeSnakeFrom  = if (state.lastEvent == GameEvent.SNAKE)  state.lastEventPosition else null
    val activeLadderFrom = if (state.lastEvent == GameEvent.LADDER) state.lastEventPosition else null

    val playerNames: List<String> = List(state.playerCount) { idx ->
        val custom = state.playerNames.getOrNull(idx)?.trim()
        when {
            !custom.isNullOrBlank()                            -> custom
            state.gameMode == GameMode.VS_COMPUTER && idx == 0 -> stringResource(R.string.player)
            state.gameMode == GameMode.VS_COMPUTER             -> stringResource(R.string.computer)
            else -> "P${idx + 1}"
        }
    }

    val topCount      = (state.playerCount + 1) / 2
    val topIndices    = (0 until topCount)
    val bottomIndices = (topCount until state.playerCount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                        color    = Color(0xFF4FC3F7)
                    )
                }
                Text(
                    text       = stringResource(R.string.game_title),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 17.sp,
                    color      = Color(0xFFECEFF1)
                )
                Spacer(modifier = Modifier.width(88.dp))
            }

            // Subtle header separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x22FFFFFF), Color.Transparent)
                        )
                    )
            )

            Spacer(Modifier.height(8.dp))

            // ── Turn chip ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(chipAccent.copy(alpha = 0.12f))
                    .border(1.5.dp, chipAccent.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(chipAccent, CircleShape)
                    )
                    Text(
                        text       = turnText,
                        color      = chipAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Top player row ────────────────────────────────────────────
            PlayerRow(
                indices     = topIndices,
                state       = state,
                playerNames = playerNames
            )

            Spacer(Modifier.height(8.dp))

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
                Spacer(Modifier.height(8.dp))
                PlayerRow(
                    indices     = bottomIndices,
                    state       = state,
                    playerNames = playerNames
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Dice ──────────────────────────────────────────────────────
            DiceSection(
                diceValue = state.diceValue,
                isRolling = state.isRolling,
                isEnabled = isDiceEnabled,
                onRoll    = { controller.rollDice() }
            )

            Spacer(Modifier.height(20.dp))
        }

        // ── Winner dialog ─────────────────────────────────────────────────
        if (state.winner != null) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = { controller.restartGame() },
                        shape   = RoundedCornerShape(10.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Text(
                            text       = stringResource(R.string.play_again),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text(
                            text  = stringResource(R.string.back_to_menu),
                            color = Color(0xFF4FC3F7)
                        )
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 40.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = stringResource(R.string.game_over),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 20.sp,
                            color      = Color(0xFFFFD700)
                        )
                    }
                },
                text = {
                    Text(
                        text      = stringResource(R.string.winner_message, state.winner ?: ""),
                        fontSize  = 16.sp,
                        textAlign = TextAlign.Center,
                        color     = Color(0xFFECEFF1)
                    )
                },
                shape             = RoundedCornerShape(24.dp),
                containerColor    = Color(0xFF111C33),
                titleContentColor = Color.White,
                textContentColor  = Color(0xFFB0BEC5)
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
    val maxPerRow = (state.playerCount + 1) / 2
    Row(modifier = Modifier.fillMaxWidth()) {
        var slot = 0
        indices.forEach { idx ->
            ProfileSection(
                name          = playerNames.getOrElse(idx) { "P${idx + 1}" },
                position      = state.positions.getOrElse(idx) { 0 },
                isActive      = state.currentPlayerIndex == idx && state.winner == null,
                tokenColor    = PLAYER_COLORS.getOrElse(idx) { Color.Gray },
                timeRemaining = if (state.currentPlayerIndex == idx) state.timeRemaining else 30,
                skipsUsed     = state.skipCounts.getOrElse(idx) { 0 },
                isEliminated  = idx in state.eliminatedPlayers,
                modifier      = Modifier.weight(1f)
            )
            slot++
        }
        repeat(maxPerRow - slot) {
            Spacer(modifier = Modifier.weight(1f))
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
            .shadow(14.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(BoardFrameGradient)
            .border(1.5.dp, BoardFrameBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        BoardCanvas(
            playerPositions  = playerPositions,
            activeSnakeFrom  = activeSnakeFrom,
            activeLadderFrom = activeLadderFrom
        )
    }
}
