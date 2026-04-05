package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter

/**
 * Vocal reduction by subtracting the center (shared L+R) component.
 * Works best on tracks where vocals are panned center.
 */
class KaraokeFilter(
    downstream: FloatPcmAudioFilter,
    private val level: Float,       // center reduction strength (0 – 1)
    private val monoLevel: Float    // mono blend level (0 – 1)
) : BaseAudioFilter(downstream) {

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        if (input.size < 2) {
            downstream.process(input, offset, length)
            return
        }
        for (i in offset until offset + length) {
            val l = input[0][i]
            val r = input[1][i]
            val center = (l + r) * 0.5f * level
            input[0][i] = (l - center + (l + r) * 0.5f * monoLevel).coerceIn(-1f, 1f)
            input[1][i] = (r - center + (l + r) * 0.5f * monoLevel).coerceIn(-1f, 1f)
        }
        downstream.process(input, offset, length)
    }
}
