package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter

/**
 * Simple linear-interpolation resampler that changes playback rate.
 * Speed > 1 = faster + higher pitch (nightcore).
 * Speed < 1 = slower + lower pitch (vaporwave).
 *
 * This is a rate change (speed and pitch coupled), not a true
 * time-stretching implementation. Suitable for nightcore / vaporwave presets.
 */
class TimescaleFilter(
    downstream: FloatPcmAudioFilter,
    private val speed: Double       // e.g. 1.3 for nightcore, 0.8 for vaporwave
) : BaseAudioFilter(downstream) {

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        if (speed == 1.0) {
            downstream.process(input, offset, length)
            return
        }

        val outputLength = (length / speed).toInt().coerceAtLeast(1)
        val out = Array(input.size) { FloatArray(outputLength) }

        for (i in 0 until outputLength) {
            val srcPos = i * speed
            val srcIdx = srcPos.toInt().coerceIn(0, length - 1)
            val nextIdx = (srcIdx + 1).coerceIn(0, length - 1)
            val frac = (srcPos - srcIdx).toFloat()

            for (ch in input.indices) {
                out[ch][i] = input[ch][offset + srcIdx] * (1f - frac) +
                             input[ch][offset + nextIdx] * frac
            }
        }

        downstream.process(out, 0, outputLength)
    }
}
