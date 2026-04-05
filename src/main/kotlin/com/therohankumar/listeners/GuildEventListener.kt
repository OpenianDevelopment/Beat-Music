package com.therohankumar.listeners

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.entity.Guild
import com.therohankumar.repository.GuildRepository
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class GuildEventListener(
    private val guildRepository: GuildRepository,
    private val musicManagerRegistry: MusicManagerRegistry
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(GuildEventListener::class.java)

    override fun onReady(event: ReadyEvent) {
        // Upsert all guilds the bot is already in — covers guilds joined before this startup
        val knownIds = guildRepository.findAllById(event.jda.guilds.map { it.idLong }).map { it.id }.toSet()
        event.jda.guilds.forEach { g ->
            if (g.idLong !in knownIds) {
                guildRepository.save(Guild(id = g.idLong, name = g.name))
                log.info("Synced pre-existing guild: {} ({})", g.name, g.idLong)
            }
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        val existing = guildRepository.findById(guild.idLong)
        if (existing.isPresent) {
            existing.get().apply {
                isActive = true
                leftAt = null
            }.also { guildRepository.save(it) }
        } else {
            guildRepository.save(Guild(id = guild.idLong, name = guild.name))
        }
        log.info("Joined guild: {} ({})", guild.name, guild.idLong)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        guildRepository.findById(event.guild.idLong).ifPresent { g ->
            g.isActive = false
            g.leftAt = OffsetDateTime.now()
            guildRepository.save(g)
        }
        musicManagerRegistry.remove(event.guild.idLong)
        log.info("Left guild: {} ({})", event.guild.name, event.guild.idLong)
    }
}
