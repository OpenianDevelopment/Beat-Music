package com.therohankumar.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Temporarily holds search results between the /play modal being shown
 * and the user submitting their selection. Keyed by userId.
 * Results are removed on retrieval or when the user plays something new.
 */
@Component
class SearchResultStore {
    private val store = ConcurrentHashMap<Long, List<AudioTrack>>()

    fun put(userId: Long, tracks: List<AudioTrack>) {
        store[userId] = tracks
    }

    fun take(userId: Long): List<AudioTrack>? = store.remove(userId)
}
