package com.therohankumar.commands

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.audio.filters.FilterConfig
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.selections.SelectOption
import net.dv8tion.jda.api.components.selections.StringSelectMenu
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.springframework.stereotype.Component

@Component
class FilterCommand(private val registry: MusicManagerRegistry) : SlashCommand {

    override val name = "filters"

    override fun build(): SlashCommandData =
        Commands.slash(name, "Enable or disable audio filters — select to enable, deselect to disable")

    override fun execute(event: SlashCommandInteractionEvent) {
        val manager = CommandUtil.requireSameVoice(event, registry) ?: return
        val cfg = manager.filterConfig

        val activeKeys = buildSet {
            if (cfg.echo != null)       add("echo")
            if (cfg.reverb != null)     add("reverb")
            if (cfg.tremolo != null)    add("tremolo")
            if (cfg.vibrato != null)    add("vibrato")
            if (cfg.bassboost != null)  add("bassboost")
            if (cfg.nightcore)          add("nightcore")
            if (cfg.vaporwave)          add("vaporwave")
            if (cfg.karaoke != null)    add("karaoke")
            if (cfg.distortion != null) add("distortion")
            if (cfg.rotation != null)   add("rotation")
        }

        val allFilters = listOf(
            "echo"       to "Echo",
            "reverb"     to "Reverb",
            "tremolo"    to "Tremolo",
            "vibrato"    to "Vibrato",
            "bassboost"  to "Bass Boost",
            "nightcore"  to "Nightcore",
            "vaporwave"  to "Vaporwave",
            "karaoke"    to "Karaoke",
            "distortion" to "Distortion",
            "rotation"   to "Rotation (8D)"
        )

        val menuBuilder = StringSelectMenu.create("filter:manage:${event.guild!!.idLong}")
            .setPlaceholder("Select filters to enable; deselect to disable…")
            .setMinValues(0)
            .setMaxValues(allFilters.size)
        allFilters.forEach { (value, label) ->
            menuBuilder.addOptions(SelectOption.of(label, value).withDefault(value in activeKeys))
        }

        val active = if (activeKeys.isEmpty()) "None" else activeKeys.joinToString(", ") { it.replaceFirstChar(Char::uppercaseChar) }
        event.replyEmbeds(CommandUtil.embed("Audio Filters", "Active: **$active**\nSelect to enable, deselect to disable."))
            .addComponents(ActionRow.of(menuBuilder.build()))
            .setEphemeral(true)
            .queue()
    }
}
