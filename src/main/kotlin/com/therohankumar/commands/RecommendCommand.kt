package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import com.therohankumar.service.RecommendationService
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class RecommendCommand(
    private val registry: MusicManagerRegistry,
    private val recommendationService: RecommendationService
) : SlashCommand {

    override val name = "recommend"

    override fun build(): SlashCommandData =
        Commands.slash(name, "Show top 5 track recommendations based on this server's history")

    override fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild ?: return
        event.deferReply().queue()

        val tracks = recommendationService.recommend(guild.idLong)

        if (tracks.isEmpty()) {
            event.hook.editOriginalEmbeds(
                CommandUtil.embed("Recommendations", "Not enough play history yet. Play more songs to unlock autoplay recommendations!")
            ).queue()
            return
        }

        val embed = EmbedBuilder()
            .setColor(CommandUtil.EMBED_COLOR)
            .setTitle("Recommended Tracks")
            .setDescription("Based on this server's listening history:")

        tracks.forEachIndexed { i, track ->
            val duration = track.durationMs?.let { CommandUtil.formatDuration(it.toLong()) } ?: "?"
            embed.addField(
                "${i + 1}. ${track.title ?: "Unknown"}",
                "${track.author ?: "Unknown"} · $duration",
                false
            )
        }

        event.hook.editOriginalEmbeds(embed.build()).queue()
    }
}
