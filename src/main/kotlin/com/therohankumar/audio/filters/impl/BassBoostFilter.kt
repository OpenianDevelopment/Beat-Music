package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter
import kotlin.math.*

/**
 * Simple low-shelf IIR EQ filter for bass boost.
 * Shelving frequency ~200 Hz; gain applied in dB.
 */
class BassBoostFilter(
    downstream: FloatPcmAudioFilter,
    gainDb: Float,
    sampleRate: Int
) : BaseAudioFilter(downstream) {

    // Biquad coefficients (low shelf at ~200 Hz)
    private val b0: Double
    private val b1: Double
    private val b2: Double
    private val a1: Double
    private val a2: Double

    // Per-channel biquad state
    private var x1 = DoubleArray(2)
    private var x2 = DoubleArray(2)
    private var y1 = DoubleArray(2)
    private var y2 = DoubleArray(2)

    init {
        val A = 10.0.pow(gainDb / 40.0)
        val w0 = 2.0 * PI * 200.0 / sampleRate
        val cosW = cos(w0)
        val sinW = sin(w0)
        val S = 1.0  // shelf slope
        val alpha = sinW / 2.0 * sqrt((A + 1.0 / A) * (1.0 / S - 1.0) + 2.0)

        val a0 = (A + 1) + (A - 1) * cosW + 2 * sqrt(A) * alpha
        b0 = A * ((A + 1) - (A - 1) * cosW + 2 * sqrt(A) * alpha) / a0
        b1 = 2 * A * ((A - 1) - (A + 1) * cosW) / a0
        b2 = A * ((A + 1) - (A - 1) * cosW - 2 * sqrt(A) * alpha) / a0
        a1 = -2 * ((A - 1) + (A + 1) * cosW) / a0
        a2 = ((A + 1) + (A - 1) * cosW - 2 * sqrt(A) * alpha) / a0
    }

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        if (x1.size < input.size) {
            x1 = DoubleArray(input.size)
            x2 = DoubleArray(input.size)
            y1 = DoubleArray(input.size)
            y2 = DoubleArray(input.size)
        }
        for (i in offset until offset + length) {
            for (ch in input.indices) {
                val x = input[ch][i].toDouble()
                val y = b0 * x + b1 * x1[ch] + b2 * x2[ch] - a1 * y1[ch] - a2 * y2[ch]
                x2[ch] = x1[ch]; x1[ch] = x
                y2[ch] = y1[ch]; y1[ch] = y
                input[ch][i] = y.toFloat().coerceIn(-1f, 1f)
            }
        }
        downstream.process(input, offset, length)
    }
}
