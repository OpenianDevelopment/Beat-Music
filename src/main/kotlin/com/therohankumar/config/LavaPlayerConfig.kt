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
import dev.lavalink.youtube.clients.TvHtml5SimplyWithThumbnail
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
            TvHtml5SimplyWithThumbnail(), // OAuth-compatible
            MusicWithThumbnail(),
            AndroidVrWithThumbnail(),
            WebWithThumbnail()
        )

        // IPv6 rotation must be configured BEFORE useOauth2 — rotation reconfigures the
        // HTTP client, which would shut down the connection pool that OAuth2 polling already started.
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

        if (ytRefreshToken.isNotBlank()) {
            ytSource.useOauth2(ytRefreshToken, false)
            log.info("YouTube OAuth2 configured")
        } else {
            // Enable OAuth2 handler, then explicitly trigger the device code flow on a
            // virtual thread so Spring startup isn't blocked.
            ytSource.useOauth2("", true) // skipInitialization=true, we trigger it manually
            Thread.ofVirtual().start {
                try {
                    log.warn("=== YouTube OAuth2 Setup ===")
                    val deviceCode    = ytSource.oauth2Handler.fetchDeviceCode()
                    val url           = deviceCode.get("verification_url").text()
                    val code          = deviceCode.get("user_code").text()
                    val intervalSecs  = deviceCode.get("interval").`as`(Long::class.java) ?: 5L
                    val expiresSecs   = deviceCode.get("expires_in").`as`(Long::class.java) ?: 1800L
                    val deviceCodeStr = deviceCode.get("device_code").text()
                    log.warn("Visit: {}", url)
                    log.warn("Enter code: {}", code)
                    log.warn("You have {} seconds to authorize.", expiresSecs)

                    // Poll until the user authorizes or the code expires
                    val deadline = System.currentTimeMillis() + expiresSecs * 1000
                    var token: String? = null
                    while (System.currentTimeMillis() < deadline && token == null) {
                        Thread.sleep(intervalSecs * 1000)
                        val result = ytSource.oauth2Handler.fetchRefreshToken(deviceCodeStr)
                        val error  = result.get("error").text()
                        when {
                            error == "authorization_pending" -> { /* keep polling */ }
                            error == "slow_down"             -> Thread.sleep(5000)
                            error != null                    -> { log.error("OAuth2 error: {}", error); break }
                            else                             -> token = result.get("refresh_token").text()
                        }
                    }

                    if (token != null) {
                        log.warn("=== Authorization successful! ===")
                        log.warn("Add this to your .env: YT_REFRESH_TOKEN={}", token)
                    } else {
                        log.error("OAuth2 flow timed out or failed. Restart the bot to try again.")
                    }
                } catch (e: Exception) {
                    log.error("OAuth2 device flow failed: {}", e.message, e)
                }
            }
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
