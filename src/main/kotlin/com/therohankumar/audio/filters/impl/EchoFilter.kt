package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter

/**
 * Simple delay-line echo with feedback.
 */
class EchoFilter(
    downstream: FloatPcmAudioFilter,
    delaySeconds: Float,            // delay in seconds (0.05 – 2.0)
    private val decay: Float,       // feedback coefficient (0.0 – 0.9)
    sampleRate: Int,
    channels: Int
) : BaseAudioFilter(downstream) {

    private val bufferSize = (delaySeconds * sampleRate).toInt().coerceAtLeast(1)
    private val buffers = Array(channels) { FloatArray(bufferSize) }
    private var writeIdx = 0

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            for (ch in 0 until minOf(input.size, buffers.size)) {
                val delayed = buffers[ch][writeIdx]
                buffers[ch][writeIdx] = input[ch][i] + delayed * decay
                input[ch][i] = (input[ch][i] + delayed).coerceIn(-1f, 1f)
            }
            writeIdx = (writeIdx + 1) % bufferSize
        }
        downstream.process(input, offset, length)
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        for (buf in buffers) buf.fill(0f)
        writeIdx = 0
        super.seekPerformed(requestedTime, providedTime)
    }
}
