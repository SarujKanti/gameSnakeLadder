package com.skd.snake_ladder.core

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import com.skd.snake_ladder.R
import com.skd.snake_ladder.data.SettingsRepository

class SoundManager(private val context: Context) {

    private val settings = SettingsRepository(context)
    private var mediaPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // ── Dice / Win (MediaPlayer) ──────────────────────────────────────────────

    fun playDiceSound() {
        if (!settings.soundEnabled) return
        release()
        mediaPlayer = MediaPlayer.create(context, R.raw.dice_roll)
        mediaPlayer?.setOnCompletionListener { release() }
        mediaPlayer?.start()
    }

    fun playWinSound() {
        if (!settings.soundEnabled) return
        release()
        mediaPlayer = MediaPlayer.create(context, R.raw.win_sound)
        mediaPlayer?.setOnCompletionListener { release() }
        mediaPlayer?.start()
    }

    // ── Snake sound — low descending tone ─────────────────────────────────────

    fun playSnakeSound() {
        if (!settings.soundEnabled) return
        Thread {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
                // Two low descending beeps
                tg.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 350)
                Thread.sleep(200)
                tg.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 350)
                Thread.sleep(400)
                tg.release()
            } catch (_: Exception) { }
        }.start()
    }

    // ── Ladder sound — cheerful ascending tones ───────────────────────────────

    fun playLadderSound() {
        if (!settings.soundEnabled) return
        Thread {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP,  120)
                Thread.sleep(90)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP,  120)
                Thread.sleep(90)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
                Thread.sleep(300)
                tg.release()
            } catch (_: Exception) { }
        }.start()
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun cleanup() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
