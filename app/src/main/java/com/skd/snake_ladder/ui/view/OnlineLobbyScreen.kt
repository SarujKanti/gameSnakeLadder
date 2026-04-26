package com.skd.snake_ladder.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.online.OnlineGameViewModel
import com.skd.snake_ladder.online.OnlineUiState
import com.skd.snake_ladder.ui.theme.LocalAppColors

@Composable
fun OnlineLobbyScreen(
    viewModel: OnlineGameViewModel,
    onBack: () -> Unit
) {
    val colors  = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        viewModel.exitToMenu()
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgGradient)
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "lobby_state"
        ) { state ->
            when (state) {
                is OnlineUiState.Idle        -> LobbyIdleContent(viewModel, onBack)
                is OnlineUiState.Loading     -> LobbyLoadingContent()
                is OnlineUiState.WaitingRoom -> WaitingRoomContent(state, viewModel, onBack)
                is OnlineUiState.InGame      -> { /* MainActivity observes and switches screen */ }
                is OnlineUiState.Error       -> LobbyErrorContent(state.message) { viewModel.resetToIdle() }
            }
        }
    }
}

// ── Idle — pick Create or Join ────────────────────────────────────────────────

@Composable
private fun LobbyIdleContent(viewModel: OnlineGameViewModel, onBack: () -> Unit) {
    var mode by remember { mutableStateOf<LobbyMode?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Text("← Back", color = Color(0xFF4FC3F7), fontSize = 13.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text("🌐", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Play Online",
            style = TextStyle(
                brush = Brush.linearGradient(listOf(Color(0xFF4FC3F7), Color(0xFFFFD700))),
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Challenge friends anywhere",
            fontSize = 14.sp,
            color = Color(0xFF546E7A)
        )

        Spacer(Modifier.height(40.dp))

        when (mode) {
            null           -> {
                OnlineOptionCard(
                    emoji       = "🏠",
                    title       = "Create Room",
                    subtitle    = "Start a new game and invite friends",
                    accentColor = Color(0xFF1565C0),
                    onClick     = { mode = LobbyMode.Create }
                )
                Spacer(Modifier.height(14.dp))
                OnlineOptionCard(
                    emoji       = "🚪",
                    title       = "Join Room",
                    subtitle    = "Enter a room code to join",
                    accentColor = Color(0xFF6A1B9A),
                    onClick     = { mode = LobbyMode.Join }
                )
            }
            LobbyMode.Create -> CreateRoomForm(viewModel) { mode = null }
            LobbyMode.Join   -> JoinRoomForm(viewModel)   { mode = null }
        }
    }
}

private enum class LobbyMode { Create, Join }

@Composable
private fun OnlineOptionCard(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A2540), Color(0xFF111C33))))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(18.dp))
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            )
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    Brush.linearGradient(listOf(accentColor, accentColor.copy(alpha = 0.7f))),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) { Text(emoji, fontSize = 22.sp) }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    fontSize = 17.sp, fontWeight = FontWeight.Bold,    color = Color(0xFFECEFF1))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF546E7A))
        }
        Text("›", fontSize = 22.sp, color = Color(0xFF4FC3F7))
    }
}

// ── Create Room form ──────────────────────────────────────────────────────────

@Composable
private fun CreateRoomForm(viewModel: OnlineGameViewModel, onBack: () -> Unit) {
    var name        by remember { mutableStateOf("") }
    var playerCount by remember { mutableStateOf(2) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FormLabel("Your name")
        LobbyTextField(value = name, onValueChange = { name = it.take(16) }, placeholder = "Enter your name")

        Spacer(Modifier.height(20.dp))
        FormLabel("Number of players")
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (2..6).forEach { count ->
                OnlineCountChip(
                    count    = count,
                    selected = count == playerCount,
                    color    = PLAYER_COLORS[count - 1],
                    onClick  = { playerCount = count }
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        LobbyButton("Create Room") { viewModel.createRoom(name, playerCount) }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("← Back", color = Color(0xFF4FC3F7), fontSize = 13.sp) }
    }
}

// ── Join Room form ────────────────────────────────────────────────────────────

@Composable
private fun JoinRoomForm(viewModel: OnlineGameViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        FormLabel("Your name")
        LobbyTextField(
            value = name, onValueChange = { name = it.take(16) },
            placeholder = "Enter your name", imeAction = ImeAction.Next
        )

        Spacer(Modifier.height(16.dp))
        FormLabel("Room code")
        LobbyTextField(
            value = code,
            onValueChange = { code = it.take(6).uppercase() },
            placeholder = "e.g. AB3X7K",
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.Characters
        )

        Spacer(Modifier.height(28.dp))
        LobbyButton(
            text    = "Join Room",
            enabled = code.length == 6,
            color   = Color(0xFF6A1B9A)
        ) { viewModel.joinRoom(code, name) }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("← Back", color = Color(0xFF4FC3F7), fontSize = 13.sp) }
    }
}

