package com.skd.snake_ladder.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skd.snake_ladder.domain.model.GameMode

@Composable
fun ModeSelectionScreen(
    onModeSelected: (GameMode) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Select Game Mode")

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onModeSelected(GameMode.VS_COMPUTER) }
        ) {
            Text("👤 vs 🤖 Computer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onModeSelected(GameMode.TWO_PLAYERS) }
        ) {
            Text("👤 vs 👤 2 Players")
        }
    }
}
