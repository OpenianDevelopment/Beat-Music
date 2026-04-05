package com.therohankumar.config

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup
import com.sedmelluq.lava.extensions.youtuberotator.planner.RotatingNanoIpRoutePlanner
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
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

    @Value("\${youtube.ipv6-block:}")
    private lateinit var ipv6Block: String

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
            ytSource.useOauth2(ytRefreshToken, false)
            log.info("YouTube OAuth2 configured")
        } else {
            log.warn("YouTube OAuth2 not configured — anonymous access only; may hit rate limits")
        }

        if (ipv6Block.isNotBlank()) {
            val planner = RotatingNanoIpRoutePlanner(listOf(Ipv6Block(ipv6Block)))
            // Use a no-op delegate — the old sedmelluq YoutubeHttpContextFilter (default)
            // crashes with v2 source manager because it expects a tokenTracker that is never set.
            val noop = object : HttpContextFilter {
                override fun onContextOpen(context: HttpClientContext) {}
                override fun onContextClose(context: HttpClientContext) {}
                override fun onRequest(context: HttpClientContext, request: HttpUriRequest, isRetry: Boolean) {}
                override fun onRequestResponse(context: HttpClientContext, request: HttpUriRequest, response: HttpResponse) = false
                override fun onRequestException(context: HttpClientContext, request: HttpUriRequest, error: Throwable) = false
            }
            YoutubeIpRotatorSetup(planner)
                .forConfiguration(ytSource.httpInterfaceManager, false)
                .withMainDelegateFilter(noop)
                .setup()
            log.info("IPv6 rotation enabled with block {}", ipv6Block)
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
