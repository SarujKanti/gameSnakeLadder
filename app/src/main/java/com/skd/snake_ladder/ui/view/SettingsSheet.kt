package com.skd.snake_ladder.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.data.BoardConfigs
import com.skd.snake_ladder.domain.model.ThemeMode
import com.skd.snake_ladder.viewmodel.SettingsViewModel

private val SheetBg = Brush.verticalGradient(
    listOf(Color(0xFF131F38), Color(0xFF0F1829))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val boardIndex   by viewModel.boardIndex.collectAsStateWithLifecycle()
    val themeMode    by viewModel.themeMode.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest   = onDismiss,
        containerColor     = Color(0xFF131F38),
        contentColor       = Color(0xFFECEFF1),
        dragHandle         = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Color(0x44FFFFFF), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Header ─────────────────────────────────────────────────────────
            Text(
                text       = "⚙️  Settings",
                fontSize   = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color(0xFFECEFF1)
            )

            Divider(color = Color(0x20FFFFFF))

            // ── Sound ──────────────────────────────────────────────────────────
            SettingsSection(title = "🔊  Sound") {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text      = "Game Sounds",
                            fontSize  = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color     = Color(0xFFECEFF1)
                        )
                        Text(
                            text     = "Dice · Snake · Ladder · Win",
                            fontSize = 11.sp,
                            color    = Color(0xFF78909C)
                        )
                    }
                    Switch(
                        checked         = soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor       = Color(0xFFFFFFFF),
                            checkedTrackColor       = Color(0xFF1565C0),
                            uncheckedThumbColor     = Color(0xFF78909C),
                            uncheckedTrackColor     = Color(0x30FFFFFF)
                        )
                    )
                }
            }

            Divider(color = Color(0x20FFFFFF))

            // ── Board ──────────────────────────────────────────────────────────
            SettingsSection(title = "🗺️  Board Layout") {
                Text(
                    text     = "Choose one of 7 unique snake & ladder configurations",
                    fontSize = 12.sp,
                    color    = Color(0xFF78909C)
                )
                Spacer(Modifier.height(12.dp))
                // 7 boards — 2 per row (+ 1 solo on last row)
                val chunked = BoardConfigs.configs.chunked(2)
                chunked.forEach { row ->
                    Row(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { config ->
                            val isSelected = config.id == boardIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0x331565C0) else Color(0x15FFFFFF)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color(0xFF4FC3F7) else Color(0x20FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setBoardIndex(config.id) }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(config.emoji, fontSize = 24.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = config.name,
                                        fontSize   = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color      = if (isSelected) Color(0xFF4FC3F7) else Color(0xFF90A4AE)
                                    )
                                    // Mini snake/ladder count label
                                    Text(
                                        text    = "${config.snakes.size}🐍  ${config.ladders.size}🪜",
                                        fontSize = 9.sp,
                                        color   = Color(0xFF546E7A)
                                    )
                                }
                            }
                        }
                        // Pad last row if odd count
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Divider(color = Color(0x20FFFFFF))

            // ── Theme ──────────────────────────────────────────────────────────
            SettingsSection(title = "🎨  App Theme") {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeModeButton(
                        label    = "🌙 Dark",
                        selected = themeMode == ThemeMode.DARK,
                        onClick  = { viewModel.setThemeMode(ThemeMode.DARK) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        label    = "☀️ Light",
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick  = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        label    = "📱 System",
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick  = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text       = title,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF4FC3F7),
            letterSpacing = 0.5.sp
        )
        content()
    }
}

@Composable
private fun ThemeModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0x331565C0) else Color(0x15FFFFFF))
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) Color(0xFF4FC3F7) else Color(0x20FFFFFF),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color      = if (selected) Color(0xFF4FC3F7) else Color(0xFF90A4AE)
        )
    }
}
