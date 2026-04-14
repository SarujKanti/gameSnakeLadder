package com.skd.snake_ladder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skd.snake_ladder.online.OnlineGameViewModel
import com.skd.snake_ladder.online.OnlineUiState
import com.skd.snake_ladder.ui.view.GameScreen
import com.skd.snake_ladder.ui.view.ModeSelectionScreen
import com.skd.snake_ladder.ui.view.OnlineLobbyScreen
import com.skd.snake_ladder.viewmodel.GameViewModel

private enum class AppScreen { Menu, LocalGame, OnlineLobby, OnlineGame }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val localVm:  GameViewModel       = viewModel()
            val onlineVm: OnlineGameViewModel = viewModel()

            val localState  = localVm.state.collectAsStateWithLifecycle().value
            val onlineUi    = onlineVm.uiState.collectAsStateWithLifecycle().value

            var currentScreen by remember { mutableStateOf(AppScreen.Menu) }

            // Auto-navigate to online game when Firebase signals "playing"
            LaunchedEffect(onlineUi) {
                if (onlineUi is OnlineUiState.InGame && currentScreen == AppScreen.OnlineLobby) {
                    currentScreen = AppScreen.OnlineGame
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                color = androidx.compose.ui.graphics.Color(0xFF070D1A)
            ) {
                when (currentScreen) {

                    AppScreen.Menu -> {
                        ModeSelectionScreen(
                            onModeSelected       = { mode, count, names ->
                                localVm.setGameMode(mode, count, names)
                                currentScreen = AppScreen.LocalGame
                            },
                            hasSavedGameForCount = { count -> localVm.hasSavedGameForCount(count) },
                            onResumeSavedGame    = {
                                localVm.resumeSavedGame()
                                currentScreen = AppScreen.LocalGame
                            },
                            onPlayOnline         = { currentScreen = AppScreen.OnlineLobby }
                        )
                    }

                    AppScreen.LocalGame -> {
                        BackHandler {
                            localVm.exitToMenu()
                            currentScreen = AppScreen.Menu
                        }
                        GameScreen(
                            controller = localVm,
                            onBack     = {
                                localVm.exitToMenu()
                                currentScreen = AppScreen.Menu
                            }
                        )
                    }

                    AppScreen.OnlineLobby -> {
                        BackHandler {
                            onlineVm.exitToMenu()
                            currentScreen = AppScreen.Menu
                        }
                        OnlineLobbyScreen(
                            viewModel = onlineVm,
                            onBack    = {
                                onlineVm.exitToMenu()
                                currentScreen = AppScreen.Menu
                            }
                        )
                    }

                    AppScreen.OnlineGame -> {
                        BackHandler {
                            onlineVm.exitToMenu()
                            currentScreen = AppScreen.Menu
                        }
                        GameScreen(
                            controller = onlineVm,
                            onBack     = {
                                onlineVm.exitToMenu()
                                currentScreen = AppScreen.Menu
                            }
                        )
                    }
                }
            }
        }
    }
}
