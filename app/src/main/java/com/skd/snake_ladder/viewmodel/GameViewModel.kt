package com.skd.snake_ladder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skd.snake_ladder.core.GameEngine
import com.skd.snake_ladder.core.SoundManager
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.GameState
import com.skd.snake_ladder.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = GameEngine()
    private val diceUseCase = RollDiceUseCase()
    private val soundManager = SoundManager(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    fun rollDice() {
        val current = _state.value
        if (current.isRolling || current.winner != null) return
        // Block player input during computer's turn
        if (current.gameMode == GameMode.VS_COMPUTER && !current.isPlayerTurn) return

        viewModelScope.launch {

            _state.value = _state.value.copy(isRolling = true)

            soundManager.playDiceSound()
            delay(300)

            val dice = diceUseCase.roll()
            val snapshot = _state.value

            val startPosition =
                if (snapshot.isPlayerTurn) snapshot.playerPosition
                else snapshot.opponentPosition

            val targetPosition = startPosition + dice

            if (targetPosition > 100) {
                _state.value = snapshot.copy(
                    diceValue = dice,
                    isRolling = false
                )
                return@launch
            }

            // Step-by-step movement animation
            var tempPosition = startPosition

            repeat(dice) {
                delay(250)
                tempPosition++

                _state.value =
                    if (snapshot.isPlayerTurn)
                        _state.value.copy(playerPosition = tempPosition, diceValue = dice)
                    else
                        _state.value.copy(opponentPosition = tempPosition, diceValue = dice)
            }

            // Apply snake or ladder after movement
            val finalPosition = engine.calculateNewPosition(tempPosition, 0)

            delay(400)

            _state.value =
                if (snapshot.isPlayerTurn)
                    _state.value.copy(playerPosition = finalPosition)
                else
                    _state.value.copy(opponentPosition = finalPosition)

            // Determine winner name (fix for TWO_PLAYERS mode)
            val isWinner = engine.checkWinner(finalPosition)
            val winnerName = if (isWinner) {
                if (snapshot.isPlayerTurn) {
                    if (snapshot.gameMode == GameMode.TWO_PLAYERS) "Player 1" else "Player"
                } else {
                    if (snapshot.gameMode == GameMode.TWO_PLAYERS) "Player 2" else "Computer"
                }
            } else null

            if (isWinner) soundManager.playWinSound()

            _state.value = _state.value.copy(
                isRolling = false,
                isPlayerTurn = !snapshot.isPlayerTurn,
                winner = winnerName
            )

            // Auto computer turn in VS_COMPUTER mode
            if (_state.value.gameMode == GameMode.VS_COMPUTER &&
                !_state.value.isPlayerTurn &&
                _state.value.winner == null
            ) {
                delay(800)
                rollDice()
            }
        }
    }

    fun setGameMode(mode: GameMode) {
        _state.value = GameState(gameMode = mode)
    }

    fun resetGame() {
        _state.value = GameState()
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.cleanup()
    }
}
