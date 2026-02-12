package com.skd.snake_ladder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skd.snake_ladder.ui.view.GameScreen
import com.skd.snake_ladder.ui.view.ModeSelectionScreen
import com.skd.snake_ladder.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val viewModel: GameViewModel = viewModel()
            val state = viewModel.state.collectAsStateWithLifecycle().value

            if (state.gameMode == null) {

                ModeSelectionScreen(
                    onModeSelected = {
                        viewModel.setGameMode(it)
                    }
                )

            } else {

                GameScreen(viewModel)

            }
        }

    }
}
