package com.therohankumar.commands

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.util.concurrent.BlockingQueue

class Shift: ICommand {
    override val name = "shift"

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)

        val queue: BlockingQueue<AudioTrack> = musicManager.trackScheduler.queue
        if (queue.isEmpty()) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Queue Empty",
                description = "There are no tracks to Shift in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        else if (queue.size == 1){
            val embed = EmbedUtils.createErrorEmbed(
                title = "Not Enough Tracks",
                description = "There are not enough tracks to shift in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }

        val trackNumber = event.getOption("track")!!.asInt.minus(-1)
        val newPosition = event.getOption("position")!!.asInt.minus(-1)

        if(trackNumber == newPosition) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Same Position",
                description = "New Position of track should not same as current one",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        if(trackNumber >= queue.size || newPosition >= queue.size) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Invalid Input",
                description = "Current Track Position and New Track Position should be between 1 and ${queue.size}",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }

        musicManager.trackScheduler.shiftTrack(trackNumber, newPosition)
        val embed = EmbedUtils.createGenericEmbed(
            title = "Track Shifted",
            description = "Moved track ${trackNumber + 1} to position ${newPosition + 1}",
            requestedBy = event.user
        )

        event.hook.sendMessageEmbeds(embed).queue()
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash(name, "Shift tracks higher or lower in queue")
            .addOption(OptionType.INTEGER,"track","Current track number in queue",true)
            .addOption(OptionType.INTEGER, "position", "Which position to push the track to",true)
    }
}