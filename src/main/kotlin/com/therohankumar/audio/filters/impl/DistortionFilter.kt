package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Waveshaping distortion combining sin, cos, and tan offsets.
 */
class DistortionFilter(
    downstream: FloatPcmAudioFilter,
    private val sinOffset: Float,
    private val cosOffset: Float,
    private val tanOffset: Float,
    private val scale: Float = 1f,
    private val offset: Float = 0f
) : BaseAudioFilter(downstream) {

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            for (ch in input.indices) {
                val x = input[ch][i].toDouble()
                var result = 0.0
                if (sinOffset != 0f) result += sin(x + sinOffset)
                if (cosOffset != 0f) result += cos(x + cosOffset)
                if (tanOffset != 0f) result += tan(x * tanOffset).coerceIn(-3.0, 3.0)
                if (result == 0.0) result = x
                input[ch][i] = (result * scale + this.offset).toFloat().coerceIn(-1f, 1f)
            }
        }
        downstream.process(input, offset, length)
    }
}
