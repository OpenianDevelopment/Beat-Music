package com.therohankumar.commands

import com.therohankumar.audio.LoopMode
import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.Command as JDACommand
import org.springframework.stereotype.Component

@Component
class LoopCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "loop"

    override fun build(): SlashCommandData = Commands.slash(name, "Set the loop mode")
        .addOptions(
            OptionData(OptionType.STRING, "mode", "Loop mode", true)
                .addChoices(
                    JDACommand.Choice("Off", "off"),
                    JDACommand.Choice("Track", "track"),
                    JDACommand.Choice("Queue", "queue")
                )
        )

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val mode = when (event.getOption("mode")!!.asString) {
            "track" -> LoopMode.TRACK
            "queue" -> LoopMode.QUEUE
            else -> LoopMode.OFF
        }
        manager.scheduler.loopMode = mode
        val label = when (mode) {
            LoopMode.OFF -> "🔁 Loop disabled"
            LoopMode.TRACK -> "🔂 Looping current track"
            LoopMode.QUEUE -> "🔁 Looping entire queue"
        }
        event.replyEmbeds(CommandUtil.successEmbed("Loop", label)).queue()
    }
}
