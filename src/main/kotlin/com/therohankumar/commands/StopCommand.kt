package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class StopCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "stop"

    override fun build(): SlashCommandData = Commands.slash(name, "Stop playback and clear the queue")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val guild = event.guild!!
        manager.scheduler.queue.clear()
        manager.player.stopTrack()
        guild.audioManager.closeAudioConnection()
        event.replyEmbeds(CommandUtil.successEmbed("Stopped", "Queue cleared and disconnected.")).queue()
    }
}
