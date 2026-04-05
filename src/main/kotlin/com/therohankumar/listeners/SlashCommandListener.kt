package com.therohankumar.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import com.therohankumar.commands.CommandRegistry
import com.therohankumar.entity.CommandLog
import com.therohankumar.repository.CommandLogRepository
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlashCommandListener(
    private val registry: CommandRegistry,
    private val commandLogRepository: CommandLogRepository,
    private val objectMapper: ObjectMapper
) : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(SlashCommandListener::class.java)

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        log.info("[Autocomplete] Received event — command='{}' focused='{}' value='{}'",
            event.name, event.focusedOption.name, event.focusedOption.value)
        registry[event.name]?.onAutoComplete(event)
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = registry[event.name] ?: return
        var success = true
        var errorMsg: String? = null

        try {
            command.execute(event)
        } catch (e: Exception) {
            success = false
            errorMsg = e.message
            log.error("Command /${event.name} failed for user ${event.user.idLong}", e)
            if (!event.isAcknowledged) {
                event.reply("An unexpected error occurred.").setEphemeral(true).queue()
            }
        } finally {
            persistLog(event, success, errorMsg)
        }
    }

    private fun persistLog(event: SlashCommandInteractionEvent, success: Boolean, errorMsg: String?) {
        Thread.ofVirtual().start {
            try {
                val args = event.options.associate { it.name to it.asString }
                commandLogRepository.save(CommandLog(
                    guildId = event.guild?.idLong,
                    userId = event.user.idLong,
                    command = event.name,
                    args = objectMapper.writeValueAsString(args),
                    success = success,
                    errorMsg = errorMsg
                ))
            } catch (e: Exception) {
                log.warn("Failed to persist command log for /${event.name}", e)
            }
        }
    }
}
