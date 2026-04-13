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

    private val engine       = GameEngine()
    private val diceUseCase  = RollDiceUseCase()
    private val soundManager = SoundManager(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    // ── Public API ────────────────────────────────────────────────────────

    /** Called by UI tap — blocked during computer's turn in VS_COMPUTER. */
    fun rollDice() = rollDiceInternal(computerInitiated = false)

    /**
     * Start a new game.
     * [playerCount] is 1..6; for VS_COMPUTER it is always 2 (player + computer).
     */
    fun setGameMode(mode: GameMode, playerCount: Int = 2) {
        val count = playerCount.coerceIn(2, 6)
        _state.value = GameState(
            gameMode    = mode,
            playerCount = count,
            positions   = List(count) { 0 }
        )
    }

    /** Restart the SAME mode/player-count — used by "Play Again". */
    fun restartGame() {
        val cur = _state.value
        _state.value = GameState(
            gameMode    = cur.gameMode,
            playerCount = cur.playerCount,
            positions   = List(cur.playerCount) { 0 }
        )
    }

    /** Return to mode-selection screen. */
    fun resetGame() { _state.value = GameState() }

    override fun onCleared() {
        super.onCleared()
        soundManager.cleanup()
    }

    // ── Internal roll logic ───────────────────────────────────────────────

    private fun rollDiceInternal(computerInitiated: Boolean) {
        val current = _state.value
        if (current.isRolling || current.winner != null) return
        // Block UI-initiated rolls during computer's turn in VS_COMPUTER
        if (!computerInitiated &&
            current.gameMode == GameMode.VS_COMPUTER &&
            !current.isPlayerTurn
        ) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isRolling = true)
            soundManager.playDiceSound()
            delay(300)

            val dice        = diceUseCase.roll()
            val snapshot    = _state.value
            val currentIdx  = snapshot.currentPlayerIndex
            val startPos    = snapshot.positions.getOrElse(currentIdx) { 0 }
            val nextIdx     = (currentIdx + 1) % snapshot.playerCount

            // ── Exceeds 100 → skip turn ──────────────────────────────────
            if (startPos + dice > 100) {
                _state.value = snapshot.copy(
                    diceValue          = dice,
                    isRolling          = false,
                    currentPlayerIndex = nextIdx
                )
                triggerComputerIfNeeded()
                return@launch
            }

            // ── Step-by-step movement ────────────────────────────────────
            var tempPos = startPos
            repeat(dice) {
                delay(250)
                tempPos++
                _state.value = _state.value.copy(
                    positions = _state.value.positions.updatedAt(currentIdx, tempPos),
                    diceValue = dice
                )
            }

            // ── Snake / Ladder detection ─────────────────────────────────
            val isSnake  = engine.isSnakePosition(tempPos)
            val isLadder = engine.isLadderPosition(tempPos)
            if (isSnake || isLadder) {
                _state.value = _state.value.copy(
                    lastEvent         = if (isSnake) GameEvent.SNAKE else GameEvent.LADDER,
                    lastEventPosition = tempPos
                )
                delay(1200)
            } else {
                delay(400)
            }

            // ── Apply final position (snake/ladder jump) ─────────────────
            val finalPos = engine.calculateNewPosition(tempPos, 0)
            _state.value = _state.value.copy(
                positions         = _state.value.positions.updatedAt(currentIdx, finalPos),
                lastEvent         = null,
                lastEventPosition = 0
            )

            // ── Winner check ─────────────────────────────────────────────
            val isWinner   = engine.checkWinner(finalPos)
            val winnerName = if (isWinner) playerNameFor(snapshot, currentIdx) else null
            if (isWinner) soundManager.playWinSound()

            _state.value = _state.value.copy(
                isRolling          = false,
                currentPlayerIndex = nextIdx,
                winner             = winnerName
            )

            triggerComputerIfNeeded()
        }
    }

    /** Auto-start computer's roll in VS_COMPUTER mode if it's the computer's turn. */
    private suspend fun triggerComputerIfNeeded() {
        val s = _state.value
        if (s.gameMode == GameMode.VS_COMPUTER &&
            !s.isPlayerTurn &&
            s.winner == null
        ) {
            delay(800)
            rollDiceInternal(computerInitiated = true)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun playerNameFor(state: GameState, idx: Int): String = when {
        state.gameMode == GameMode.VS_COMPUTER && idx == 0 -> "Player"
        state.gameMode == GameMode.VS_COMPUTER             -> "Computer"
        else -> "Player ${idx + 1}"
    }

    /** Returns a new list with the element at [index] replaced by [value]. */
    private fun List<Int>.updatedAt(index: Int, value: Int): List<Int> =
        toMutableList().also { it[index] = value }
}
