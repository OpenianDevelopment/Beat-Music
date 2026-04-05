package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class NowPlayingCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "nowplaying"

    override fun build(): SlashCommandData = Commands.slash(name, "Show what's currently playing")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requirePlaying(event, registry) ?: return
        val track   = manager.player.playingTrack!!
        event.replyEmbeds(CommandUtil.nowPlayingEmbed(track, manager, queued = false)).queue()
    }
}
