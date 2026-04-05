package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class SeekCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "seek"

    override fun build(): SlashCommandData = Commands.slash(name, "Seek to a position in the current track")
        .addOption(OptionType.STRING, "position", "Timestamp, e.g. 1:30 or 90 (seconds)", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val track = manager.player.playingTrack!!
        val input = event.getOption("position")!!.asString

        val posMs = CommandUtil.parseTimestamp(input) ?: run {
            event.replyEmbeds(CommandUtil.errorEmbed("Invalid timestamp. Use formats like `1:30` or `90`."))
                .setEphemeral(true).queue()
            return
        }

        if (!track.isSeekable) {
            event.replyEmbeds(CommandUtil.errorEmbed("This track is not seekable.")).setEphemeral(true).queue()
            return
        }

        if (posMs > track.duration) {
            event.replyEmbeds(CommandUtil.errorEmbed("Position exceeds track duration (${CommandUtil.formatDuration(track.duration)})."))
                .setEphemeral(true).queue()
            return
        }

        track.position = posMs
        event.replyEmbeds(CommandUtil.successEmbed("Seeked", "Jumped to **${CommandUtil.formatDuration(posMs)}**.")).queue()
    }
}
