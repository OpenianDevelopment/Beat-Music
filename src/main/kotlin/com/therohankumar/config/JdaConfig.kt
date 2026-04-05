package com.therohankumar.config

import com.therohankumar.commands.CommandRegistry
import com.therohankumar.listeners.FilterSelectMenuListener
import com.therohankumar.listeners.GuildEventListener
import com.therohankumar.listeners.PlayModalListener
import com.therohankumar.listeners.SlashCommandListener
import moe.kyokobot.libdave.NativeDaveFactory
import moe.kyokobot.libdave.jda.LDJDADaveSessionFactory
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioModuleConfig
import net.dv8tion.jda.api.audio.dave.DaveSessionFactory
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JdaConfig {

    private val log = LoggerFactory.getLogger(JdaConfig::class.java)

    @Value("\${discord.token}")
    private lateinit var token: String

    @Value("\${discord.guild-id:}")
    private lateinit var guildId: String

    @Bean
    fun daveSessionFactory(): DaveSessionFactory = LDJDADaveSessionFactory(NativeDaveFactory())

    @Bean
    fun jda(
        daveSessionFactory: DaveSessionFactory,
        slashCommandListener: SlashCommandListener,
        guildEventListener: GuildEventListener,
        filterSelectMenuListener: FilterSelectMenuListener,
        playModalListener: PlayModalListener,
        commandRegistry: CommandRegistry
    ): JDA {
        val jda = JDABuilder.createDefault(token)
            .setAudioModuleConfig(
                AudioModuleConfig().withDaveSessionFactory(daveSessionFactory)
            )
            .enableIntents(
                GatewayIntent.GUILD_VOICE_STATES,
                GatewayIntent.GUILD_MESSAGES
            )
            .addEventListeners(slashCommandListener, guildEventListener, filterSelectMenuListener, playModalListener)
            .addEventListeners(object : ListenerAdapter() {
                override fun onReady(event: ReadyEvent) {
                    log.info("Beat Bot ready — logged in as {}", event.jda.selfUser.asTag)
                    registerCommands(event.jda, commandRegistry)
                }
            })
            .build()

        return jda
    }

    private fun registerCommands(jda: JDA, registry: CommandRegistry) {
        val commandData = registry.all.map { it.build() }
        commandData.forEach { cmd ->
            val autoCompleteOptions = cmd.options.filter { it.isAutoComplete }
            if (autoCompleteOptions.isNotEmpty()) {
                log.info("Command /{} has autocomplete on: {}", cmd.name, autoCompleteOptions.map { it.name })
            }
        }
        if (guildId.isNotBlank()) {
            val guild = jda.getGuildById(guildId)
            if (guild != null) {
                // Clear any stale global commands so they don't shadow guild commands
                jda.updateCommands().queue {
                    log.info("Cleared global commands")
                }
                guild.updateCommands().addCommands(commandData).queue {
                    log.info("Registered {} slash commands to dev guild {}", it.size, guildId)
                }
            } else {
                log.warn("DISCORD_GUILD_ID set to {} but guild not found; falling back to global", guildId)
                jda.updateCommands().addCommands(commandData).queue {
                    log.info("Registered {} slash commands globally (propagation up to 1h)", it.size)
                }
            }
        } else {
            jda.updateCommands().addCommands(commandData).queue {
                log.info("Registered {} slash commands globally (propagation up to 1h)", it.size)
            }
        }
    }
}
