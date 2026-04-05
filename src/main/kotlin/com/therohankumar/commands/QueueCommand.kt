package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class QueueCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "queue"

    override fun build(): SlashCommandData = Commands.slash(name, "Show the current queue")
        .addOption(OptionType.INTEGER, "page", "Page number (default 1)", false)

    override fun execute(event: SlashCommandInteractionEvent) {
        val guild   = event.guild ?: return
        val manager = registry.get(guild.idLong) ?: run {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is empty.")).setEphemeral(true).queue()
            return
        }

        val playing = manager.player.playingTrack
        val queue   = manager.scheduler.queue.toList()

        if (playing == null && queue.isEmpty()) {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is empty.")).setEphemeral(true).queue()
            return
        }

        val pageSize   = 10
        val page       = (event.getOption("page")?.asInt ?: 1).coerceAtLeast(1)
        val totalPages = ((queue.size - 1) / pageSize + 1).coerceAtLeast(1)
        val actualPage = page.coerceAtMost(totalPages)
        val start      = (actualPage - 1) * pageSize
        val end        = (start + pageSize).coerceAtMost(queue.size)

        val totalDuration = queue.sumOf { it.duration }

        val embed = EmbedBuilder()
            .setColor(CommandUtil.EMBED_COLOR)
            .setTitle("Queue  ·  Page $actualPage / $totalPages")

        val sb = StringBuilder()

        playing?.let { t ->
            sb.append("**▶  Now Playing**\n")
            sb.append("[${t.info.title}](${t.info.uri})  `${CommandUtil.formatDuration(t.duration)}`\n")
            sb.append(CommandUtil.progressBar(t.position, t.duration))
            sb.append("  `${CommandUtil.formatDuration(t.position)} / ${CommandUtil.formatDuration(t.duration)}`\n")
        }

        if (queue.isNotEmpty()) {
            sb.append("\n**Up Next**\n")
            queue.subList(start, end).forEachIndexed { i, track ->
                val num  = start + i + 1
                val dur  = if (track.info.isStream) "🔴 Live" else CommandUtil.formatDuration(track.duration)
                sb.append("`$num.` [${track.info.title}](${track.info.uri})  `$dur`\n")
            }
        }

        embed.setDescription(sb.toString().trimEnd())

        val footerParts = mutableListOf<String>()
        footerParts += "${queue.size} track${if (queue.size != 1) "s" else ""} queued"
        if (totalDuration > 0) footerParts += CommandUtil.formatDuration(totalDuration) + " total"

        val loopIcon = when (manager.scheduler.loopMode) {
            com.therohankumar.audio.LoopMode.TRACK -> "🔂 Loop: track"
            com.therohankumar.audio.LoopMode.QUEUE -> "🔁 Loop: queue"
            else -> null
        }
        if (loopIcon != null) footerParts += loopIcon
        if (manager.scheduler.autoplay) footerParts += "✨ Autoplay"

        embed.setFooter(footerParts.joinToString("  ·  "))

        event.replyEmbeds(embed.build()).queue()
    }
}
