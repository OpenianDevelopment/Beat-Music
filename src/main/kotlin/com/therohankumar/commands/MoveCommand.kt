package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class MoveCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "move"

    override fun build(): SlashCommandData = Commands.slash(name, "Move a track to a different position in the queue")
        .addOption(OptionType.INTEGER, "from", "Current position (1-based)", true)
        .addOption(OptionType.INTEGER, "to", "Target position (1-based)", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        if (manager.scheduler.queue.isEmpty()) {
            event.replyEmbeds(CommandUtil.errorEmbed("The queue is empty.")).setEphemeral(true).queue()
            return
        }

        val from = event.getOption("from")!!.asInt - 1
        val to = event.getOption("to")!!.asInt - 1
        val queue = manager.scheduler.queue
        val size = queue.size

        if (from < 0 || from >= size || to < 0 || to >= size) {
            event.replyEmbeds(CommandUtil.errorEmbed("Positions must be between 1 and $size.")).setEphemeral(true).queue()
            return
        }
        if (from == to) {
            event.replyEmbeds(CommandUtil.errorEmbed("From and to positions are the same.")).setEphemeral(true).queue()
            return
        }

        val list = queue.toMutableList()
        val track = list.removeAt(from)
        list.add(to, track)
        queue.clear()
        queue.addAll(list)

        event.replyEmbeds(CommandUtil.embed("Moved", "Moved **${track.info.title}** to position ${to + 1}.")).queue()
    }
}
