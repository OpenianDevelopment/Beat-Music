package com.therohankumar

import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class AudioFilterSelectListeners: ListenerAdapter() {
    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if(event.componentId != "filter_select") return
        val guildId = event.guild?.idLong ?: return
        if(!AudioPlayerManager.musicManagerExist(guildId)) return
        val musicManager = AudioPlayerManager.getMusicManager(guildId)
        val selectedValues = event.values
        musicManager.audioFilter.updateFilter {
            isNightcore = selectedValues.contains("nightcore")
            isEcho = selectedValues.contains("echo")
            isEightD = selectedValues.contains("eightd")
            isVibrato = selectedValues.contains("vibrato")
            isTremolo = selectedValues.contains("tremolo")
            isBassBoost = selectedValues.contains("bassboost")
            isReverb = selectedValues.contains("reverb")
        }

        val activeFilters = getActiveFiltersText(selectedValues)

        val updatedEmbed = EmbedUtils.createAudioFilterEmbed(activeFilters, true, "These Filters has been enbaled", event.user)
        event.editMessageEmbeds(updatedEmbed).setComponents().queue()
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if(event.componentId != "reset_filters") return
        val guildId = event.guild?.idLong ?: return
        if(!AudioPlayerManager.musicManagerExist(guildId)) return
        val musicManager = AudioPlayerManager.getMusicManager(guildId)
        musicManager.audioFilter.resetFilters()
        val updatedEmbed = EmbedBuilder()
            .setTitle("Audio Filters")
            .setDescription("Audio Filters has been reset")
            .setColor(EmbedUtils.YELLOW_COLOR)
            .build()
        event.editMessageEmbeds(updatedEmbed).setComponents().queue()
    }

    private fun getActiveFiltersText(selectedValues: List<String>): String {
        val filterNameMap = mapOf(
            "nightcore" to "Nightcore",
            "eightd" to "8D",
            "vibrato" to "Vibrato",
            "tremolo" to "Tremolo",
            "bassboost" to "Bass Boost",
            "echo" to "Echo",
            "reverb" to "Reverb"
        )

        val activeFilters = selectedValues.mapNotNull { filterNameMap[it] }
        return if (activeFilters.isEmpty()) "No filters active"
        else activeFilters.joinToString(", ")
    }
}