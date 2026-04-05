package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class VolumeCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "volume"

    override fun build(): SlashCommandData = Commands.slash(name, "Set the playback volume (1–200)")
        .addOption(OptionType.INTEGER, "level", "Volume level from 1 to 200", true)

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val level = event.getOption("level")!!.asInt

        if (level !in 1..200) {
            event.replyEmbeds(CommandUtil.errorEmbed("Volume must be between 1 and 200.")).setEphemeral(true).queue()
            return
        }

        manager.player.volume = level
        val filled = (level / 10).coerceIn(0, 20)
        val icon = when {
            level == 0   -> "🔇"
            level <= 50  -> "🔈"
            level <= 100 -> "🔉"
            else         -> "🔊"
        }
        val bar = "▰".repeat(filled) + "▱".repeat(20 - filled)
        event.replyEmbeds(CommandUtil.successEmbed("Volume", "$icon  $bar  **$level%**")).queue()
    }
}
