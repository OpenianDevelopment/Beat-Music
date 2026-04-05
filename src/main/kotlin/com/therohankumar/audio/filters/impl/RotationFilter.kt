package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter
import kotlin.math.cos
import kotlin.math.sin

/**
 * 8D audio — rotates the stereo field at a given rate using a rotation matrix.
 * Only meaningful on stereo (2-channel) output.
 */
class RotationFilter(
    downstream: FloatPcmAudioFilter,
    hz: Float,                      // rotation speed in Hz (e.g. 0.2)
    private val sampleRate: Int
) : BaseAudioFilter(downstream) {

    private var angle = 0.0
    private val angleStep = 2.0 * Math.PI * hz / sampleRate

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        if (input.size < 2) {
            downstream.process(input, offset, length)
            return
        }
        for (i in offset until offset + length) {
            val cosA = cos(angle).toFloat()
            val sinA = sin(angle).toFloat()
            val l = input[0][i]
            val r = input[1][i]
            input[0][i] = l * cosA - r * sinA
            input[1][i] = l * sinA + r * cosA
            angle += angleStep
            if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
        }
        downstream.process(input, offset, length)
    }
}
