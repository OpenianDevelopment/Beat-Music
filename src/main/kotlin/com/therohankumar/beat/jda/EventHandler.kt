package com.therohankumar.beat.jda

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventHandler(private val commandManager: CommandManager) : ListenerAdapter() {
    private val log: Logger = LoggerFactory.getLogger(EventHandler::class.java)
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        commandManager.onSlashCommand(event)
    }
}