// ── Waiting Room ──────────────────────────────────────────────────────────────

@Composable
private fun WaitingRoomContent(
    room: OnlineUiState.WaitingRoom,
    viewModel: OnlineGameViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick  = { viewModel.exitToMenu(); onBack() },
            modifier = Modifier.align(Alignment.Start)
        ) { Text("← Leave", color = Color(0xFF4FC3F7), fontSize = 13.sp) }

        Spacer(Modifier.height(20.dp))
        Text("Waiting for Players", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFECEFF1))
        Spacer(Modifier.height(8.dp))
        Text("Share this code with your friends:", fontSize = 13.sp, color = Color(0xFF546E7A))
        Spacer(Modifier.height(12.dp))

        // Room code display
        Box(
            modifier = Modifier
                .background(Color(0x14FFFFFF), RoundedCornerShape(14.dp))
                .border(1.5.dp, Color(0x55FFD700), RoundedCornerShape(14.dp))
                .padding(horizontal = 28.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text          = room.roomCode,
                fontSize      = 32.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color(0xFFFFD700),
                letterSpacing = 6.sp
            )
        }

        Spacer(Modifier.height(28.dp))
        Text(
            text = "${room.joinedNames.size} / ${room.totalNeeded} players joined",
            fontSize = 13.sp, color = Color(0xFF78909C), fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(room.totalNeeded) { idx ->
                val joined = idx < room.joinedNames.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (joined) Color(0x14FFFFFF) else Color(0x08FFFFFF),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (joined) Color(0x22FFFFFF) else Color(0x0AFFFFFF),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (joined) PLAYER_COLORS[idx] else Color(0xFF2A3A55),
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = if (joined) room.joinedNames[idx] else "Waiting…",
                        fontSize   = 14.sp,
                        fontWeight = if (joined) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (joined) Color(0xFFECEFF1) else Color(0xFF37474F)
                    )
                    if (idx == 0) {
                        Spacer(Modifier.weight(1f))
                        Text("HOST", fontSize = 9.sp, color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        if (room.isHost) {
            val ready = room.joinedNames.size >= room.totalNeeded
            LobbyButton(text = if (ready) "Start Game →" else "Waiting for players…", enabled = ready) {
                viewModel.startOnlineGame()
            }
        } else {
            Text(
                text = "Waiting for the host to start the game…",
                fontSize = 13.sp, color = Color(0xFF546E7A), textAlign = TextAlign.Center
            )
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun LobbyLoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF4FC3F7), strokeWidth = 3.dp)
            Spacer(Modifier.height(16.dp))
            Text("Connecting…", color = Color(0xFF78909C), fontSize = 14.sp)
        }
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun LobbyErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⚠️", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text(message, color = Color(0xFFEF9A9A), fontSize = 15.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onRetry) {
                Text("Try Again", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Shared small composables ──────────────────────────────────────────────────

@Composable
private fun FormLabel(text: String) {
    Text(
        text     = text,
        fontSize = 12.sp,
        color    = Color(0xFF546E7A),
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

@Composable
private fun LobbyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction = ImeAction.Done,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, fontSize = 14.sp, color = Color(0xFF37474F)) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        textStyle     = TextStyle(color = Color(0xFFECEFF1), fontSize = 15.sp),
        keyboardOptions = KeyboardOptions(imeAction = imeAction, capitalization = capitalization),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = Color(0xFF4FC3F7),
            unfocusedBorderColor    = Color(0x2AFFFFFF),
            focusedContainerColor   = Color(0x0CFFFFFF),
            unfocusedContainerColor = Color(0x06FFFFFF),
            cursorColor             = Color(0xFF4FC3F7)
        )
    )
}

@Composable
private fun LobbyButton(
    text: String,
    enabled: Boolean = true,
    color: Color = Color(0xFF1565C0),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(if (enabled) 6.dp else 0.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled)
                    Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f)))
                else
                    Brush.linearGradient(listOf(Color(0xFF1A2540), Color(0xFF111C33)))
            )
            .clickable(
                enabled           = enabled,
                indication        = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Bold,
            color      = if (enabled) Color.White else Color(0xFF37474F)
        )
    }
}

@Composable
private fun OnlineCountChip(count: Int, selected: Boolean, color: Color, onClick: () -> Unit) {
    val bg     = if (selected) color else Color(0x18FFFFFF)
    val border = if (selected) color else Color(0x2AFFFFFF)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.5.dp, border, CircleShape)
            .background(bg, CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "$count",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = if (selected) Color.White else Color(0x88FFFFFF)
        )
    }
}
