package com.therohankumar.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.entity.PlayEvent
import com.therohankumar.entity.Track
import com.therohankumar.repository.PlayEventRepository
import com.therohankumar.repository.TrackRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.ConcurrentHashMap

@Service
class EventRecordingService(
    private val playEventRepository: PlayEventRepository,
    private val trackRepository: TrackRepository,
    private val objectMapper: ObjectMapper,
    private val transactionTemplate: TransactionTemplate
) {
    private val log = LoggerFactory.getLogger(EventRecordingService::class.java)

    // guildId → current PlayEvent.id (for skip tracking)
    private val currentEventId = ConcurrentHashMap<Long, Long>()

    fun onTrackStart(guildId: Long, track: AudioTrack) {
        Thread.ofVirtual().start {
            try {
                upsertTrack(track)
                val event = playEventRepository.save(PlayEvent(
                    guildId      = guildId,
                    userId       = track.userData as? Long ?: 0L,
                    trackId      = track.info.identifier,
                    trackTitle   = track.info.title,
                    trackAuthor  = track.info.author,
                    trackDuration = track.duration.toInt().takeIf { it > 0 },
                    source       = track.sourceManager?.sourceName,
                    requestedBy  = track.userData as? Long
                ))
                currentEventId[guildId] = event.id
            } catch (e: Exception) {
                log.warn("Failed to record track start for guild {}: {}", guildId, e.message)
            }
        }
    }

    fun onTrackSkipped(guildId: Long, track: AudioTrack, positionMs: Long) {
        val eventId = currentEventId.remove(guildId) ?: return
        Thread.ofVirtual().start {
            try {
                playEventRepository.updateSkippedAt(eventId, positionMs.toInt())
            } catch (e: Exception) {
                log.warn("Failed to record skip for guild {}: {}", guildId, e.message)
            }
        }
    }

    fun upsertTrack(track: AudioTrack) {
        transactionTemplate.execute {
            val existing = trackRepository.findById(track.info.identifier)
            if (existing.isPresent) {
                trackRepository.incrementPlayCount(track.info.identifier)
            } else {
                trackRepository.save(Track(
                    trackId   = track.info.identifier,
                    title     = track.info.title,
                    author    = track.info.author,
                    durationMs = track.duration.toInt().takeIf { it > 0 },
                    thumbnail = track.info.artworkUrl,
                    source    = track.sourceManager?.sourceName,
                    playCount = 1
                ))
            }
        }
    }
}
