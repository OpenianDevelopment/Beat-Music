package com.therohankumar.commands

import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Skip: ICommand {
    override val name = "skip"
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        val track = musicManager.player.playingTrack
        if(track == null) {
            val embed = EmbedUtils.createErrorEmbed("Not Playing Anything", "You need to start playing song first")
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        val embed = EmbedUtils.createSkipEmbed(track.info.title, null, event.user)
        event.hook.sendMessageEmbeds(embed).queue()
        musicManager.trackScheduler.nextTrack()
    }
    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash("skip", "Skip the current playing song")
    }
}