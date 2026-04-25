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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skd.snake_ladder.online.OnlineGameViewModel
import com.skd.snake_ladder.online.OnlineUiState
import com.skd.snake_ladder.ui.theme.Snake_ladderTheme
import com.skd.snake_ladder.ui.view.GameScreen
import com.skd.snake_ladder.ui.view.ModeSelectionScreen
import com.skd.snake_ladder.ui.view.OnlineLobbyScreen
import com.skd.snake_ladder.ui.view.SplashScreen
import com.skd.snake_ladder.viewmodel.GameViewModel
import com.skd.snake_ladder.viewmodel.SettingsViewModel

private enum class AppScreen { Splash, Menu, LocalGame, OnlineLobby, OnlineGame }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val localVm:    GameViewModel       = viewModel()
            val onlineVm:   OnlineGameViewModel = viewModel()
            val settingsVm: SettingsViewModel   = viewModel()

            val onlineUi         = onlineVm.uiState.collectAsStateWithLifecycle().value
            val opponentLeftName = onlineVm.opponentLeftEvent.collectAsStateWithLifecycle().value
            val themeMode        = settingsVm.themeMode.collectAsStateWithLifecycle().value

            var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

            // Auto-navigate to online game when Firebase signals "playing"
            LaunchedEffect(onlineUi) {
                if (onlineUi is OnlineUiState.InGame && currentScreen == AppScreen.OnlineLobby) {
                    currentScreen = AppScreen.OnlineGame
                }
            }

            Snake_ladderTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = Color(0xFF070D1A)
                ) {
                    when (currentScreen) {

                        // ── Splash ────────────────────────────────────────────
                        AppScreen.Splash -> {
                            SplashScreen(onDone = { currentScreen = AppScreen.Menu })
                        }

                        // ── Menu ──────────────────────────────────────────────
                        AppScreen.Menu -> {
                            ModeSelectionScreen(
                                settingsVm           = settingsVm,
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

                        // ── Local game ────────────────────────────────────────
                        AppScreen.LocalGame -> {
                            BackHandler {
                                localVm.exitToMenu()
                                currentScreen = AppScreen.Menu
                            }
                            GameScreen(
                                controller  = localVm,
                                settingsVm  = settingsVm,
                                onBack      = {
                                    localVm.exitToMenu()
                                    currentScreen = AppScreen.Menu
                                }
                            )
                        }

                        // ── Online lobby ──────────────────────────────────────
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

                        // ── Online game ───────────────────────────────────────
                        AppScreen.OnlineGame -> {
                            BackHandler {
                                onlineVm.exitToMenu()
                                currentScreen = AppScreen.Menu
                            }
                            GameScreen(
                                controller  = onlineVm,
                                settingsVm  = settingsVm,
                                onBack      = {
                                    onlineVm.exitToMenu()
                                    currentScreen = AppScreen.Menu
                                }
                            )
                        }
                    }

                    // ── Opponent-left dialog (2-player online) ────────────────
                    if (opponentLeftName != null) {
                        AlertDialog(
                            onDismissRequest = {},
                            confirmButton = {
                                Button(
                                    onClick = {
                                        onlineVm.dismissOpponentLeft()
                                        onlineVm.exitToMenu()
                                        currentScreen = AppScreen.Menu
                                    },
                                    shape  = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1565C0)
                                    )
                                ) {
                                    Text(
                                        text       = "Back to Menu",
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = 14.sp
                                    )
                                }
                            },
                            title = {
                                Text(
                                    text       = "Opponent Left",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 18.sp,
                                    color      = Color(0xFFFFD700)
                                )
                            },
                            text = {
                                Text(
                                    text     = "$opponentLeftName has left the game.",
                                    fontSize = 15.sp,
                                    color    = Color(0xFFECEFF1)
                                )
                            },
                            shape          = RoundedCornerShape(20.dp),
                            containerColor = Color(0xFF111C33)
                        )
                    }
                }
            }
        }
    }
}
