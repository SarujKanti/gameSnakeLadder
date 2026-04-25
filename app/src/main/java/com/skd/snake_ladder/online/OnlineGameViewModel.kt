package com.skd.snake_ladder.online

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.skd.snake_ladder.core.GameEngine
import com.skd.snake_ladder.core.SoundManager
import com.skd.snake_ladder.domain.model.GameEvent
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.GameState
import com.skd.snake_ladder.domain.usecase.RollDiceUseCase
import com.skd.snake_ladder.viewmodel.GameController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// ── UI state for the lobby / waiting room ─────────────────────────────────────
sealed class OnlineUiState {
    object Idle : OnlineUiState()
    object Loading : OnlineUiState()
    data class WaitingRoom(
        val roomCode: String,
        val joinedNames: List<String>,
        val totalNeeded: Int,
        val isHost: Boolean
    ) : OnlineUiState()
    object InGame : OnlineUiState()
    data class Error(val message: String) : OnlineUiState()
}

class OnlineGameViewModel(application: Application) : AndroidViewModel(application), GameController {

    private val repository   = OnlineRepository()
    private val settings     = com.skd.snake_ladder.data.SettingsRepository(application)
    private val engine       = GameEngine(settings.selectedBoard)
    private val soundManager = SoundManager(application)
    private val diceUseCase  = RollDiceUseCase()

    override val boardConfig: com.skd.snake_ladder.data.BoardConfig get() = engine.config

    private val _state    = MutableStateFlow(GameState())
    override val state: StateFlow<GameState> = _state

    private val _uiState  = MutableStateFlow<OnlineUiState>(OnlineUiState.Idle)
    val uiState: StateFlow<OnlineUiState> = _uiState

    /**
     * Non-null when an opponent has left a 2-player game.
     * The value is the name of the player who left.
     * Collect in the UI and reset with [dismissOpponentLeft].
     */
    private val _opponentLeftEvent = MutableStateFlow<String?>(null)
    val opponentLeftEvent: StateFlow<String?> = _opponentLeftEvent

    fun dismissOpponentLeft() { _opponentLeftEvent.value = null }

    private var roomCode        = ""
    private var myPlayerId      = ""
    private var myPlayerIndex   = -1
    private var isHost          = false
    private var timerJob: Job?       = null
    private var opponentRollJob: Job? = null
    private var observeJob: Job?      = null

    init {
        myPlayerId = getOrCreatePlayerId(application)
    }

    // ── Create / Join ─────────────────────────────────────────────────────────

    fun createRoom(playerName: String, playerCount: Int) {
        isHost = true
        _uiState.value = OnlineUiState.Loading
        viewModelScope.launch {
            val code = generateRoomCode()
            val ok   = repository.createRoom(code, myPlayerId, playerName.ifBlank { "Host" }, playerCount)
            if (ok) {
                roomCode      = code
                myPlayerIndex = 0
                startObserving()
            } else {
                _uiState.value = OnlineUiState.Error("Could not create room. Check your internet connection.")
            }
        }
    }

    fun joinRoom(code: String, playerName: String) {
        isHost = false
        _uiState.value = OnlineUiState.Loading
        viewModelScope.launch {
            val trimmed      = code.uppercase().trim()
            val (ok, errMsg) = repository.joinRoom(trimmed, myPlayerId, playerName.ifBlank { "Player" })
            if (ok) {
                roomCode = trimmed
                startObserving()
            } else {
                _uiState.value = OnlineUiState.Error(errMsg.ifBlank { "Could not join room." })
            }
        }
    }

    fun startOnlineGame() {
        if (!isHost) return
        viewModelScope.launch { repository.startGame(roomCode) }
    }

    fun resetToIdle() {
        _uiState.value = OnlineUiState.Idle
    }

    // ── Observe Firebase ──────────────────────────────────────────────────────

