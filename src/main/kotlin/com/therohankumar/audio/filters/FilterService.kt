package com.therohankumar.audio.filters

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.audio.filters.impl.*

/**
 * Builds the DSP filter chain from a [FilterConfig].
 * Filters are ordered: karaoke → equalizer → timescale → tremolo → vibrato → rotation → distortion → echo → reverb
 *
 * Each filter is chained to the next via its constructor's [downstream] parameter.
 * All filters are returned so LavaPlayer can call lifecycle methods (seek/flush/close) on each.
 */
class FilterService(private val config: FilterConfig) : PcmFilterFactory {

    override fun buildChain(
        track: AudioTrack?,
        format: AudioDataFormat,
        output: UniversalPcmAudioFilter
    ): MutableList<AudioFilter> {
        val sr = format.sampleRate
        val ch = format.channelCount
        val filters = mutableListOf<AudioFilter>()

        // Build chain in reverse (tail → head). Each step wraps the previous downstream.
        var downstream: FloatPcmAudioFilter = output

        // ── Reverb (last in chain = outermost wrapper) ─────────────────────────
        config.reverb?.let {
            val f = ReverbFilter(downstream, it.roomSize, it.damping, sr, ch)
            filters.add(f); downstream = f
        }

        // ── Echo ───────────────────────────────────────────────────────────────
        config.echo?.let {
            val f = EchoFilter(downstream, it.delay, it.decay, sr, ch)
            filters.add(f); downstream = f
        }

        // ── Distortion ─────────────────────────────────────────────────────────
        config.distortion?.let {
            val f = DistortionFilter(downstream, it.sinOffset, it.cosOffset, it.tanOffset, it.scale, it.offset)
            filters.add(f); downstream = f
        }

        // ── Rotation (8D) ──────────────────────────────────────────────────────
        config.rotation?.let {
            val f = RotationFilter(downstream, it.hz, sr)
            filters.add(f); downstream = f
        }

        // ── Vibrato ────────────────────────────────────────────────────────────
        config.vibrato?.let {
            val f = VibratoFilter(downstream, it.frequency, it.depth, sr, ch)
            filters.add(f); downstream = f
        }

        // ── Tremolo ────────────────────────────────────────────────────────────
        config.tremolo?.let {
            val f = TremoloFilter(downstream, it.frequency, it.depth, sr)
            filters.add(f); downstream = f
        }

        // ── Timescale (nightcore / vaporwave) ──────────────────────────────────
        val speed = when {
            config.nightcore -> 1.3
            config.vaporwave -> 0.8
            else -> null
        }
        speed?.let {
            val f = TimescaleFilter(downstream, it)
            filters.add(f); downstream = f
        }

        // ── Bassboost (low-shelf IIR EQ) ──────────────────────────────────────
        config.bassboost?.let { gainDb ->
            val f = BassBoostFilter(downstream, gainDb, sr)
            filters.add(f); downstream = f
        }

        // ── Karaoke (first in chain = closest to raw audio) ───────────────────
        config.karaoke?.let {
            val f = KaraokeFilter(downstream, it.level, it.monoLevel)
            filters.add(f); downstream = f
        }

        return filters
    }
}
