package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class ShuffleCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "shuffle"

    override fun build(): SlashCommandData = Commands.slash(name, "Shuffle the queue")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        if (manager.scheduler.queue.isEmpty()) {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is empty.")).setEphemeral(true).queue()
            return
        }
        manager.scheduler.shuffle()
        event.replyEmbeds(CommandUtil.embed("Shuffled", "🔀 Queue has been shuffled.")).queue()
    }
}
