package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class ClearCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "clear"

    override fun build(): SlashCommandData = Commands.slash(name, "Clear the queue (keeps the current track playing)")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        if (manager.scheduler.queue.isEmpty()) {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is already empty.")).setEphemeral(true).queue()
            return
        }
        val count = manager.scheduler.queue.size
        manager.scheduler.queue.clear()
        event.replyEmbeds(CommandUtil.embed("Queue Cleared", "Removed $count track${if (count != 1) "s" else ""}.")).queue()
    }
}
