package com.therohankumar.commands

import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class NowPlaying: ICommand {
    override val name = "now-playing"
    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        if (musicManager.player.playingTrack == null) {
            val embed = EmbedUtils.createErrorEmbed("Error", "Nothing is being played right now!!")
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        val track = musicManager.player.playingTrack
        val embed = EmbedUtils.createNowPlayingEmbed(track.info.title, track.info.uri, track.info.author, track.duration, track.info.artworkUrl, track.userData as User)
        event.hook.sendMessageEmbeds(embed).queue()
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash("now-playing", "Check what is currently playing")
    }
}