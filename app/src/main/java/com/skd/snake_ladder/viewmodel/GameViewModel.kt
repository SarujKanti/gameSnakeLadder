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
import kotlinx.coroutines.Job
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

    private var timerJob: Job? = null

    /** Snapshot saved when user backs out of a multiplayer game in progress. */
    private var savedMultiplayerState: GameState? = null

    // ── Public API ────────────────────────────────────────────────────────

    fun rollDice() = rollDiceInternal(computerInitiated = false)

    fun setGameMode(mode: GameMode, playerCount: Int = 2, playerNames: List<String> = emptyList()) {
        savedMultiplayerState = null          // starting fresh always clears the cache
        val count = playerCount.coerceIn(2, 6)
        _state.value = GameState(
            gameMode          = mode,
            playerCount       = count,
            positions         = List(count) { 0 },
            skipCounts        = List(count) { 0 },
            eliminatedPlayers = emptySet(),
            timeRemaining     = 30,
            playerNames       = playerNames.map { it.trim() }
        )
        startTurnTimer()
    }

    fun restartGame() {
        timerJob?.cancel()
        val cur = _state.value
        _state.value = GameState(
            gameMode          = cur.gameMode,
            playerCount       = cur.playerCount,
            positions         = List(cur.playerCount) { 0 },
            skipCounts        = List(cur.playerCount) { 0 },
            eliminatedPlayers = emptySet(),
            timeRemaining     = 30,
            playerNames       = cur.playerNames
        )
        startTurnTimer()
    }

    /**
     * Called when the user navigates back to the menu.
     * Saves the current multiplayer game if it is still in progress.
     */
    fun exitToMenu() {
        timerJob?.cancel()
        val cur = _state.value
        if (cur.gameMode == GameMode.MULTI_PLAYER &&
            cur.winner == null &&
            cur.positions.any { it > 0 }
        ) {
            savedMultiplayerState = cur
        }
        _state.value = GameState()
    }

    /** Returns true if there is a saved multiplayer game with exactly [playerCount] players. */
    fun hasSavedGameForCount(playerCount: Int): Boolean =
        savedMultiplayerState?.let {
            it.playerCount == playerCount && it.winner == null
        } ?: false

    /** Restores the cached multiplayer game and resumes the turn timer. */
    fun resumeSavedGame() {
        val saved = savedMultiplayerState ?: return
        savedMultiplayerState = null
        _state.value = saved
        startTurnTimer()
    }

    fun resetGame() {
        timerJob?.cancel()
        savedMultiplayerState = null
        _state.value = GameState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.cleanup()
    }

    // ── Turn timer ────────────────────────────────────────────────────────

    private fun startTurnTimer() {
        timerJob?.cancel()
        val snap = _state.value
        // Computer never times out
        if (snap.gameMode == GameMode.VS_COMPUTER && !snap.isPlayerTurn) return
        if (snap.winner != null) return

        _state.value = snap.copy(timeRemaining = 30)

        timerJob = viewModelScope.launch {
            for (t in 29 downTo 0) {
                delay(1000)
                val cur = _state.value
                // Stop if rolling, game over, or turn already changed
                if (cur.isRolling || cur.winner != null) return@launch
                if (cur.currentPlayerIndex != snap.currentPlayerIndex) return@launch
                _state.value = cur.copy(timeRemaining = t)
            }
            onTimeout()
        }
    }

    private fun onTimeout() {
        val s = _state.value
        if (s.winner != null || s.isRolling) return

        val idx       = s.currentPlayerIndex
        val newSkips  = s.skipCounts.toMutableList().also {
            if (idx < it.size) it[idx]++
        }
        val newElim   = s.eliminatedPlayers.toMutableSet()
        var winner: String? = null

        if (newSkips.getOrElse(idx) { 0 } >= 3) {
            if (s.playerCount == 2) {
                // Other player wins immediately
                winner = playerNameFor(s, 1 - idx)
                soundManager.playWinSound()
            } else {
                // Eliminate this player
                newElim.add(idx)
                val active = (0 until s.playerCount).filter { it !in newElim }
                if (active.size <= 1) {
                    winner = active.firstOrNull()?.let { playerNameFor(s, it) }
                    if (winner != null) soundManager.playWinSound()
                }
            }
        }

        val nextIdx = if (winner != null) idx
                      else nextActivePlayer(idx, s.playerCount, newElim)

        _state.value = s.copy(
            skipCounts         = newSkips,
            eliminatedPlayers  = newElim,
            currentPlayerIndex = nextIdx,
            timeRemaining      = 30,
            winner             = winner
        )

        if (winner == null) {
            viewModelScope.launch { triggerComputerIfNeeded() }
        }
    }

    // ── Dice roll ─────────────────────────────────────────────────────────

    private fun rollDiceInternal(computerInitiated: Boolean) {
        val current = _state.value
        if (current.isRolling || current.winner != null) return
        if (!computerInitiated &&
            current.gameMode == GameMode.VS_COMPUTER &&
            !current.isPlayerTurn) return
        if (!computerInitiated &&
            current.currentPlayerIndex in current.eliminatedPlayers) return

        // Cancel timer the moment the player taps
        timerJob?.cancel()

        viewModelScope.launch {
            _state.value = _state.value.copy(isRolling = true, timeRemaining = 30)
            soundManager.playDiceSound()
            delay(300)

            val dice       = diceUseCase.roll()
            val snapshot   = _state.value
            val currentIdx = snapshot.currentPlayerIndex
            val startPos   = snapshot.positions.getOrElse(currentIdx) { 0 }
            val nextIdx    = nextActivePlayer(currentIdx, snapshot.playerCount, snapshot.eliminatedPlayers)

            // Exceeds 100 → skip turn (no movement)
            if (startPos + dice > 100) {
                _state.value = snapshot.copy(
                    diceValue          = dice,
                    isRolling          = false,
                    currentPlayerIndex = nextIdx,
                    timeRemaining      = 30
                )
                triggerComputerIfNeeded()
                return@launch
            }

            // Step-by-step movement
            var tempPos = startPos
            repeat(dice) {
                delay(250)
                tempPos++
                _state.value = _state.value.copy(
                    positions = _state.value.positions.updatedAt(currentIdx, tempPos),
                    diceValue = dice
                )
            }

            // Snake / Ladder
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

            // Final position
            val finalPos      = engine.calculateNewPosition(tempPos, 0)
            val isWinner      = engine.checkWinner(finalPos)
            val winnerName    = if (isWinner) playerNameFor(snapshot, currentIdx) else null
            if (isWinner) soundManager.playWinSound()

            _state.value = _state.value.copy(
                positions          = _state.value.positions.updatedAt(currentIdx, finalPos),
                lastEvent          = null,
                lastEventPosition  = 0,
                isRolling          = false,
                currentPlayerIndex = nextIdx,
                winner             = winnerName,
                timeRemaining      = 30
            )

            triggerComputerIfNeeded()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private suspend fun triggerComputerIfNeeded() {
        val s = _state.value
        if (s.winner != null) return
        if (s.gameMode == GameMode.VS_COMPUTER && !s.isPlayerTurn) {
            delay(800)
            rollDiceInternal(computerInitiated = true)
        } else {
            startTurnTimer()
        }
    }

    private fun playerNameFor(state: GameState, idx: Int): String {
        val custom = state.playerNames.getOrNull(idx)?.trim()
        if (!custom.isNullOrBlank()) return custom
        return when {
            state.gameMode == GameMode.VS_COMPUTER && idx == 0 -> "Player"
            state.gameMode == GameMode.VS_COMPUTER             -> "Computer"
            else -> "P${idx + 1}"
        }
    }

    /** Returns the next non-eliminated player index after [current]. */
    private fun nextActivePlayer(current: Int, count: Int, eliminated: Set<Int>): Int {
        var next = (current + 1) % count
        repeat(count) {
            if (next !in eliminated) return next
            next = (next + 1) % count
        }
        return current
    }

    private fun List<Int>.updatedAt(index: Int, value: Int): List<Int> =
        toMutableList().also { it[index] = value }
}
