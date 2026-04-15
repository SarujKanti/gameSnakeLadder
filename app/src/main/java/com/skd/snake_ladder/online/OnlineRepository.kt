package com.skd.snake_ladder.online

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skd.snake_ladder.domain.model.GameEvent
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.GameState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// ── Snapshot returned to the ViewModel on every Firebase update ───────────────
data class RoomSnapshot(
    val roomCode: String,
    val hostId: String,
    val playerCount: Int,
    val status: String,          // "waiting" | "playing"
    val joinedCount: Int,        // how many players have actually joined
    val playerNames: List<String>,
    val playerIds: List<String>,
    val gameState: GameState,
    val myPlayerIndex: Int
)

class OnlineRepository {

    private val rooms = Firebase.database.reference.child("rooms")

    // ── Create ────────────────────────────────────────────────────────────────

    suspend fun createRoom(
        roomCode: String,
        hostId: String,
        hostName: String,
        playerCount: Int
    ): Boolean = runCatching {
        val data = mapOf(
            "hostId"      to hostId,
            "playerCount" to playerCount,
            "status"      to "waiting",
            "players"     to mapOf(
                "0" to mapOf("id" to hostId, "name" to hostName, "connected" to true)
            ),
            "gameState"   to emptyGameStateMap(playerCount)
        )
        rooms.child(roomCode).setValue(data).await()
    }.isSuccess

    // ── Join ──────────────────────────────────────────────────────────────────

    suspend fun joinRoom(
        roomCode: String,
        playerId: String,
        playerName: String
    ): Pair<Boolean, String> = runCatching {
        val snap        = rooms.child(roomCode).get().await()
        if (!snap.exists())                             return Pair(false, "Room not found")
        val status      = snap.child("status").getValue(String::class.java)
        if (status != "waiting")                        return Pair(false, "Game already started")
        val playerCount = snap.child("playerCount").getValue(Int::class.java) ?: 2
        val joined      = snap.child("players").childrenCount.toInt()
        if (joined >= playerCount)                      return Pair(false, "Room is full")
        // Check if already in room (re-join case)
        val existingIdx = snap.child("players").children
            .firstOrNull { it.child("id").getValue(String::class.java) == playerId }
            ?.key?.toIntOrNull()
        val idx = existingIdx ?: joined
        rooms.child(roomCode).child("players").child(idx.toString())
            .setValue(mapOf("id" to playerId, "name" to playerName, "connected" to true))
            .await()
        Pair(true, "")
    }.getOrElse { Pair(false, it.message ?: "Failed to join") }

    // ── Observe ───────────────────────────────────────────────────────────────

