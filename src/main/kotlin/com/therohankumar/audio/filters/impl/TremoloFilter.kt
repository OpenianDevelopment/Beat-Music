package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter
import kotlin.math.sin

/**
 * Amplitude modulation using a sine LFO.
 * Creates a "trembling" volume oscillation effect.
 */
class TremoloFilter(
    downstream: FloatPcmAudioFilter,
    private val frequency: Float,   // LFO rate in Hz (0.1 – 20)
    private val depth: Float,       // modulation depth (0 – 1)
    private val sampleRate: Int
) : BaseAudioFilter(downstream) {

    private var phase = 0.0
    private val phaseStep = 2.0 * Math.PI * frequency / sampleRate

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            // multiplier oscillates between (1 - depth) and 1.0
            val multiplier = ((1.0 - depth) + depth * (0.5 + 0.5 * sin(phase))).toFloat()
            for (ch in input.indices) {
                input[ch][i] *= multiplier
            }
            phase += phaseStep
            if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
        }
        downstream.process(input, offset, length)
    }
}
