package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.exp

object SoundManager {
    private const val TAG = "SoundManager"
    private const val SAMPLE_RATE = 22050

    private var moveTrack: AudioTrack? = null
    private var collisionTrack: AudioTrack? = null
    private var winTrack: AudioTrack? = null

    var isSoundEnabled: Boolean = true

    init {
        try {
            generateMoveSound()
            generateCollisionSound()
            generateWinSound()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize SoundManager", e)
        }
    }

    private fun generateMoveSound() {
        val duration = 0.05 // 50ms
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = 1.0 - (i.toDouble() / numSamples)
            // Gently sliding frequency: 500Hz to 700Hz
            val freq = 500.0 + (i.toDouble() / numSamples) * 200.0
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * 32767.0 * envelope * 0.12).toInt().toShort()
        }
        moveTrack = createStaticTrack(samples)
    }

    private fun generateCollisionSound() {
        val duration = 0.08 // 80ms
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val envelope = exp(-5.0 * (i.toDouble() / numSamples)) // rapid decay
            // Low thud frequency: 120Hz to 60Hz
            val freq = 120.0 - (i.toDouble() / numSamples) * 60.0
            val wave = sin(2.0 * PI * freq * t)
            samples[i] = (wave * 32767.0 * envelope * 0.30).toInt().toShort()
        }
        collisionTrack = createStaticTrack(samples)
    }

    private fun generateWinSound() {
        val duration = 1.0 // 1 second
        val numSamples = (duration * SAMPLE_RATE).toInt()
        val samples = ShortArray(numSamples)
        
        // Define an arpeggio: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046Hz)
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val delays = doubleArrayOf(0.0, 0.12, 0.24, 0.36) // delay of onset in seconds
        
        for (i in 0 until numSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sumWave = 0.0
            
            for (noteIdx in notes.indices) {
                val delay = delays[noteIdx]
                if (t >= delay) {
                    val noteT = t - delay
                    // exponential decay envelope for each note
                    val noteEnvelope = exp(-3.0 * noteT)
                    val freq = notes[noteIdx]
                    sumWave += sin(2.0 * PI * freq * noteT) * noteEnvelope * 0.25
                }
            }
            
            // Overall master fade out
            val masterEnvelope = 1.0 - (i.toDouble() / numSamples)
            val finalWave = sumWave * masterEnvelope
            samples[i] = (finalWave.coerceIn(-1.0, 1.0) * 32767.0 * 0.45).toInt().toShort()
        }
        winTrack = createStaticTrack(samples)
    }

    private fun createStaticTrack(samples: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        return track
    }

    fun playMove() {
        if (!isSoundEnabled) return
        try {
            moveTrack?.let {
                it.stop()
                it.setPlaybackHeadPosition(0)
                it.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing move sound", e)
        }
    }

    fun playCollision() {
        if (!isSoundEnabled) return
        try {
            collisionTrack?.let {
                it.stop()
                it.setPlaybackHeadPosition(0)
                it.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing collision sound", e)
        }
    }

    fun playWin() {
        if (!isSoundEnabled) return
        try {
            winTrack?.let {
                it.stop()
                it.setPlaybackHeadPosition(0)
                it.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing win sound", e)
        }
    }

    fun release() {
        try {
            moveTrack?.release()
            collisionTrack?.release()
            winTrack?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing tracks", e)
        }
    }
}
