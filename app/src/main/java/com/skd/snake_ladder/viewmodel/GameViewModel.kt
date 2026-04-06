package com.skd.snake_ladder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skd.snake_ladder.core.GameEngine
import com.skd.snake_ladder.core.SoundManager
import com.skd.snake_ladder.domain.model.GameEvent
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.GameState
import com.skd.snake_ladder.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val engine      = GameEngine()
    private val diceUseCase = RollDiceUseCase()
    private val soundManager = SoundManager(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    // Public entry-point called by UI — blocked during computer's turn
    fun rollDice() = rollDiceInternal(computerInitiated = false)

    private fun rollDiceInternal(computerInitiated: Boolean) {
        val current = _state.value
        if (current.isRolling || current.winner != null) return
        if (!computerInitiated && current.gameMode == GameMode.VS_COMPUTER && !current.isPlayerTurn) return

        viewModelScope.launch {

            _state.value = _state.value.copy(isRolling = true)

            soundManager.playDiceSound()
            delay(300)

            val dice     = diceUseCase.roll()
            val snapshot = _state.value

            val startPosition = if (snapshot.isPlayerTurn) snapshot.playerPosition
                                else snapshot.opponentPosition

            if (startPosition + dice > 100) {
                _state.value = snapshot.copy(
                    diceValue    = dice,
                    isRolling    = false,
                    isPlayerTurn = !snapshot.isPlayerTurn
                )
                if (_state.value.gameMode == GameMode.VS_COMPUTER &&
                    !_state.value.isPlayerTurn &&
                    _state.value.winner == null
                ) {
                    delay(800)
                    rollDiceInternal(computerInitiated = true)
                }
                return@launch
            }

            // ── Step-by-step movement ────────────────────────────────────
            var tempPosition = startPosition
            repeat(dice) {
                delay(250)
                tempPosition++
                _state.value = if (snapshot.isPlayerTurn)
                    _state.value.copy(playerPosition = tempPosition, diceValue = dice)
                else
                    _state.value.copy(opponentPosition = tempPosition, diceValue = dice)
            }

            // ── Snake / Ladder event ─────────────────────────────────────
            val isSnake  = engine.isSnakePosition(tempPosition)
            val isLadder = engine.isLadderPosition(tempPosition)

            if (isSnake || isLadder) {
                // Broadcast event so the UI can show an overlay animation
                _state.value = _state.value.copy(
                    lastEvent         = if (isSnake) GameEvent.SNAKE else GameEvent.LADDER,
                    lastEventPosition = tempPosition
                )
                delay(1200)   // hold here while the overlay is visible
            } else {
                delay(400)
            }

            // ── Apply final position ─────────────────────────────────────
            val finalPosition = engine.calculateNewPosition(tempPosition, 0)

            _state.value = if (snapshot.isPlayerTurn)
                _state.value.copy(playerPosition = finalPosition, lastEvent = null, lastEventPosition = 0)
            else
                _state.value.copy(opponentPosition = finalPosition, lastEvent = null, lastEventPosition = 0)

            // ── Winner check ─────────────────────────────────────────────
            val isWinner = engine.checkWinner(finalPosition)
            val winnerName = if (isWinner) {
                if (snapshot.isPlayerTurn)
                    if (snapshot.gameMode == GameMode.TWO_PLAYERS) "Player 1" else "Player"
                else
                    if (snapshot.gameMode == GameMode.TWO_PLAYERS) "Player 2" else "Computer"
            } else null

            if (isWinner) soundManager.playWinSound()

            _state.value = _state.value.copy(
                isRolling    = false,
                isPlayerTurn = !snapshot.isPlayerTurn,
                winner       = winnerName
            )

            // ── Auto computer turn ───────────────────────────────────────
            if (_state.value.gameMode == GameMode.VS_COMPUTER &&
                !_state.value.isPlayerTurn &&
                _state.value.winner == null
            ) {
                delay(800)
                rollDiceInternal(computerInitiated = true)
            }
        }
    }

    fun setGameMode(mode: GameMode) { _state.value = GameState(gameMode = mode) }
    fun resetGame()                  { _state.value = GameState() }

    override fun onCleared() {
        super.onCleared()
        soundManager.cleanup()
    }
}
