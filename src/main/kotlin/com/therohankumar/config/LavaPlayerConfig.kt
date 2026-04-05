package com.therohankumar.config

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail
import dev.lavalink.youtube.clients.MusicWithThumbnail
import dev.lavalink.youtube.clients.TvHtml5SimplyWithThumbnail
import dev.lavalink.youtube.clients.Web
import dev.lavalink.youtube.clients.WebEmbedded
import dev.lavalink.youtube.clients.WebEmbeddedWithThumbnail
import dev.lavalink.youtube.clients.WebWithThumbnail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LavaPlayerConfig {

    private val log = LoggerFactory.getLogger(LavaPlayerConfig::class.java)

    @Value("\${youtube.po-token:}")
    private lateinit var ytPoToken: String

    @Value("\${youtube.visitor-data:}")
    private lateinit var ytVisitorData: String

    @Value("\${youtube.ipv6-block:}")
    private lateinit var ipv6Block: String

    @Bean
    fun audioPlayerManager(): AudioPlayerManager {
        val manager = DefaultAudioPlayerManager()

        // PO token applies to WEB and WEBEMBEDDED clients (static, must be set before source is used)
        if (ytPoToken.isNotBlank() && ytVisitorData.isNotBlank()) {
            Web.setPoTokenAndVisitorData(ytPoToken, ytVisitorData)
            WebEmbedded.setPoTokenAndVisitorData(ytPoToken, ytVisitorData)
            log.info("YouTube PO token configured")
        }

        val ytSource = YoutubeAudioSourceManager(
            /* allowSearch = */ true,
            MusicWithThumbnail(),         // YouTube Music search (ytmsearch:)
            WebWithThumbnail(),            // Playback + full metadata (uses PO token if set)
            WebEmbeddedWithThumbnail(),    // Fallback (uses PO token if set)
            TvHtml5SimplyWithThumbnail(), // Fallback: playback + metadata
            AndroidVrWithThumbnail()      // Fallback: playback
        )

        if (ipv6Block.isNotBlank()) {
            val planner = RotatingNanoIpRoutePlanner(listOf(Ipv6Block(ipv6Block)))
            YoutubeIpRotatorSetup(planner)
                .forConfiguration(ytSource.httpInterfaceManager, false)
                .withMainDelegateFilter(ytSource.contextFilter)
                .setup()
            log.info("IPv6 rotation enabled with block {}", ipv6Block)
        }

        manager.registerSourceManager(ytSource)

        AudioSourceManagers.registerRemoteSources(
            manager,
            com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java
        )
        AudioSourceManagers.registerLocalSource(manager)

        return manager
    }
}
