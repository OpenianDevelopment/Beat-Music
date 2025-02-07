package com.therohankumar.interfaces

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {
    val name: String

    suspend fun execute(event: SlashCommandInteractionEvent)
    fun createSlashCommand(): SlashCommandData
}