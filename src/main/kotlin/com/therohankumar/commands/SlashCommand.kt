package com.therohankumar.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface SlashCommand {
    val name: String
    fun build(): SlashCommandData
    fun execute(event: SlashCommandInteractionEvent)
    fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
}