    fun observeRoom(roomCode: String, myPlayerId: String): Flow<RoomSnapshot?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.exists()) { trySend(null); return }

                val hostId      = snap.child("hostId").getValue(String::class.java) ?: ""
                val playerCount = snap.child("playerCount").getValue(Int::class.java) ?: 2
                val status      = snap.child("status").getValue(String::class.java) ?: "waiting"
                val joinedCount = snap.child("players").childrenCount.toInt()

                val playerNames       = mutableListOf<String>()
                val playerIds        = mutableListOf<String>()
                val disconnectedSet  = mutableSetOf<Int>()
                var myIdx            = -1
                for (i in 0 until playerCount) {
                    val p         = snap.child("players").child(i.toString())
                    val name      = p.child("name").getValue(String::class.java) ?: ""
                    val id        = p.child("id").getValue(String::class.java) ?: ""
                    val connected = p.child("connected").getValue(Boolean::class.java) ?: true
                    playerNames.add(name.ifBlank { "P${i + 1}" })
                    playerIds.add(id)
                    if (id == myPlayerId) myIdx = i
                    // Only flag as disconnected during an active game (id known means they joined)
                    if (!connected && id.isNotBlank()) disconnectedSet.add(i)
                }

                val gs            = snap.child("gameState")
                val positions     = (0 until playerCount).map { gs.child("positions").child(it.toString()).getValue(Int::class.java) ?: 0 }
                val skipCounts    = (0 until playerCount).map { gs.child("skipCounts").child(it.toString()).getValue(Int::class.java) ?: 0 }
                val eliminatedSet = gs.child("eliminatedPlayers").children
                    .mapNotNull { it.getValue(Int::class.java) }.toSet()
                val currentIdx    = gs.child("currentPlayerIndex").getValue(Int::class.java) ?: 0
                val diceValue     = gs.child("diceValue").getValue(Int::class.java) ?: 1
                val winnerRaw     = gs.child("winner").getValue(String::class.java) ?: ""
                val isRolling     = gs.child("isRolling").getValue(Boolean::class.java) ?: false
                val timeRemaining = gs.child("timeRemaining").getValue(Int::class.java) ?: 30
                val lastEventStr  = gs.child("lastEvent").getValue(String::class.java) ?: ""
                val lastEventPos  = gs.child("lastEventPosition").getValue(Int::class.java) ?: 0
                val lastEvent     = when (lastEventStr) {
                    "SNAKE"  -> GameEvent.SNAKE
                    "LADDER" -> GameEvent.LADDER
                    else     -> null
                }

                val gameState = GameState(
                    positions           = positions,
                    currentPlayerIndex  = currentIdx,
                    playerCount         = playerCount,
                    diceValue           = diceValue,
                    isRolling           = isRolling,
                    winner              = winnerRaw.ifBlank { null },
                    gameMode            = GameMode.ONLINE,
                    lastEvent           = lastEvent,
                    lastEventPosition   = lastEventPos,
                    skipCounts          = skipCounts,
                    eliminatedPlayers   = eliminatedSet,
                    disconnectedPlayers = disconnectedSet,
                    timeRemaining       = timeRemaining,
                    playerNames         = playerNames,
                    myPlayerIndex       = myIdx
                )

                trySend(RoomSnapshot(
                    roomCode      = roomCode,
                    hostId        = hostId,
                    playerCount   = playerCount,
                    status        = status,
                    joinedCount   = joinedCount.toInt(),
                    playerNames   = playerNames,
                    playerIds     = playerIds,
                    gameState     = gameState,
                    myPlayerIndex = myIdx
                ))
            }

            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        rooms.child(roomCode).addValueEventListener(listener)
        awaitClose { rooms.child(roomCode).removeEventListener(listener) }
    }

    // ── Write game state ──────────────────────────────────────────────────────

    suspend fun updateGameState(roomCode: String, state: GameState) {
        val update = mapOf(
            "currentPlayerIndex" to state.currentPlayerIndex,
            "diceValue"          to state.diceValue,
            "isRolling"          to state.isRolling,
            "winner"             to (state.winner ?: ""),
            "timeRemaining"      to state.timeRemaining,
            "lastEvent"          to (state.lastEvent?.name ?: ""),
            "lastEventPosition"  to state.lastEventPosition,
            "positions"          to state.positions,
            "skipCounts"         to state.skipCounts,
            "eliminatedPlayers"  to state.eliminatedPlayers.toList()
        )
        runCatching { rooms.child(roomCode).child("gameState").updateChildren(update).await() }
    }

    // ── Control ───────────────────────────────────────────────────────────────

    suspend fun startGame(roomCode: String) {
        runCatching { rooms.child(roomCode).child("status").setValue("playing").await() }
    }

    suspend fun markDisconnected(roomCode: String, playerIndex: Int) {
        runCatching {
            rooms.child(roomCode).child("players").child(playerIndex.toString())
                .child("connected").setValue(false).await()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun emptyGameStateMap(count: Int): Map<String, Any> = mapOf(
        "positions"          to List(count) { 0 },
        "currentPlayerIndex" to 0,
        "diceValue"          to 1,
        "winner"             to "",
        "isRolling"          to false,
        "timeRemaining"      to 30,
        "lastEvent"          to "",
        "lastEventPosition"  to 0,
        "skipCounts"         to List(count) { 0 },
        "eliminatedPlayers"  to emptyList<Int>()
    )
}
