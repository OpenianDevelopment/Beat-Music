package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class PauseCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "pause"

    override fun build(): SlashCommandData = Commands.slash(name, "Pause the current track")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        manager.player.isPaused = true
        event.replyEmbeds(CommandUtil.successEmbed("Paused", "Use `/resume` to continue.")).queue()
    }
}
