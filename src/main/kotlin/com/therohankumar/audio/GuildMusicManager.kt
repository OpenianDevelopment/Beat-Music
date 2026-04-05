package com.therohankumar.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.therohankumar.audio.filters.FilterConfig
import com.therohankumar.audio.filters.FilterService
import net.dv8tion.jda.api.managers.AudioManager
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class GuildMusicManager(
    val player: AudioPlayer,
    val scheduler: TrackScheduler,
    private val audioManager: AudioManager,
    val guildId: Long,
    val playerManager: AudioPlayerManager
) {
    val sendHandler = AudioPlayerSendHandler(player)
    var filterConfig: FilterConfig = FilterConfig()

    /** Apply the current [filterConfig] to the player. Pass null to clear all filters. */
    fun applyFilters() {
        player.setFilterFactory(if (filterConfig.hasAnyFilter()) FilterService(filterConfig) else null)
    }

    private val idleExecutor = Executors.newSingleThreadScheduledExecutor()
    private var idleTask: ScheduledFuture<*>? = null

    init {
        scheduler.onQueueEmpty = {
            if (scheduler.autoplay) {
                // autoplay: RecommendationService wires into this via onAutoplayNeeded
                onAutoplayNeeded()
            } else {
                scheduleIdleDisconnect()
            }
        }
    }

    /** Replaced by RecommendationService after construction. */
    var onAutoplayNeeded: () -> Unit = { scheduleIdleDisconnect() }

    fun scheduleIdleDisconnect() {
        idleTask?.cancel(false)
        idleTask = idleExecutor.schedule({
            audioManager.closeAudioConnection()
        }, 5, TimeUnit.MINUTES)
    }

    fun cancelIdleDisconnect() {
        idleTask?.cancel(false)
        idleTask = null
    }

    fun destroy() {
        cancelIdleDisconnect()
        idleExecutor.shutdown()
        player.destroy()
    }
}
