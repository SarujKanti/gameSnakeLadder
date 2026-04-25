package com.skd.snake_ladder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.skd.snake_ladder.data.SettingsRepository
import com.skd.snake_ladder.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    private val _soundEnabled = MutableStateFlow(repo.soundEnabled)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled

    private val _boardIndex = MutableStateFlow(repo.boardIndex)
    val boardIndex: StateFlow<Int> = _boardIndex

    private val _themeMode = MutableStateFlow(repo.themeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode

    fun setSoundEnabled(enabled: Boolean) {
        repo.soundEnabled = enabled
        _soundEnabled.value = enabled
    }

    fun setBoardIndex(index: Int) {
        repo.boardIndex = index
        _boardIndex.value = index
    }

    fun setThemeMode(mode: ThemeMode) {
        repo.themeMode = mode
        _themeMode.value = mode
    }
}
