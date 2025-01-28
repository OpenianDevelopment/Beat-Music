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
        val taskScheduler = musicManager.taskScheduler
        val queue: BlockingQueue<AudioTrack> = musicManager.taskScheduler.queue
        if (queue.isEmpty()) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Queue Empty",
                description = "There are no tracks to Shift in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        if (queue.count()<=1){
            val embed = EmbedUtils.createErrorEmbed(
                title = "Not Enough Tracks",
                description = "There are not enough tracks to shift in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        val songpos = event.interaction.getOption("track")!!.asInt -1
        val newsongpos = event.interaction.getOption("new_pos")!!.asInt-1
        when{
            songpos == newsongpos -> {
                val embed = EmbedUtils.createErrorEmbed(
                    title = "Invaild Input",
                    description = "new songpos cannot be equal to newsongpos",
                    requestedBy = event.user
                )
                event.hook.sendMessageEmbeds(embed).queue()
                return
            }
            songpos < 0 -> {
                val embed = EmbedUtils.createErrorEmbed(
                    title = "Invaild Input",
                    description = "songpos cannot be lower than 1.",
                    requestedBy = event.user
                )
                event.hook.sendMessageEmbeds(embed).queue()
                return
            }
            newsongpos <= 0 -> {
                val embed = EmbedUtils.createErrorEmbed(
                    title = "Invaild Input",
                    description = "newsongpos cannot be lower than 1.",
                    requestedBy = event.user
                )
                event.hook.sendMessageEmbeds(embed).queue()
                return
            }
            songpos >= queue.count() ->{
                val embed = EmbedUtils.createErrorEmbed(
                    title = "Invaild Input",
                    description = "songpos cannot be greater than the songs in queue.",
                    requestedBy = event.user
                )
                event.hook.sendMessageEmbeds(embed).queue()
                return
            }
        }
        taskScheduler.shiftTrack(songpos,newsongpos)
        val embed = EmbedUtils.createGenericEmbed(
            title = "Track Shifted",
            description = "Track was shifted.",
            requestedBy = event.user
        )
        event.hook.sendMessageEmbeds(embed).queue()
        return
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash(name, "Shifts tracks in queue.")
            .addOption(OptionType.INTEGER,"track","Current Track Position",true)
            .addOption(OptionType.INTEGER, "new_pos", "New Track position Number",true)
    }
}