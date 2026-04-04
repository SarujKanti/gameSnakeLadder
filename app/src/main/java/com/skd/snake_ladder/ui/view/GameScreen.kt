package com.skd.snake_ladder.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Turn indicator text
    val turnText = when {
        state.winner != null -> stringResource(R.string.game_over)
        state.isRolling -> stringResource(R.string.rolling)
        state.isPlayerTurn -> when (state.gameMode) {
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

    // Dice is disabled during rolling or during computer's turn in VS_COMPUTER mode
    val isDiceEnabled = !state.isRolling &&
            (state.gameMode != GameMode.VS_COMPUTER || state.isPlayerTurn) &&
            state.winner == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onBack,
                modifier = Modifier.semantics {
                    contentDescription = "Go back to menu"
                }
            ) {
                Text(
                    text = stringResource(R.string.back_to_menu),
                    fontSize = 13.sp,
                    color = Color(0xFF1565C0)
                )
            }

            Text(
                text = stringResource(R.string.game_title),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A237E)
            )

            // Placeholder for symmetry
            Spacer(modifier = Modifier.width(88.dp))
        }

        // Turn indicator chip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (state.winner != null) Color(0xFFF9A825)
                    else if (state.isRolling) Color(0xFF78909C)
                    else Color(0xFF1565C0),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                text = turnText,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Player profile cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (state.gameMode) {
                GameMode.VS_COMPUTER -> {
                    ProfileSection(
                        name = stringResource(R.string.player),
                        position = state.playerPosition,
                        isActive = state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileSection(
                        name = stringResource(R.string.computer),
                        position = state.opponentPosition,
                        isActive = !state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                }
                GameMode.TWO_PLAYERS -> {
                    ProfileSection(
                        name = stringResource(R.string.player_1),
                        position = state.playerPosition,
                        isActive = state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileSection(
                        name = stringResource(R.string.player_2),
                        position = state.opponentPosition,
                        isActive = !state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Game board
        BoardCanvas(
            playerPositions = listOf(
                Pair(state.playerPosition, Color(0xFF1565C0)),
                Pair(state.opponentPosition, Color(0xFFAD1457))
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dice section
        DiceSection(
            diceValue = state.diceValue,
            isRolling = state.isRolling,
            isEnabled = isDiceEnabled,
            onRoll = { viewModel.rollDice() }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Winner dialog
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
