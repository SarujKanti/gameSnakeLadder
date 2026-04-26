package com.skd.snake_ladder.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skd.snake_ladder.data.BoardColorScheme
import com.skd.snake_ladder.data.BoardColorSchemes
import com.skd.snake_ladder.data.BoardConfigs
import com.skd.snake_ladder.domain.model.ThemeMode
import com.skd.snake_ladder.ui.theme.LocalAppColors
import com.skd.snake_ladder.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val colors           = LocalAppColors.current
    val soundEnabled     by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val boardIndex       by viewModel.boardIndex.collectAsStateWithLifecycle()
    val boardColorIndex  by viewModel.boardColorIndex.collectAsStateWithLifecycle()
    val themeMode        by viewModel.themeMode.collectAsStateWithLifecycle()

    val sheetBg  = colors.surfaceSheet
    val textMain = colors.textPrimary
    val textSub  = colors.textSecondary
    val divColor = colors.divider
    val accentC  = colors.accent

    val btnBgSelected    = if (colors.isDark) Color(0x331565C0) else Color(0xFFE3EEFF)
    val btnBgUnselected  = if (colors.isDark) Color(0x15FFFFFF) else Color(0xFFF0F4FF)
    val btnBorderSel     = accentC.copy(alpha = 0.8f)
    val btnBorderUnsel   = if (colors.isDark) Color(0x20FFFFFF) else Color(0xFFD0DCF0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = sheetBg,
        contentColor     = textMain,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        if (colors.isDark) Color(0x44FFFFFF) else Color(0xFFCFD8DC),
                        RoundedCornerShape(2.dp)
                    )
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
                color      = textMain
            )

            Divider(color = divColor)

            // ── Sound ──────────────────────────────────────────────────────────
            SettingsSection(title = "🔊  Sound", labelColor = accentC) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text       = "Game Sounds",
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = textMain
                        )
                        Text(
                            text     = "Dice · Snake · Ladder · Win",
                            fontSize = 11.sp,
                            color    = textSub
                        )
                    }
                    Switch(
                        checked         = soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor   = Color(0xFFFFFFFF),
                            checkedTrackColor   = Color(0xFF1565C0),
                            uncheckedThumbColor = textSub,
                            uncheckedTrackColor = if (colors.isDark) Color(0x30FFFFFF) else Color(0xFFCFD8DC)
                        )
                    )
                }
            }

            Divider(color = divColor)

            // ── Board Layout ───────────────────────────────────────────────────
            SettingsSection(title = "🗺️  Board Layout", labelColor = accentC) {
                Text(
                    text     = "Choose one of 7 unique snake & ladder configurations",
                    fontSize = 12.sp,
                    color    = textSub
                )
                Spacer(Modifier.height(12.dp))
                val chunked = BoardConfigs.configs.chunked(2)
                chunked.forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { config ->
                            val isSelected = config.id == boardIndex
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) btnBgSelected else btnBgUnselected)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) btnBorderSel else btnBorderUnsel,
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
                                        color      = if (isSelected) accentC else textSub
                                    )
                                    Text(
                                        text     = "${config.snakes.size}🐍  ${config.ladders.size}🪜",
                                        fontSize = 9.sp,
                                        color    = colors.textHint.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Divider(color = divColor)

            // ── Board Color ────────────────────────────────────────────────────
            SettingsSection(title = "🎨  Board Color", labelColor = accentC) {
                Text(
                    text     = "Choose a color theme for the game board",
                    fontSize = 12.sp,
                    color    = textSub
                )
                Spacer(Modifier.height(14.dp))

                // 3 per row
                BoardColorSchemes.schemes.chunked(3).forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { scheme ->
                            val isSelected = scheme.id == boardColorIndex
                            BoardColorCard(
                                scheme     = scheme,
                                isSelected = isSelected,
                                accentC    = accentC,
                                selectedBg = btnBgSelected,
                                unselectedBg = btnBgUnselected,
                                selectedBorder = btnBorderSel,
                                unselectedBorder = btnBorderUnsel,
                                textSub    = textSub,
                                modifier   = Modifier.weight(1f),
                                onClick    = { viewModel.setBoardColorIndex(scheme.id) }
                            )
                        }
                        // Pad last row if fewer than 3
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Divider(color = divColor)

            // ── Theme ──────────────────────────────────────────────────────────
            SettingsSection(title = "🌓  App Theme", labelColor = accentC) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeModeButton(
                        label            = "🌙 Dark",
                        selected         = themeMode == ThemeMode.DARK,
                        onClick          = { viewModel.setThemeMode(ThemeMode.DARK) },
                        selectedBg       = btnBgSelected,
                        unselectedBg     = btnBgUnselected,
                        selectedBorder   = btnBorderSel,
                        unselectedBorder = btnBorderUnsel,
                        selectedColor    = accentC,
                        unselectedColor  = textSub,
                        modifier         = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        label            = "☀️ Light",
                        selected         = themeMode == ThemeMode.LIGHT,
                        onClick          = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        selectedBg       = btnBgSelected,
                        unselectedBg     = btnBgUnselected,
                        selectedBorder   = btnBorderSel,
                        unselectedBorder = btnBorderUnsel,
                        selectedColor    = accentC,
                        unselectedColor  = textSub,
                        modifier         = Modifier.weight(1f)
                    )
                    ThemeModeButton(
                        label            = "📱 System",
                        selected         = themeMode == ThemeMode.SYSTEM,
                        onClick          = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        selectedBg       = btnBgSelected,
                        unselectedBg     = btnBgUnselected,
                        selectedBorder   = btnBorderSel,
                        unselectedBorder = btnBorderUnsel,
                        selectedColor    = accentC,
                        unselectedColor  = textSub,
                        modifier         = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Board Color Card ──────────────────────────────────────────────────────────

@Composable
private fun BoardColorCard(
    scheme: BoardColorScheme,
    isSelected: Boolean,
    accentC: Color,
    selectedBg: Color,
    unselectedBg: Color,
    selectedBorder: Color,
    unselectedBorder: Color,
    textSub: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier            = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) selectedBg else unselectedBg)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) selectedBorder else unselectedBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Mini checkerboard swatch — 4×4 grid drawn with Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(if (isSelected) 4.dp else 1.dp, RoundedCornerShape(8.dp), clip = false)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 1.5.dp else 0.5.dp,
                    color = if (isSelected) selectedBorder.copy(alpha = 0.6f) else unselectedBorder,
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cols     = 5
                val rows     = 4
                val cellW    = size.width  / cols
                val cellH    = size.height / rows
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        drawRect(
                            color   = if ((r + c) % 2 == 0) scheme.cellLight else scheme.cellDark,
                            topLeft = Offset(c * cellW, r * cellH),
                            size    = Size(cellW, cellH)
                        )
                    }
                }
                // Subtle grid lines
                for (c in 1 until cols) {
                    drawLine(
                        color       = scheme.gridLine.copy(alpha = 0.5f),
                        start       = Offset(c * cellW, 0f),
                        end         = Offset(c * cellW, size.height),
                        strokeWidth = 0.6f
                    )
                }
                for (r in 1 until rows) {
                    drawLine(
                        color       = scheme.gridLine.copy(alpha = 0.5f),
                        start       = Offset(0f, r * cellH),
                        end         = Offset(size.width, r * cellH),
                        strokeWidth = 0.6f
                    )
                }
            }
        }

        // Emoji
        Text(scheme.emoji, fontSize = 16.sp)

        // Name
        Text(
            text       = scheme.name,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = if (isSelected) accentC else textSub,
            maxLines   = 1
        )
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    labelColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text          = title,
            fontSize      = 13.sp,
            fontWeight    = FontWeight.Bold,
            color         = labelColor,
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
    selectedBg: Color,
    unselectedBg: Color,
    selectedBorder: Color,
    unselectedBorder: Color,
    selectedColor: Color,
    unselectedColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) selectedBg else unselectedBg)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) selectedBorder else unselectedBorder,
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
            color      = if (selected) selectedColor else unselectedColor
        )
    }
}
