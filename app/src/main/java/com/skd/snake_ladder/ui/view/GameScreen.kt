package com.skd.snake_ladder.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.*
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
import com.skd.snake_ladder.data.BoardColorScheme
import com.skd.snake_ladder.data.BoardConfig
import com.skd.snake_ladder.domain.model.GameEvent
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.ui.theme.LocalAppColors
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    controller: GameController,
    onBack: () -> Unit = {}
) {
    val colors          = LocalAppColors.current
    val state           by controller.state.collectAsStateWithLifecycle()
    val boardConfig      = controller.boardConfig
    val boardColorScheme = controller.boardColorScheme

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
            !state.isMoving &&
            state.winner == null &&
            (state.gameMode != GameMode.VS_COMPUTER || state.isPlayerTurn) &&
            (state.gameMode != GameMode.ONLINE || state.isPlayerTurn) &&
            state.currentPlayerIndex !in state.eliminatedPlayers

    val chipAccent = when {
        state.winner != null              -> Color(0xFFFFD700)
        state.isRolling || state.isMoving -> Color(0xFF546E7A)
        else -> PLAYER_COLORS.getOrElse(state.currentPlayerIndex) { colors.accent }
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

    val myIdx           = state.myPlayerIndex
    val opponentIndices = (0 until state.playerCount).filter { it != myIdx }
    val opponentRows    = opponentIndices.chunked(3)
    val maxOppPerRow    = opponentRows.maxOfOrNull { it.size } ?: 1

    val dialogBg = if (colors.isDark) Color(0xFF111C33) else Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                val cdBack = stringResource(R.string.cd_back)
                // Back button — pill-shaped tinted button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.accent.copy(alpha = if (colors.isDark) 0.12f else 0.10f))
                        .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .clickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick           = onBack
                        )
                        .semantics { contentDescription = cdBack }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text       = "← ${stringResource(R.string.back_to_menu)}",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = colors.accent
                    )
                }
                // Gradient title
                Text(
                    text = stringResource(R.string.game_title),
                    style = TextStyle(
                        brush      = Brush.linearGradient(
                            if (colors.isDark)
                                listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC))
                            else
                                listOf(Color(0xFF1356B4), Color(0xFF0277BD))
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Subtle header separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, colors.divider, Color.Transparent)
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
                    verticalAlignment     = Alignment.CenterVertically,
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

            // ── Opponent rows (above board) ───────────────────────────────
            opponentRows.forEach { chunk ->
                PlayerRow(
                    indices     = chunk,
                    state       = state,
                    playerNames = playerNames,
                    maxPerRow   = maxOppPerRow
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(2.dp))

            // ── Board ─────────────────────────────────────────────────────
            BoardWithOverlay(
                playerPositions  = state.positions.mapIndexed { idx, pos ->
                    Pair(pos, PLAYER_COLORS.getOrElse(idx) { Color.Gray })
                },
                activeSnakeFrom  = activeSnakeFrom,
                activeLadderFrom = activeLadderFrom,
                boardConfig      = boardConfig,
                colorScheme      = boardColorScheme
            )

            Spacer(Modifier.height(6.dp))

            // ── My player card (below board) ──────────────────────────────
            PlayerRow(
                indices     = listOf(myIdx),
                state       = state,
                playerNames = playerNames,
                maxPerRow   = 1
            )

            Spacer(Modifier.height(12.dp))

            // ── Dice ──────────────────────────────────────────────────────
            DiceSection(
                diceValue    = state.diceValue,
                isRolling    = state.isRolling,
                isEnabled    = isDiceEnabled,
                waitingLabel = when {
                    state.isMoving                                           -> "Moving..."
                    state.gameMode == GameMode.ONLINE && !state.isPlayerTurn -> "Opponent's Turn"
                    else                                                     -> null
                },
                onRoll       = { controller.rollDice() }
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
                            color = colors.accent
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
                        color     = colors.textPrimary
                    )
                },
                shape             = RoundedCornerShape(24.dp),
                containerColor    = dialogBg,
                titleContentColor = colors.textPrimary,
                textContentColor  = colors.textSecondary
            )
        }
    }
}

// ── Player card row ───────────────────────────────────────────────────────────
@Composable
private fun PlayerRow(
    indices: List<Int>,
    state: com.skd.snake_ladder.domain.model.GameState,
    playerNames: List<String>,
    maxPerRow: Int = indices.size
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        indices.forEach { idx ->
            ProfileSection(
                name           = playerNames.getOrElse(idx) { "P${idx + 1}" },
                position       = state.positions.getOrElse(idx) { 0 },
                isActive       = state.currentPlayerIndex == idx && state.winner == null,
                tokenColor     = PLAYER_COLORS.getOrElse(idx) { Color.Gray },
                timeRemaining  = if (state.currentPlayerIndex == idx) state.timeRemaining else 30,
                skipsUsed      = state.skipCounts.getOrElse(idx) { 0 },
                isEliminated   = idx in state.eliminatedPlayers,
                isDisconnected = idx in state.disconnectedPlayers,
                modifier       = Modifier.weight(1f)
            )
        }
        // Fill remaining slots so cards keep equal width across rows
        repeat(maxPerRow - indices.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ── Board frame + canvas ──────────────────────────────────────────────────────
@Composable
private fun BoardWithOverlay(
    playerPositions: List<Pair<Int, Color>>,
    activeSnakeFrom: Int?,
    activeLadderFrom: Int?,
    boardConfig: BoardConfig,
    colorScheme: BoardColorScheme
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
            activeLadderFrom = activeLadderFrom,
            boardConfig      = boardConfig,
            colorScheme      = colorScheme
        )
    }
}
