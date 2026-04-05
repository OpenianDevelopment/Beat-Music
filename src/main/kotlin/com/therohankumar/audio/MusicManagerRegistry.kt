package com.therohankumar.audio

import com.therohankumar.service.EventRecordingService
import com.therohankumar.service.RecommendationService
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import jakarta.annotation.PreDestroy
import net.dv8tion.jda.api.entities.Guild
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MusicManagerRegistry(
    private val playerManager: AudioPlayerManager,
    private val eventRecordingService: EventRecordingService,
    @param:Lazy private val recommendationService: RecommendationService
) {

    private val managers = ConcurrentHashMap<Long, GuildMusicManager>()

    fun getOrCreate(guild: Guild): GuildMusicManager {
        return managers.getOrPut(guild.idLong) {
            val player = playerManager.createPlayer()
            val scheduler = TrackScheduler(player)
            player.addListener(scheduler)

            val manager = GuildMusicManager(player, scheduler, guild.audioManager, guild.idLong, playerManager)
            guild.audioManager.sendingHandler = manager.sendHandler

            // Wire event recording callbacks
            scheduler.onTrackStart   = { track -> eventRecordingService.onTrackStart(guild.idLong, track) }
            scheduler.onTrackSkipped = { track, posMs -> eventRecordingService.onTrackSkipped(guild.idLong, track, posMs) }

            // Wire autoplay
            manager.onAutoplayNeeded = { recommendationService.triggerAutoplay(manager) }

            manager
        }
    }

    fun get(guildId: Long): GuildMusicManager? = managers[guildId]

    fun getActiveSessionCount(): Int = managers.values.count { it.player.playingTrack != null }

    fun remove(guildId: Long) {
        managers.remove(guildId)?.destroy()
    }

    @PreDestroy
    fun shutdown() {
        managers.values.forEach { it.destroy() }
        managers.clear()
    }
}
