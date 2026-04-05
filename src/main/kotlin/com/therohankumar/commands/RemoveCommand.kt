package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class RemoveCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "remove"

    override fun build(): SlashCommandData = Commands.slash(name, "Remove a track from the queue by position")
        .addOption(OptionType.INTEGER, "position", "Queue position to remove (1-based)", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        if (manager.scheduler.queue.isEmpty()) {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is empty.")).setEphemeral(true).queue()
            return
        }

        val pos = event.getOption("position")!!.asInt - 1
        val queue = manager.scheduler.queue
        if (pos < 0 || pos >= queue.size) {
            event.replyEmbeds(CommandUtil.errorEmbed("Invalid position. Queue has ${queue.size} track${if (queue.size != 1) "s" else ""}."))
                .setEphemeral(true).queue()
            return
        }

        val list = queue.toMutableList()
        val removed = list.removeAt(pos)
        queue.clear()
        queue.addAll(list)

        event.replyEmbeds(CommandUtil.embed("Removed", "Removed **${removed.info.title}** from the queue.")).queue()
    }
}
