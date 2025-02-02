package com.therohankumar.commands

import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.util.concurrent.BlockingQueue
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.entities.User

class Queue : ICommand {
    override val name = "queue"
    private val itemsPerPage = 10

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        val queue: BlockingQueue<AudioTrack> = musicManager.trackScheduler.queue

        if (queue.isEmpty()) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Queue Empty",
                description = "There are no tracks in the queue.",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }

        // Convert BlockingQueue to List for safe iteration
        val queueList = queue.toList()
        val pageNumber = event.getOption("page")?.asLong?.toInt() ?: 1
        val totalPages = (queueList.size + itemsPerPage - 1) / itemsPerPage

        if (pageNumber < 1 || pageNumber > totalPages) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Invalid Page",
                description = "Please enter a page number between 1 and $totalPages",
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }

        val startIdx = (pageNumber - 1) * itemsPerPage
        val endIdx = minOf(startIdx + itemsPerPage, queueList.size)
        val currentTrack = musicManager.player.playingTrack

        // Build queue list
        val queueDescription = buildString {
            if (currentTrack != null) {
                append("**Now Playing:**\n")
                append("[${currentTrack.info.title}](${currentTrack.info.uri}) | ")
                append("`${formatDuration(currentTrack.duration)}` | ")
                append("Requested by ${(currentTrack.userData as User).asMention}\n\n")
            }

            append("**Queue:**\n")
            for (i in startIdx until endIdx) {
                val track = queueList[i]
                append("`${i + 1}.` [${track.info.title}](${track.info.uri}) | ")
                append("`${formatDuration(track.duration)}` | ")
                append("Requested by ${(track.userData as User).asMention}\n")
            }

            // Add page navigation help
            if (totalPages > 1) {
                append("\n*Use `/queue <page>` to view different pages*")
            }
        }

        // Calculate total duration
        val totalDuration = queueList.sumOf { it.duration } + (currentTrack?.duration ?: 0)

        val embed = EmbedUtils.createQueueEmbed(
            queueDescription = queueDescription,
            totalTracks = queueList.size,
            totalDuration = totalDuration,
            currentPage = pageNumber,
            totalPages = totalPages,
            requestedBy = event.user
        )

        event.hook.sendMessageEmbeds(embed).queue()
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash(name, "View the current music queue")
            .addOption(OptionType.INTEGER, "page", "Page number to view", false)
    }
}