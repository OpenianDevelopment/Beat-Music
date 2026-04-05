package com.therohankumar.audio.filters.impl

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.therohankumar.audio.filters.BaseAudioFilter

/**
 * Freeverb-inspired stereo reverb using parallel comb filters + series all-pass filters.
 *
 * roomSize: controls comb filter feedback (larger = longer reverb tail)
 * damping:  controls high-frequency absorption (higher = darker reverb)
 */
class ReverbFilter(
    downstream: FloatPcmAudioFilter,
    roomSize: Float,                // 0.0 – 1.0
    damping: Float,                 // 0.0 – 1.0
    sampleRate: Int,
    channels: Int
) : BaseAudioFilter(downstream) {

    // Comb filter delay lengths (samples at 44100 Hz, scaled for actual sample rate)
    private val combDelays = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617)
    private val stereoSpread = 23
    private val allPassDelays = intArrayOf(556, 441, 341, 225)
    private val allPassFeedback = 0.5f

    private val feedback = 0.28f + roomSize * 0.6f        // 0.28 – 0.88
    private val dampValue = damping * 0.4f                  // soften the damping scale

    // Stereo comb filters (8 per channel)
    private val combL = Array(8) { i -> CombFilter(scale(combDelays[i], sampleRate), feedback, dampValue) }
    private val combR = Array(8) { i -> CombFilter(scale(combDelays[i] + stereoSpread, sampleRate), feedback, dampValue) }

    // All-pass filters per channel
    private val apL = Array(4) { i -> AllPassFilter(scale(allPassDelays[i], sampleRate), allPassFeedback) }
    private val apR = Array(4) { i -> AllPassFilter(scale(allPassDelays[i] + stereoSpread, sampleRate), allPassFeedback) }

    private val wet = 0.33f
    private val dry = 0.67f

    private fun scale(samples: Int, sr: Int) = (samples.toLong() * sr / 44100).toInt().coerceAtLeast(1)

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            val inL = if (input.isNotEmpty()) input[0][i] else 0f
            val inR = if (input.size > 1) input[1][i] else inL

            val mono = (inL + inR) * 0.015f  // attenuate going into reverb

            // Sum parallel comb filters
            var outL = 0f
            var outR = 0f
            for (c in 0..7) {
                outL += combL[c].process(mono)
                outR += combR[c].process(mono)
            }

            // Series all-pass filters
            for (ap in apL) outL = ap.process(outL)
            for (ap in apR) outR = ap.process(outR)

            if (input.isNotEmpty()) input[0][i] = (inL * dry + outL * wet).coerceIn(-1f, 1f)
            if (input.size > 1) input[1][i] = (inR * dry + outR * wet).coerceIn(-1f, 1f)
        }
        downstream.process(input, offset, length)
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        combL.forEach { it.reset() }; combR.forEach { it.reset() }
        apL.forEach { it.reset() };   apR.forEach { it.reset() }
        super.seekPerformed(requestedTime, providedTime)
    }

    // ── Inner DSP helpers ──────────────────────────────────────────────────────

    private class CombFilter(size: Int, private val feedback: Float, private val damp: Float) {
        private val buf = FloatArray(size)
        private var idx = 0
        private var filterStore = 0f

        fun process(input: Float): Float {
            val output = buf[idx]
            filterStore = output * (1f - damp) + filterStore * damp
            buf[idx] = input + filterStore * feedback
            idx = (idx + 1) % buf.size
            return output
        }
        fun reset() { buf.fill(0f); idx = 0; filterStore = 0f }
    }

    private class AllPassFilter(size: Int, private val feedback: Float) {
        private val buf = FloatArray(size)
        private var idx = 0

        fun process(input: Float): Float {
            val bufOut = buf[idx]
            buf[idx] = input + bufOut * feedback
            idx = (idx + 1) % buf.size
            return bufOut - input
        }
        fun reset() { buf.fill(0f); idx = 0 }
    }
}
