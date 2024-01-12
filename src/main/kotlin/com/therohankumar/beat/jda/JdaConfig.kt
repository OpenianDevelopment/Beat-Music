package com.therohankumar.beat.jda

import com.therohankumar.beat.config.props
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.concurrent.thread

@Configuration
class JdaConfig {
    @Bean
    fun shardManager(props: props, eventHandler: EventHandler): ShardManager {
        if (props.token.isBlank()) throw RuntimeException("Discord Token not configured")
        val activity: Activity =
            if (props.artist.isBlank()) Activity.listening("The Score") else Activity.listening(props.artist)
        val intents: List<GatewayIntent> = listOf(GatewayIntent.GUILD_VOICE_STATES)
        val builder = DefaultShardManagerBuilder.create(props.token, intents)
            .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOJI, CacheFlag.CLIENT_STATUS)
            .setEnableShutdownHook(false)
            .setAutoReconnect(true)
            .setShardsTotal(props.shards)
            .addEventListeners(eventHandler)
            .setActivity(activity)
        val shardManager: ShardManager = builder.build()

        Runtime.getRuntime().addShutdownHook(thread(start = false) {
            shardManager.guildCache.forEach {
                if (it.audioManager.isConnected) it.audioManager.closeAudioConnection()
            }
        })
        return shardManager
    }
}