    private fun startObserving() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            repository.observeRoom(roomCode, myPlayerId).collect { snap ->
                if (snap == null) return@collect
                myPlayerIndex = snap.myPlayerIndex

                when (snap.status) {
                    "waiting" -> {
                        _uiState.value = OnlineUiState.WaitingRoom(
                            roomCode    = snap.roomCode,
                            joinedNames = snap.playerNames.take(snap.joinedCount),
                            totalNeeded = snap.playerCount,
                            isHost      = snap.hostId == myPlayerId
                        )
                    }
                    "playing" -> {
                        val prev      = _state.value
                        _state.value  = snap.gameState
                        _uiState.value = OnlineUiState.InGame

                        val isMyTurn        = snap.gameState.currentPlayerIndex == myPlayerIndex
                        val turnJustChanged = prev.currentPlayerIndex != snap.gameState.currentPlayerIndex
                        val isRolling       = snap.gameState.isRolling
                        val hasWinner       = snap.gameState.winner != null

                        // ── Detect players who just disconnected ──────────
                        val justLeft = snap.gameState.disconnectedPlayers - prev.disconnectedPlayers
                        justLeft.filter { it != myPlayerIndex }.forEach { idx ->
                            handlePlayerLeft(idx, snap.gameState)
                        }

                        // ── Opponent dice animation ───────────────────────
                        if (isRolling && !isMyTurn) {
                            startOpponentRollAnimation()
                        } else {
                            opponentRollJob?.cancel()
                        }

                        // ── Timer on ALL devices ──────────────────────────
                        if (!isRolling && !hasWinner &&
                            (turnJustChanged || timerJob?.isActive != true)) {
                            startTurnTimer(snap.gameState.currentPlayerIndex)
                        }
                        if (isRolling || hasWinner) {
                            timerJob?.cancel()
                        }
                    }
                }
            }
        }
    }

    // ── Roll dice (current player only) ───────────────────────────────────────

    override fun rollDice() {
        val cur = _state.value
        if (cur.isRolling || cur.isMoving || cur.winner != null) return
        if (cur.currentPlayerIndex != myPlayerIndex)            return
        if (myPlayerIndex in cur.eliminatedPlayers)             return

        timerJob?.cancel()

        viewModelScope.launch {

            // ── Phase 1: Dice spinning — broadcast to all players ─────────
            val rolling = cur.copy(isRolling = true, timeRemaining = 30)
            _state.value = rolling
            repository.updateGameState(roomCode, rolling)

            soundManager.playDiceSound()
            delay(800)

            if (_state.value.gameMode == null) return@launch

            val snap       = _state.value
            val dice       = diceUseCase.roll()
            val currentIdx = snap.currentPlayerIndex
            val startPos   = snap.positions.getOrElse(currentIdx) { 0 }
            val nextIdx    = nextActivePlayer(currentIdx, snap.playerCount,
                                snap.eliminatedPlayers + snap.disconnectedPlayers)

            // ── Phase 2: Dice settles — LOCAL only ────────────────────────
            // Firebase still shows isRolling=true so opponents keep spinning.
            // On this device the dice face shows the real value and bounces.
            _state.value = snap.copy(isRolling = false, diceValue = dice)
            delay(550)

            if (_state.value.gameMode == null) return@launch

            // Exceeds 100 → skip turn
            if (startPos + dice > 100) {
                val newState = snap.copy(
                    diceValue          = dice,
                    isRolling          = false,
                    currentPlayerIndex = nextIdx,
                    timeRemaining      = 30
                )
                _state.value = newState
                repository.updateGameState(roomCode, newState)
                return@launch
            }

            // ── Phase 3: Token moves step-by-step (local only) ───────────
            _state.value = _state.value.copy(isMoving = true)
            var tempPos = startPos
            repeat(dice) {
                delay(200)
                if (_state.value.gameMode == null || _state.value.positions.size <= currentIdx) return@repeat
                tempPos++
                _state.value = _state.value.copy(
                    positions = _state.value.positions.updatedAt(currentIdx, tempPos)
                )
            }
            if (_state.value.gameMode == null || _state.value.positions.size <= currentIdx) return@launch

            // Snake / Ladder highlight + sound
            val isSnake  = engine.isSnakePosition(tempPos)
            val isLadder = engine.isLadderPosition(tempPos)
            if (isSnake || isLadder) {
                _state.value = _state.value.copy(
                    lastEvent         = if (isSnake) GameEvent.SNAKE else GameEvent.LADDER,
                    lastEventPosition = tempPos
                )
                if (isSnake) soundManager.playSnakeSound() else soundManager.playLadderSound()
                delay(1200)
            } else {
                delay(300)
            }
            if (_state.value.gameMode == null || _state.value.positions.size <= currentIdx) return@launch

            // ── Phase 4: Finalise — single Firebase write ─────────────────
            val finalPos   = engine.calculateNewPosition(tempPos, 0)
            val isWinner   = engine.checkWinner(finalPos)
            val winnerName = if (isWinner) snap.playerNames.getOrElse(currentIdx) { "P${currentIdx + 1}" } else null
            if (isWinner) soundManager.playWinSound()

            val newState = _state.value.copy(
                positions          = _state.value.positions.updatedAt(currentIdx, finalPos),
                lastEvent          = null,
                lastEventPosition  = 0,
                isMoving           = false,
                isRolling          = false,
                currentPlayerIndex = nextIdx,
                winner             = winnerName,
                timeRemaining      = 30
            )
            _state.value = newState
            repository.updateGameState(roomCode, newState)
        }
    }

    // ── Turn timer (runs on every device) ────────────────────────────────────

    /**
     * Starts a 30-second local countdown for [activePlayerIndex]'s turn.
     * All devices run this so the bar animates everywhere.
     * Only the device whose [myPlayerIndex] matches [activePlayerIndex]
     * actually writes the timeout result to Firebase.
     */
    private fun startTurnTimer(activePlayerIndex: Int) {
        timerJob?.cancel()
        val snap = _state.value
        if (snap.winner != null) return
        _state.value = snap.copy(timeRemaining = 30)

        timerJob = viewModelScope.launch {
            for (t in 29 downTo 0) {
                delay(1000)
                val cur = _state.value
                if (cur.isRolling || cur.winner != null) return@launch
                // Stop if the turn has already moved on (e.g. opponent rolled quickly)
                if (cur.currentPlayerIndex != activePlayerIndex) return@launch
                _state.value = cur.copy(timeRemaining = t)
            }
            // Only the active player's device commits the timeout to Firebase
            if (activePlayerIndex == myPlayerIndex) {
                onTimeout()
            }
        }
    }

    // ── Opponent dice animation ───────────────────────────────────────────────

    /**
     * Cycles random dice face values locally while Firebase reports isRolling=true
     * for an opponent's turn, so all watching devices see a rolling animation.
     */
    private fun startOpponentRollAnimation() {
        if (opponentRollJob?.isActive == true) return   // already running
        opponentRollJob = viewModelScope.launch {
            while (true) {
                delay(120)
                val cur = _state.value
                if (!cur.isRolling) break
                _state.value = cur.copy(diceValue = (1..6).random())
            }
        }
    }

    private suspend fun onTimeout() {
        val s = _state.value
        if (s.winner != null || s.isRolling) return
        if (s.currentPlayerIndex != myPlayerIndex) return
        // Safety: don't time out a player who already disconnected
        if (myPlayerIndex in s.disconnectedPlayers) return

        val idx      = s.currentPlayerIndex
        val newSkips = s.skipCounts.toMutableList().also { if (idx < it.size) it[idx]++ }
        val newElim  = s.eliminatedPlayers.toMutableSet()
        var winner: String? = null

        if (newSkips.getOrElse(idx) { 0 } >= 3) {
            // For 2-player, the remaining active (non-disconnected) opponent wins
            val remaining = (0 until s.playerCount)
                .filter { it != idx && it !in newElim && it !in s.disconnectedPlayers }
            if (s.playerCount == 2 || remaining.size == 1) {
                winner = remaining.firstOrNull()?.let { s.playerNames.getOrElse(it) { "P${it + 1}" } }
                if (winner != null) soundManager.playWinSound()
            } else {
                newElim.add(idx)
                val active = (0 until s.playerCount)
                    .filter { it !in newElim && it !in s.disconnectedPlayers }
                if (active.size <= 1) {
                    winner = active.firstOrNull()?.let { s.playerNames.getOrElse(it) { "P${it + 1}" } }
                    if (winner != null) soundManager.playWinSound()
                }
            }
        }

        val skipAll = newElim + s.disconnectedPlayers
        val nextIdx = if (winner != null) idx else nextActivePlayer(idx, s.playerCount, skipAll)
        val newState = s.copy(
            skipCounts         = newSkips,
            eliminatedPlayers  = newElim,
            currentPlayerIndex = nextIdx,
            timeRemaining      = 30,
            winner             = winner
        )
        _state.value = newState
        repository.updateGameState(roomCode, newState)
    }

    // ── Handle player disconnection ───────────────────────────────────────────

    private fun handlePlayerLeft(idx: Int, gs: GameState) {
        val skipAll = gs.eliminatedPlayers + gs.disconnectedPlayers   // idx already in disconnectedPlayers
        val activePlayers = (0 until gs.playerCount).filter { it !in skipAll }

        if (gs.playerCount == 2) {
            // Show "opponent left" dialog — remaining player can return to menu
            val name = gs.playerNames.getOrElse(idx) { "Player ${idx + 1}" }
            _opponentLeftEvent.value = name
            return
        }

        // 3+ players: determine if only 1 remains → declare winner
        if (activePlayers.size == 1) {
            val winnerId   = activePlayers[0]
            val winnerName = gs.playerNames.getOrElse(winnerId) { "P${winnerId + 1}" }
            soundManager.playWinSound()
            val newState = gs.copy(winner = winnerName, currentPlayerIndex = winnerId)
            _state.value = newState
            // Acting leader (lowest active index) writes to Firebase
            if (myPlayerIndex == activePlayers.minOrNull()) {
                viewModelScope.launch { repository.updateGameState(roomCode, newState) }
            }
            return
        }

        // Advance the turn if the leaving player was the current player
        if (gs.currentPlayerIndex == idx) {
            val nextIdx  = nextActivePlayer(idx, gs.playerCount, skipAll)
            val newState = gs.copy(currentPlayerIndex = nextIdx, timeRemaining = 30)
            _state.value = newState
            if (myPlayerIndex == activePlayers.minOrNull()) {
                viewModelScope.launch { repository.updateGameState(roomCode, newState) }
            }
        }
    }

    // ── Restart / Exit ────────────────────────────────────────────────────────

    override fun restartGame() {
        if (!isHost) return
        val cur = _state.value
        viewModelScope.launch {
            val newState = GameState(
                gameMode          = GameMode.ONLINE,
                playerCount       = cur.playerCount,
                positions         = List(cur.playerCount) { 0 },
                skipCounts        = List(cur.playerCount) { 0 },
                eliminatedPlayers = emptySet(),
                timeRemaining     = 30,
                playerNames       = cur.playerNames,
                myPlayerIndex     = myPlayerIndex
            )
            _state.value = newState
            repository.updateGameState(roomCode, newState)
        }
    }

    fun exitToMenu() {
        timerJob?.cancel()
        opponentRollJob?.cancel()
        observeJob?.cancel()
        viewModelScope.launch {
            if (myPlayerIndex >= 0 && roomCode.isNotBlank()) {
                repository.markDisconnected(roomCode, myPlayerIndex)
            }
        }
        _state.value  = GameState()
        _uiState.value = OnlineUiState.Idle
        roomCode      = ""
        myPlayerIndex = -1
        isHost        = false
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        opponentRollJob?.cancel()
        observeJob?.cancel()
        soundManager.cleanup()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun nextActivePlayer(current: Int, count: Int, eliminated: Set<Int>): Int {
        var next = (current + 1) % count
        repeat(count) {
            if (next !in eliminated) return next
            next = (next + 1) % count
        }
        return current
    }

    private fun List<Int>.updatedAt(index: Int, value: Int): List<Int> =
        toMutableList().also { if (index < size) it[index] = value }

    private fun generateRoomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private fun getOrCreatePlayerId(context: Context): String {
        val prefs = context.getSharedPreferences("snake_ladder_prefs", Context.MODE_PRIVATE)
        return prefs.getString("player_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("player_id", it).apply()
        }
    }
}
