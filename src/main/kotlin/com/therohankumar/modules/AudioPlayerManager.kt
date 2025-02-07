package com.therohankumar.modules

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import com.therohankumar.ENV

object AudioPlayerManager {
    val audioPlayerManager = DefaultAudioPlayerManager().apply {
        configuration.setFrameBufferFactory { i, audioDataFormat, atomicBoolean ->
            NonAllocatingAudioFrameBuffer(i, audioDataFormat, atomicBoolean)
        }
        this.configuration.isFilterHotSwapEnabled = true
        val ytSourceManager = dev.lavalink.youtube.YoutubeAudioSourceManager(true)
        ENV.IPV6_BLOCK?.let { ipv6Block ->
            try {
                val block = listOf(ipv6Block).map { Ipv6Block(it) }
                val routePlanner = RotatingNanoIpRoutePlanner(block)
                val rotator = YoutubeIpRotatorSetup(routePlanner)
                rotator.forConfiguration(ytSourceManager.httpInterfaceManager, false).withMainDelegateFilter(ytSourceManager.contextFilter).setup()
            }
            catch (ex: Exception) {
                println(ex)
            }
        }
        this.registerSourceManagers(ytSourceManager)
        AudioSourceManagers.registerRemoteSources(this)
        AudioSourceManagers.registerLocalSource(this)
    }
    private val musicManagers = mutableMapOf<Long, GuildMusicManager>()

    fun getMusicManager(guildId: Long): GuildMusicManager {
        return musicManagers.getOrPut(guildId) {
            GuildMusicManager(audioPlayerManager.createPlayer())
        }
    }

    fun destroyMusicManager(guildId: Long) {
        val manager = musicManagers[guildId]
        manager?.player?.destroy()
        musicManagers.remove(guildId)
    }

    fun musicManagerExist(guildId: Long) : Boolean {
        return musicManagers.containsKey(guildId)
    }

    fun init() {
        return
    }
}