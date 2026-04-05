package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class SkipCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "skip"

    override fun build(): SlashCommandData = Commands.slash(name, "Skip one or more tracks")
        .addOption(OptionType.INTEGER, "amount", "Number of tracks to skip (default 1)", false)

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val amount = (event.getOption("amount")?.asInt ?: 1).coerceAtLeast(1)

        // Drop extra tracks from queue before advancing
        repeat(amount - 1) { manager.scheduler.queue.poll() }
        manager.scheduler.nextTrack()

        event.replyEmbeds(
            CommandUtil.successEmbed("Skipped", if (amount > 1) "Skipped $amount tracks." else null)
        ).queue()
    }
}
