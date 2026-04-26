package com.skd.snake_ladder.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.R
import com.skd.snake_ladder.data.BoardConfigs
import com.skd.snake_ladder.domain.model.GameMode
import com.skd.snake_ladder.domain.model.ThemeMode
import com.skd.snake_ladder.ui.theme.LocalAppColors
import com.skd.snake_ladder.viewmodel.SettingsViewModel

// ── Shared token palette & bg gradient (used by other screens too) ─────────────
internal val PLAYER_COLORS = listOf(
    Color(0xFF2196F3), // sky blue
    Color(0xFFE91E63), // pink
    Color(0xFF4CAF50), // green
    Color(0xFFFF9800), // amber
    Color(0xFF9C27B0), // purple
    Color(0xFF00BCD4), // cyan
)

// Legacy fallback gradient — all screens now use LocalAppColors.current.bgGradient
@Suppress("unused")
internal val AppBgGradient = Brush.verticalGradient(
    colorStops = arrayOf(
        0.0f  to Color(0xFF070D1A),
        0.45f to Color(0xFF0F1829),
        1.0f  to Color(0xFF0A1020)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    settingsVm: SettingsViewModel,
    onModeSelected: (GameMode, Int, List<String>) -> Unit,
    hasSavedGameForCount: (Int) -> Boolean = { false },
    onResumeSavedGame: () -> Unit = {},
    onPlayOnline: () -> Unit = {}
) {
    val colors = LocalAppColors.current

    // ── Live settings state ────────────────────────────────────────────────────
    val boardIndex   by settingsVm.boardIndex.collectAsStateWithLifecycle()
    val soundEnabled by settingsVm.soundEnabled.collectAsStateWithLifecycle()
    val themeMode    by settingsVm.themeMode.collectAsStateWithLifecycle()
    val activeBoard  = BoardConfigs.configs.getOrElse(boardIndex) { BoardConfigs.configs[0] }

    var showSettings     by remember { mutableStateOf(false) }
    var multiExpanded    by remember { mutableStateOf(false) }
    var selectedCount    by remember { mutableStateOf(2) }
    val nameInputs       = remember { mutableStateListOf("", "", "", "", "", "") }
    var showContinueDialog by remember { mutableStateOf(false) }
    var pendingCount       by remember { mutableStateOf(2) }
    var pendingNames       by remember { mutableStateOf(listOf<String>()) }

    // Gentle floating animation for emoji logo
    val inf = rememberInfiniteTransition(label = "logo")
    val logoFloat by inf.animateFloat(
        initialValue  = -5f, targetValue  = 5f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ), label = "float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 12.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar: settings gear ────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (colors.isDark) Color(0x14FFFFFF) else Color(0x14000000)
                        )
                        .clickable { showSettings = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint               = colors.accent,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── Animated logo ─────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.graphicsLayer { translationY = logoFloat }
            ) {
                Text("🐍", fontSize = 42.sp)
                Text("🎲", fontSize = 52.sp)
                Text("🪜", fontSize = 42.sp)
            }

            Spacer(Modifier.height(12.dp))

            // ── Title ──────────────────────────────────────────────────────────
            Text(
                text  = stringResource(R.string.game_title),
                style = TextStyle(
                    brush = Brush.linearGradient(
                        if (colors.isDark)
                            listOf(Color(0xFF4FC3F7), Color(0xFFB3E5FC), Color(0xFFFFD700))
                        else
                            listOf(Color(0xFF1565C0), Color(0xFF0288D1), Color(0xFFF9A825))
                    ),
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text      = stringResource(R.string.game_subtitle),
                fontSize  = 13.sp,
                color     = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // ── Live Settings Status Strip ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (colors.isDark) Color(0x0DFFFFFF) else Color(0x0A000000)
                    )
                    .border(
                        1.dp,
                        if (colors.isDark) Color(0x14FFFFFF) else Color(0x18000000),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                StatusChip(
                    icon  = activeBoard.emoji,
                    label = activeBoard.name,
                    tint  = colors.accent,
                    isDark = colors.isDark,
                    onClick = { showSettings = true }
                )

                VerticalDivider(
                    modifier  = Modifier.height(22.dp),
                    color     = colors.divider,
                    thickness = 1.dp
                )

                val soundTint by animateColorAsState(
                    targetValue    = if (soundEnabled) Color(0xFF43A047) else colors.textHint,
                    animationSpec  = tween(300),
                    label          = "sound"
                )
                StatusChip(
                    icon    = if (soundEnabled) "🔊" else "🔇",
                    label   = if (soundEnabled) "Sound On" else "Sound Off",
                    tint    = soundTint,
                    isDark  = colors.isDark,
                    onClick = { showSettings = true }
                )

                VerticalDivider(
                    modifier  = Modifier.height(22.dp),
                    color     = colors.divider,
                    thickness = 1.dp
                )

                val themeIcon  = when (themeMode) { ThemeMode.DARK -> "🌙"; ThemeMode.LIGHT -> "☀️"; else -> "📱" }
                val themeLabel = when (themeMode) { ThemeMode.DARK -> "Dark"; ThemeMode.LIGHT -> "Light"; else -> "System" }
                StatusChip(
                    icon    = themeIcon,
                    label   = themeLabel,
                    tint    = Color(0xFFFFB300),
                    isDark  = colors.isDark,
                    onClick = { showSettings = true }
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Section label ──────────────────────────────────────────────────
            Text(
                text          = stringResource(R.string.select_mode),
                fontSize      = 11.sp,
                fontWeight    = FontWeight.Bold,
                color         = colors.textHint,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            // ── VS Computer ────────────────────────────────────────────────────
            ModeCard(
                accentColor = Color(0xFF1565C0),
                colors      = colors,
                onClick     = { onModeSelected(GameMode.VS_COMPUTER, 2, emptyList()) }
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    ModeIcon(
                        emoji      = "🤖",
                        background = Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF0D47A1)))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = stringResource(R.string.mode_vs_computer),
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = colors.textPrimary
                        )
                        Text(
                            text     = "Challenge the AI · Any time",
                            fontSize = 12.sp,
                            color    = colors.textSecondary
                        )
                    }
                    Text("›", fontSize = 24.sp, color = colors.accent, fontWeight = FontWeight.Light)
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Multiplayer ────────────────────────────────────────────────────
            ModeCard(
                accentColor = Color(0xFF6A1B9A),
                colors      = colors,
                onClick     = { multiExpanded = !multiExpanded }
            ) {
                Column {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        ModeIcon(
                            emoji      = "👥",
                            background = Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFF4A148C)))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = stringResource(R.string.mode_multiplayer),
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = colors.textPrimary
                            )
                            Text(
                                text     = "2 – 6 players · Same device",
                                fontSize = 12.sp,
                                color    = colors.textSecondary
                            )
                        }
                        Text(
                            text  = if (multiExpanded) "⌃" else "⌄",
                            fontSize = 18.sp,
                            color = Color(0xFF9C27B0)
                        )
                    }

                    AnimatedVisibility(
                        visible = multiExpanded,
                        enter   = expandVertically(tween(280)) + fadeIn(tween(280)),
                        exit    = shrinkVertically(tween(220)) + fadeOut(tween(220))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (colors.isDark) Color(0x0AFFFFFF) else Color(0x06000000)
                                )
                                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(color = colors.divider)
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text          = stringResource(R.string.select_players),
                                fontSize      = 11.sp,
                                color         = colors.textHint,
                                fontWeight    = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (2..6).forEach { count ->
                                    PlayerCountChip(
                                        count    = count,
                                        selected = count == selectedCount,
                                        color    = PLAYER_COLORS[count - 1],
                                        onClick  = { selectedCount = count }
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(selectedCount) { idx ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier          = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .background(PLAYER_COLORS[idx], CircleShape)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        OutlinedTextField(
                                            value         = nameInputs[idx],
                                            onValueChange = { nameInputs[idx] = it.take(16) },
                                            placeholder   = {
                                                Text(
                                                    "Player ${idx + 1}",
                                                    fontSize = 13.sp,
                                                    color    = colors.textHint
                                                )
                                            },
                                            singleLine      = true,
                                            modifier        = Modifier.fillMaxWidth(),
                                            shape           = RoundedCornerShape(10.dp),
                                            textStyle       = TextStyle(
                                                color    = colors.textPrimary,
                                                fontSize = 13.sp
                                            ),
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = if (idx < selectedCount - 1)
                                                    ImeAction.Next else ImeAction.Done
                                            ),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor      = PLAYER_COLORS[idx],
                                                unfocusedBorderColor    = colors.divider,
                                                focusedContainerColor   = if (colors.isDark) Color(0x0CFFFFFF) else Color(0x06000000),
                                                unfocusedContainerColor = if (colors.isDark) Color(0x06FFFFFF) else Color(0x03000000),
                                                cursorColor             = PLAYER_COLORS[idx]
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(18.dp))

                            // Start button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF1976D2), Color(0xFF1565C0), Color(0xFF0D47A1))
                                        )
                                    )
                                    .clickable {
                                        val names = nameInputs.take(selectedCount)
                                        if (hasSavedGameForCount(selectedCount)) {
                                            pendingCount = selectedCount; pendingNames = names
                                            showContinueDialog = true
                                        } else {
                                            onModeSelected(GameMode.MULTI_PLAYER, selectedCount, names)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text          = stringResource(R.string.start_game, selectedCount),
                                    fontSize      = 15.sp,
                                    fontWeight    = FontWeight.Bold,
                                    color         = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Play Online ────────────────────────────────────────────────────
            ModeCard(
                accentColor = Color(0xFF00695C),
                colors      = colors,
                onClick     = onPlayOnline
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    ModeIcon(
                        emoji      = "🌐",
                        background = Brush.linearGradient(listOf(Color(0xFF00695C), Color(0xFF004D40)))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = "Play Online",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color      = colors.textPrimary
                        )
                        Text(
                            text     = "Create or join a room · Real players",
                            fontSize = 12.sp,
                            color    = colors.textSecondary
                        )
                    }
                    Text("›", fontSize = 24.sp, color = colors.accent, fontWeight = FontWeight.Light)
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Footer ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth(0.55f)
            ) {
                Box(Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, Color(0x33FFD700)))
                ))
                Box(Modifier.padding(horizontal = 8.dp).size(4.dp).background(Color(0xFFFFD700), CircleShape))
                Box(Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color(0x33FFD700), Color.Transparent))
                ))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text      = stringResource(R.string.tagline),
                fontSize  = 11.sp,
                color     = colors.textHint,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text          = "Powered by SKD",
                fontSize      = 10.sp,
                color         = if (colors.isDark) Color(0x44FFFFFF) else Color(0x66000000),
                fontWeight    = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign     = TextAlign.Center
            )
        }
    }

    // ── Continue-or-New dialog ────────────────────────────────────────────────
    if (showContinueDialog) {
        val dialogBg = if (colors.isDark) Color(0xFF111C33) else Color(0xFFFFFFFF)
        AlertDialog(
            onDismissRequest = { showContinueDialog = false },
            shape            = RoundedCornerShape(24.dp),
            containerColor   = dialogBg,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎲", fontSize = 36.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text       = "Unfinished Game",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 18.sp,
                        color      = colors.textPrimary
                    )
                }
            },
            text = {
                Text(
                    text      = "You have an unfinished $pendingCount-player game. Continue where you left off, or start a new one?",
                    fontSize  = 14.sp,
                    color     = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showContinueDialog = false; onResumeSavedGame() },
                    shape   = RoundedCornerShape(10.dp),
                    colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) { Text("Continue", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showContinueDialog = false; onModeSelected(GameMode.MULTI_PLAYER, pendingCount, pendingNames) }) {
                    Text("New Game", color = colors.accent)
                }
            }
        )
    }

    // ── Settings sheet ────────────────────────────────────────────────────────
    if (showSettings) {
        SettingsSheet(viewModel = settingsVm, onDismiss = { showSettings = false })
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun StatusChip(icon: String, label: String, tint: Color, isDark: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(icon, fontSize = 13.sp)
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = tint
        )
    }
}

