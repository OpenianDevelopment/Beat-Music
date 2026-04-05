package com.therohankumar.audio.filters

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter

abstract class BaseAudioFilter(protected val downstream: FloatPcmAudioFilter) : FloatPcmAudioFilter {
    override fun seekPerformed(requestedTime: Long, providedTime: Long) =
        downstream.seekPerformed(requestedTime, providedTime)
    override fun flush() = downstream.flush()
    override fun close() = downstream.close()
}
