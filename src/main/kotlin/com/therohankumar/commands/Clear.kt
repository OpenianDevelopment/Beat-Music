package com.therohankumar.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.util.concurrent.BlockingQueue

class Clear: ICommand {
    override val name = "clear"

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        val queue: BlockingQueue<AudioTrack> = musicManager.taskScheduler.queue
        if (queue.isEmpty()) {
            val embed = EmbedUtils.createGenericEmbed(
                title = "Queue Empty",
                description = "There are no tracks to clear in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        queue.clear()
        val embed = EmbedUtils.createErrorEmbed(
            title = "Queue Cleared",
            description = "All the tracks in queue were cleared.",
            requestedBy = event.user
        )
        event.hook.sendMessageEmbeds(embed).queue()
        return

    }
    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash(name, "Clear the current music queue")
    }
}
