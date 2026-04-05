package com.therohankumar.listeners

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.audio.filters.*
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class FilterSelectMenuListener(private val registry: MusicManagerRegistry) : ListenerAdapter() {

    override fun onStringSelectInteraction(event: StringSelectInteractionEvent) {
        if (!event.componentId.startsWith("filter:manage:")) return

        val guildId = event.componentId.removePrefix("filter:manage:").toLongOrNull() ?: return
        val manager = registry.get(guildId) ?: run {
            event.deferEdit().queue()
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("No active session."))
                .setComponents(emptyList()).queue()
            return
        }

        val selected = event.values.toSet()
        val prev = manager.filterConfig

        // For each filter: if selected → keep existing params or use defaults; if not → null/false
        manager.filterConfig = FilterConfig(
            echo        = if ("echo"        in selected) prev.echo        ?: EchoParams()       else null,
            reverb      = if ("reverb"      in selected) prev.reverb      ?: ReverbParams()     else null,
            tremolo     = if ("tremolo"     in selected) prev.tremolo     ?: TremoloParams()    else null,
            vibrato     = if ("vibrato"     in selected) prev.vibrato     ?: VibratoParams()    else null,
            bassboost   = if ("bassboost"   in selected) prev.bassboost   ?: 3.0f               else null,
            nightcore   = "nightcore"  in selected,
            vaporwave   = "vaporwave"  in selected,
            karaoke     = if ("karaoke"     in selected) prev.karaoke     ?: KaraokeParams()    else null,
            distortion  = if ("distortion"  in selected) prev.distortion  ?: DistortionParams() else null,
            rotation    = if ("rotation"    in selected) prev.rotation    ?: RotationParams()   else null
        )
        manager.applyFilters()

        val enabled  = selected - prev.activeKeys()
        val disabled = prev.activeKeys() - selected

        val summary = buildString {
            if (enabled.isNotEmpty())  append("Enabled: **${enabled.joinToString()}**")
            if (enabled.isNotEmpty() && disabled.isNotEmpty()) append("\n")
            if (disabled.isNotEmpty()) append("Disabled: **${disabled.joinToString()}**")
            if (isEmpty()) append("No changes.")
        }

        event.deferEdit().queue()
        event.hook.editOriginalEmbeds(CommandUtil.embed("Filters Updated", summary))
            .setComponents(emptyList())
            .queue()
    }

    private fun FilterConfig.activeKeys(): Set<String> = buildSet {
        if (echo != null)       add("echo")
        if (reverb != null)     add("reverb")
        if (tremolo != null)    add("tremolo")
        if (vibrato != null)    add("vibrato")
        if (bassboost != null)  add("bassboost")
        if (nightcore)          add("nightcore")
        if (vaporwave)          add("vaporwave")
        if (karaoke != null)    add("karaoke")
        if (distortion != null) add("distortion")
        if (rotation != null)   add("rotation")
    }
}
