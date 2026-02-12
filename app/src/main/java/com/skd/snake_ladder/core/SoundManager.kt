package com.skd.snake_ladder.core

import android.content.Context
import android.media.MediaPlayer
import com.skd.snake_ladder.R

class SoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playDiceSound() {
        release()
        mediaPlayer = MediaPlayer.create(context, R.raw.dice_roll)
        mediaPlayer?.setOnCompletionListener { release() }
        mediaPlayer?.start()
    }

    fun playWinSound() {
        release()
        mediaPlayer = MediaPlayer.create(context, R.raw.win_sound)
        mediaPlayer?.setOnCompletionListener { release() }
        mediaPlayer?.start()
    }

    private fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
