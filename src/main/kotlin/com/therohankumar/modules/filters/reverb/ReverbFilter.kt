package com.therohankumar.modules.filters.reverb

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat

class ReverbPcmAudioFilter(
    private val downstream: FloatPcmAudioFilter,
    format: AudioDataFormat
) : FloatPcmAudioFilter {

    enum class RoomPreset(
        val roomSize: Float,
        val decay: Float,
        val wetLevel: Float
    ) {
        SMALL_ROOM(30.0f, 0.5f, 0.3f),
        MEDIUM_ROOM(50.0f, 0.6f, 0.35f),
        LARGE_ROOM(75.0f, 0.7f, 0.4f),
        CONCERT_HALL(100.0f, 0.8f, 0.45f),
        AUDITORIUM(150.0f, 0.75f, 0.4f),
        STADIUM(250.0f, 0.85f, 0.5f),
        CATHEDRAL(300.0f, 0.9f, 0.6f),
        CHURCH(200.0f, 0.8f, 0.5f),
        CAVE(400.0f, 0.9f, 0.6f),
        GARAGE(40.0f, 0.6f, 0.35f),
        THEATER(125.0f, 0.7f, 0.4f)
    }

    private val channels = format.channelCount
    private val sampleRate = format.sampleRate

    var decay: Float = 0.5f
        set(value) {
            require(value in 0.0f..1.0f) { "Decay must be between 0 and 1" }
            field = value
        }

    var wetLevel: Float = 0.3f
        set(value) {
            require(value in 0.0f..1.0f) { "Wet level must be between 0 and 1" }
            field = value
        }

    var roomSize: Float = 50.0f
        set(value) {
            require(value in 1.0f..1000.0f) { "Room size must be between 1 and 1000 ms" }
            field = value
            updateDelayLength()
        }

    private var delayLength: Int = (sampleRate * (roomSize / 1000.0f)).toInt()
    private var delayBuffers: Array<FloatArray> = Array(channels) { FloatArray(delayLength) }
    private var writePosition: Int = 0

    private fun updateDelayLength() {
        delayLength = (sampleRate * (roomSize / 1000.0f)).toInt()
        delayBuffers = Array(channels) { FloatArray(delayLength) }
        writePosition = 0
    }

    fun setPreset(preset: RoomPreset) {
        decay = preset.decay
        wetLevel = preset.wetLevel
        roomSize = preset.roomSize
    }

    override fun process(input: Array<FloatArray>, offset: Int, length: Int) {
        val output = Array(channels) { FloatArray(length) }

        for (i in 0 until length) {
            val readPosition = (writePosition - delayLength + delayLength) % delayLength

            for (channel in 0 until channels) {
                // Read from delay buffer
                val delayedSample = delayBuffers[channel][readPosition]

                // Calculate new sample with decay
                val newSample = input[channel][offset + i] + (delayedSample * decay)

                // Mix dry and wet signals
                output[channel][i] = input[channel][offset + i] * (1 - wetLevel) +
                        newSample * wetLevel

                // Update delay buffer
                delayBuffers[channel][writePosition] = newSample
            }

            writePosition = (writePosition + 1) % delayLength
        }

        downstream.process(output, 0, length)
    }

    override fun flush() {
        delayBuffers.forEach { it.fill(0f) }
        writePosition = 0
        downstream.flush()
    }

    override fun close() {
        flush()
    }

    override fun seekPerformed(requestedTime: Long, providedTime: Long) {
        // Clear buffers when seeking occurs
        flush()
        downstream.seekPerformed(requestedTime, providedTime)
    }
}