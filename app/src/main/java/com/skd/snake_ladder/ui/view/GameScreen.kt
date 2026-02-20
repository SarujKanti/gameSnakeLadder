package com.skd.snake_ladder.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 👥 Profile Section

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            when (state.gameMode) {
                GameMode.VS_COMPUTER -> {
                    ProfileSection(
                        name = "👤 Player",
                        position = state.playerPosition,
                        isActive = state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )

                    ProfileSection(
                        name = "🤖 Computer",
                        position = state.opponentPosition,
                        isActive = !state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                }

                GameMode.TWO_PLAYERS -> {
                    ProfileSection(
                        name = "👤 Player 1",
                        position = state.playerPosition,
                        isActive = state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )

                    ProfileSection(
                        name = "👤 Player 2",
                        position = state.opponentPosition,
                        isActive = !state.isPlayerTurn,
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    // Optional: Nothing if mode not selected yet
                }
            }
        }


        Spacer(modifier = Modifier.height(20.dp))

        BoardCanvas(
            playerPositions = listOf(
                Pair(state.playerPosition, Color.Blue),
                Pair(state.opponentPosition, Color.Magenta)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        DiceSection(
            diceValue = state.diceValue,
            isRolling = state.isRolling,
            onRoll = { viewModel.rollDice() }
        )
        if (state.winner != null) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(onClick = { viewModel.resetGame() }) {
                    Text("Play Again")
                }
            },
            title = { Text("Game Over") },
            text = { Text("🏆 ${state.winner} Wins!") }
        )
    }
    }

}
