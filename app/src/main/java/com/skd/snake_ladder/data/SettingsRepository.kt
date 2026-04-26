package com.skd.snake_ladder.data

import android.content.Context
import com.skd.snake_ladder.domain.model.ThemeMode

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)

    // ── Sound ─────────────────────────────────────────────────────────────────
    var soundEnabled: Boolean
        get()      = prefs.getBoolean(KEY_SOUND, true)
        set(value) { prefs.edit().putBoolean(KEY_SOUND, value).apply() }

    // ── Board configuration (0-indexed) ──────────────────────────────────────
    var boardIndex: Int
        get()      = prefs.getInt(KEY_BOARD, 0).coerceIn(0, BoardConfigs.configs.lastIndex)
        set(value) { prefs.edit().putInt(KEY_BOARD, value).apply() }

    val selectedBoard: BoardConfig
        get() = BoardConfigs.configs[boardIndex]

    // ── Board color scheme (0-indexed) ────────────────────────────────────────
    var boardColorIndex: Int
        get()      = prefs.getInt(KEY_BOARD_COLOR, 0).coerceIn(0, BoardColorSchemes.schemes.lastIndex)
        set(value) { prefs.edit().putInt(KEY_BOARD_COLOR, value).apply() }

    val selectedBoardColor: BoardColorScheme
        get() = BoardColorSchemes.schemes[boardColorIndex]

    // ── Theme ─────────────────────────────────────────────────────────────────
    var themeMode: ThemeMode
        get()      = ThemeMode.valueOf(
            prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        )
        set(value) { prefs.edit().putString(KEY_THEME, value.name).apply() }

    private companion object {
        const val KEY_SOUND       = "sound_enabled"
        const val KEY_BOARD       = "board_index"
        const val KEY_BOARD_COLOR = "board_color_index"
        const val KEY_THEME       = "theme_mode"
    }
}
