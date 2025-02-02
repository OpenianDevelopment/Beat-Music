package com.therohankumar.commands

import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.FilterSettings
import com.therohankumar.modules.Utilities
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import java.util.concurrent.TimeUnit

class Filter : ICommand {
    override val name = "filter"

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        if(!Utilities.commandCheck(event)) return
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        val currentFilters = musicManager.audioFilter.getFilterStates()

        val menuBuilder = StringSelectMenu.create("filter_select")
            .setMinValues(0)
            .setMaxValues(7) // Updated total number of filters
            .setPlaceholder("Select filters to apply")
            .addOption("Nightcore", "nightcore", "Toggle Nightcore filter", Emoji.fromUnicode("🎵"))
            .addOption("8D", "eightd", "Toggle 8D filter", Emoji.fromUnicode("🔊"))
            .addOption("Vibrato", "vibrato", "Toggle Vibrato filter", Emoji.fromUnicode("〰️"))
            .addOption("Tremolo", "tremolo", "Toggle Tremolo filter", Emoji.fromUnicode("📊"))
            .addOption("Bass Boost", "bassboost", "Toggle Bass Boost filter", Emoji.fromUnicode("📊"))
            .addOption("Echo", "echo", "Toggle Echo filter", Emoji.fromUnicode("🔁"))
            .addOption("Reverb", "reverb", "Toggle Cathedral-like Reverb filter", Emoji.fromUnicode("🏛️"))

        // Set default values based on current filters
        val defaultValues = mutableListOf<String>().apply {
            if (currentFilters.isNightcore) add("nightcore")
            if (currentFilters.isEightD) add("eightd")
            if (currentFilters.isVibrato) add("vibrato")
            if (currentFilters.isTremolo) add("tremolo")
            if (currentFilters.isBassBoost) add("bassboost")
            if (currentFilters.isEcho) add("echo")
            if (currentFilters.isReverb) add("reverb")
        }

        val selectMenu = menuBuilder.setDefaultValues(defaultValues).build()

        val resetButton = Button.danger("reset_filters", "Reset All Filters")

        val embed = EmbedBuilder()
            .setTitle("Audio Filters")
            .setDescription("Select the filters you want to apply to the current track")
            .addField("Active Filters", getActiveFiltersText(currentFilters), false)
            .setFooter("You can select multiple filters at once")
            .setColor(EmbedUtils.YELLOW_COLOR)
            .build()

        event.hook.sendMessageEmbeds(embed)
            .addActionRow(selectMenu)
            .addActionRow(resetButton)
            .queue{ message ->
                val expireEmbed = EmbedUtils.createErrorEmbed("Expired", "This message has expired")
                message.editMessageEmbeds(expireEmbed).setComponents().queueAfter(5, TimeUnit.MINUTES)
            }
    }

    private fun getActiveFiltersText(filters: FilterSettings): String {
        val activeFilters = mutableListOf<String>()
        with(filters) {
            if (isNightcore) activeFilters.add("Nightcore")
            if (isEightD) activeFilters.add("8D")
            if (isVibrato) activeFilters.add("Vibrato")
            if (isTremolo) activeFilters.add("Tremolo")
            if (isBassBoost) activeFilters.add("Bass Boost")
            if (isEcho) activeFilters.add("Echo")
            if (isReverb) activeFilters.add("Reverb")
        }
        return if (activeFilters.isEmpty()) "No filters active" else activeFilters.joinToString(", ")
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash(name, "Manage audio filters")
    }
}