@Composable
private fun ModeCard(accentColor: Color, colors: com.skd.snake_ladder.ui.theme.AppColors, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation    = if (colors.isDark) 6.dp else 4.dp,
                shape        = RoundedCornerShape(18.dp),
                ambientColor = if (colors.isDark) Color.Black else accentColor.copy(0.08f),
                spotColor    = if (colors.isDark) Color.Black else accentColor.copy(0.12f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardGradient)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        // Left-edge accent bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(accentColor.copy(alpha = 0.9f), accentColor.copy(alpha = 0.25f))
                    )
                )
                .align(Alignment.CenterStart)
        )
        content()
    }
}

@Composable
private fun ModeIcon(emoji: String, background: Brush) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(4.dp, RoundedCornerShape(13.dp))
            .clip(RoundedCornerShape(13.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) { Text(emoji, fontSize = 22.sp) }
}

@Composable
private fun PlayerCountChip(count: Int, selected: Boolean, color: Color, onClick: () -> Unit) {
    val bg     = if (selected) color       else Color(0x18FFFFFF)
    val border = if (selected) color       else Color(0x2AFFFFFF)
    val text   = if (selected) Color.White else Color(0x88FFFFFF)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(1.5.dp, border, CircleShape)
            .background(bg, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = text)
    }
}
