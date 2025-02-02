package com.therohankumar.modules

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.modules.filters.reverb.ReverbPcmAudioFilter
import me.rohank05.echo.EchoPcmAudioFilter

data class FilterSettings(
    var isNightcore: Boolean = false,
    var isEightD: Boolean = false,
    var isVibrato: Boolean = false,
    var isTremolo: Boolean = false,
    var isBassBoost: Boolean = false,
    var isEcho: Boolean = false,
    var isReverb: Boolean = false
)

class Filters(private val audioPlayer: AudioPlayer) {
    private var settings = FilterSettings()

    // Extension property to check if any filter is enabled
    private val isAnyFilterEnabled: Boolean
        get() = with(settings) {
            isNightcore || isEightD || isVibrato || isTremolo || isBassBoost || isEcho || isReverb
        }

    // Function to update individual filter settings
    fun updateFilter(update: FilterSettings.() -> Unit) {
        settings.update()
        updatePlayerFilter()
    }

    // Reset all filters to default state
    fun resetFilters() {
        settings = FilterSettings()
        updatePlayerFilter()
    }

    // Update the audio player's filter chain
    private fun updatePlayerFilter() {
        if(isAnyFilterEnabled) {
            audioPlayer.setFilterFactory(this::buildChain)
            return
        }
        audioPlayer.setFilterFactory(null)
    }

    private fun buildChain(
        audioTrack: AudioTrack,
        format: AudioDataFormat,
        downstream: UniversalPcmAudioFilter
    ): List<AudioFilter> = buildList {
        var currentFilter: FloatPcmAudioFilter = downstream

        // Apply Nightcore filter
        if (settings.isNightcore) {
            TimescalePcmAudioFilter(currentFilter, format.channelCount, format.sampleRate).apply {
                pitch = 1.29
                speed = 1.29
                currentFilter = this
                add(this)
            }
        }

        // Apply 8D filter
        if (settings.isEightD) {
            RotationPcmAudioFilter(currentFilter, format.sampleRate).apply {
                setRotationSpeed(0.1)
                currentFilter = this
                add(this)
            }
        }

        // Apply Vibrato filter
        if (settings.isVibrato) {
            VibratoPcmAudioFilter(currentFilter, format.channelCount, format.sampleRate).apply {
                frequency = 4.0f
                currentFilter = this
                add(this)
            }
        }

        // Apply Tremolo filter
        if (settings.isTremolo) {
            TremoloPcmAudioFilter(currentFilter, format.channelCount, format.sampleRate).apply {
                frequency = 1.0f
                depth = 0.8f
                currentFilter = this
                add(this)
            }
        }

        if (settings.isBassBoost) {
            val bands = FloatArray(15) { index ->
                when (index) {
                    0 -> 0.25f
                    1 -> 0.15f
                    2 -> 0.10f
                    3 -> 0.05f
                    4 -> 0.02f
                    else -> 0.0f
                }
            }

            Equalizer(format.channelCount, currentFilter, bands).apply {
                currentFilter = this
                add(this)
            }
        }

        // Apply Echo filter
        if (settings.isEcho) {
            EchoPcmAudioFilter(currentFilter, format.channelCount, format.sampleRate).apply {
                setDelay(1.0)
                setDecay(0.5f)
                add(this)
            }
        }

        if (settings.isReverb) {
            ReverbPcmAudioFilter(currentFilter, format).apply {
                setPreset(ReverbPcmAudioFilter.RoomPreset.AUDITORIUM)
                currentFilter = this
                add(this)
            }
        }
    }.asReversed()

    // Getter functions for filter states
    fun getFilterStates(): FilterSettings = settings.copy()
}