package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter
import kotlin.math.sin

/**
 * Pitch modulation via a variable-length delay line.
 * Shifts the read pointer at an oscillating rate to create pitch variation.
 */
class VibratoFilter(
    downstream: FloatPcmAudioFilter,
    frequency: Float,               // LFO rate in Hz (0.1 – 14)
    depth: Float,                   // modulation depth (0 – 1), max delay = 10ms * depth
    private val sampleRate: Int,
    channels: Int
) : BaseAudioFilter(downstream) {

    private val maxDelaySamples = ((10f * depth) * sampleRate / 1000f).toInt().coerceAtLeast(2)
    private val bufferSize = maxDelaySamples * 2 + 2
    private val buffers = Array(channels) { FloatArray(bufferSize) }
    private var writeIdx = 0
    private var phase = 0.0
    private val phaseStep = 2.0 * Math.PI * frequency / sampleRate

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            val delaySamples = (maxDelaySamples * (0.5 + 0.5 * sin(phase))).toInt()
            val readIdx = (writeIdx - delaySamples + bufferSize) % bufferSize

            for (ch in 0 until minOf(input.size, buffers.size)) {
                buffers[ch][writeIdx] = input[ch][i]
                input[ch][i] = buffers[ch][readIdx]
            }

            writeIdx = (writeIdx + 1) % bufferSize
            phase += phaseStep
            if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
        }
        downstream.process(input, offset, length)
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        for (buf in buffers) buf.fill(0f)
        writeIdx = 0
        super.seekPerformed(requestedTime, providedTime)
    }
}
