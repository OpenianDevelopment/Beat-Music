package com.therohankumar.config

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import dev.lavalink.youtube.YoutubeAudioSourceManager
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail
import dev.lavalink.youtube.clients.MusicWithThumbnail
import dev.lavalink.youtube.clients.WebWithThumbnail
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LavaPlayerConfig {

    private val log = LoggerFactory.getLogger(LavaPlayerConfig::class.java)

    @Value("\${youtube.oauth2.refresh-token:}")
    private lateinit var ytRefreshToken: String

    @Bean
    fun audioPlayerManager(): AudioPlayerManager {
        val manager = DefaultAudioPlayerManager()

        val ytSource = YoutubeAudioSourceManager(
            /* allowSearch = */ true,
            MusicWithThumbnail(),
            AndroidVrWithThumbnail(),
            WebWithThumbnail()
        )

        if (ytRefreshToken.isNotBlank()) {
            // skipInitialization=false → performs account linking on startup
            ytSource.useOauth2(ytRefreshToken, false)
            log.info("YouTube OAuth2 configured")
        } else {
            log.warn("YouTube OAuth2 not configured — anonymous access only; may hit rate limits")
        }

        manager.registerSourceManager(ytSource)

        // Register all other remote sources, explicitly excluding the deprecated built-in YT source
        AudioSourceManagers.registerRemoteSources(
            manager,
            com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java
        )
        AudioSourceManagers.registerLocalSource(manager)

        return manager
    }
}
