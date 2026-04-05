package com.therohankumar.service

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.audio.GuildMusicManager
import com.therohankumar.entity.Track
import com.therohankumar.repository.PlayEventRepository
import com.therohankumar.repository.TrackRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val playEventRepository: PlayEventRepository,
    private val trackRepository: TrackRepository,
    private val playerManager: AudioPlayerManager
) {
    private val log = LoggerFactory.getLogger(RecommendationService::class.java)

    /**
     * Returns up to 5 recommended [Track] objects for [guildId] based on co-play history.
     * Returns empty list if there is not enough history yet.
     */
    fun recommend(guildId: Long): List<Track> {
        val seeds = playEventRepository.findRecentTrackIds(guildId, PageRequest.of(0, 10))
        if (seeds.isEmpty()) return emptyList()

        val candidates = playEventRepository.findCoPlayCandidates(guildId, seeds)
        if (candidates.isEmpty()) return emptyList()

        val trackIds = candidates.map { it[0] as String }
        return trackRepository.findAllById(trackIds)
    }

    /**
     * Called by [GuildMusicManager] when autoplay is on and the queue empties.
     * Loads and queues the top co-play recommendation.
     */
    fun triggerAutoplay(manager: GuildMusicManager) {
        Thread.ofVirtual().start {
            val candidates = recommend(manager.guildId)
            val best = candidates.firstOrNull() ?: run {
                manager.scheduleIdleDisconnect()
                return@start
            }

            val uri = "https://www.youtube.com/watch?v=${best.trackId}"
            manager.playerManager.loadItemOrdered(manager, uri, object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    track.userData = 0L // autoplay = no specific user
                    manager.scheduler.queue(track)
                    log.debug("Autoplay queued: {} for guild {}", track.info.title, manager.guildId)
                }
                override fun playlistLoaded(playlist: AudioPlaylist) {
                    playlist.tracks.firstOrNull()?.let { track ->
                        track.userData = 0L
                        manager.scheduler.queue(track)
                    }
                }
                override fun noMatches() { manager.scheduleIdleDisconnect() }
                override fun loadFailed(e: FriendlyException) { manager.scheduleIdleDisconnect() }
            })
        }
    }
}
