package com.therohankumar.beat.jda

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommandManager(
    private val contextBeans: CommandContext.Beans,
    commands: Collection<Command>
) {
    private final val registry: Map<String, Command>
    private val log: Logger = LoggerFactory.getLogger(CommandManager::class.java)

    init {
        val map = mutableMapOf<String, Command>()
        commands.forEach { c ->
            map[c.name] = c
        }
        registry = map
        log.info("Registered ${commands.size} commands with ${registry.size} names")
        contextBeans.commandManager = this
    }

    operator fun get(commandName: String) = registry[commandName]


    fun onSlashCommand(event: SlashCommandInteractionEvent) {
        GlobalScope.launch {
            val command = registry[event.fullCommandName] ?: return@launch
            val ctx = CommandContext(contextBeans, event)
            log.info("Invocation: ${event.fullCommandName}")
            command.invoke0(ctx)
        }
    }
}