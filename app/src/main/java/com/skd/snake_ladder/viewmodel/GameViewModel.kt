package com.skd.snake_ladder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skd.snake_ladder.core.GameEngine
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.GameState
import com.skd.snake_ladder.domain.usecase.RollDiceUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val engine = GameEngine()
    private val diceUseCase = RollDiceUseCase()

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    fun rollDice() {

        if (_state.value.isRolling || _state.value.winner != null) return

        viewModelScope.launch {

            _state.value = _state.value.copy(isRolling = true)

            delay(300)

            val dice = diceUseCase.roll()
            val current = _state.value

            val startPosition =
                if (current.isPlayerTurn)
                    current.playerPosition
                else
                    current.opponentPosition

            val targetPosition = startPosition + dice

            if (targetPosition > 100) {
                _state.value = current.copy(
                    diceValue = dice,
                    isRolling = false
                )
                return@launch
            }

            // 🎯 Step-by-step movement
            var tempPosition = startPosition

            repeat(dice) {
                delay(250)
                tempPosition++

                _state.value =
                    if (current.isPlayerTurn)
                        _state.value.copy(
                            playerPosition = tempPosition,
                            diceValue = dice
                        )
                    else
                        _state.value.copy(
                            opponentPosition = tempPosition,
                            diceValue = dice
                        )
            }

            // 🐍 Check snake / ladder AFTER movement
            val finalPosition = engine.calculateNewPosition(tempPosition, 0)

            delay(400)

            _state.value =
                if (current.isPlayerTurn)
                    _state.value.copy(
                        playerPosition = finalPosition
                    )
                else
                    _state.value.copy(
                        opponentPosition = finalPosition
                    )

            // 🏆 Check winner
            val isWinner = engine.checkWinner(finalPosition)

            _state.value = _state.value.copy(
                isRolling = false,
                isPlayerTurn = !current.isPlayerTurn,
                winner = if (isWinner)
                    if (current.isPlayerTurn) "Player"
                    else "Computer"
                else null
            )

            // 🤖 Auto computer turn
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
}
