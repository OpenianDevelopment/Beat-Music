package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class AutoplayCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "autoplay"

    override fun build(): SlashCommandData = Commands.slash(name, "Toggle autoplay (queues recommended tracks when empty)")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        manager.scheduler.autoplay = !manager.scheduler.autoplay
        val status = if (manager.scheduler.autoplay) "enabled" else "disabled"
        event.replyEmbeds(CommandUtil.embed("Autoplay", "Autoplay is now **$status**.")).queue()
    }
}
