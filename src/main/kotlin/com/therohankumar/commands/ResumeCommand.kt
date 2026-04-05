package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class ResumeCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "resume"

    override fun build(): SlashCommandData = Commands.slash(name, "Resume the paused track")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        if (!manager.player.isPaused) {
            event.replyEmbeds(CommandUtil.errorEmbed("The player is not paused.")).setEphemeral(true).queue()
            return
        }
        manager.player.isPaused = false
        event.replyEmbeds(CommandUtil.successEmbed("Resumed")).queue()
    }
}
