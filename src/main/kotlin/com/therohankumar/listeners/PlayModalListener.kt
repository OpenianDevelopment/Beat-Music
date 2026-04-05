package com.therohankumar.listeners

import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.audio.SearchResultStore
import com.therohankumar.commands.PlayCommand
import com.therohankumar.commands.util.CommandUtil
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class PlayModalListener(
    private val registry: MusicManagerRegistry,
    private val searchResultStore: SearchResultStore,
    private val playCommand: PlayCommand
) : ListenerAdapter() {

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (!event.modalId.startsWith("play:")) return

        val parts = event.modalId.split(":")
        if (parts.size < 3) return
        val guildId = parts[1].toLongOrNull() ?: return
        val userId  = parts[2].toLongOrNull() ?: return

        val tracks = searchResultStore.take(userId) ?: run {
            event.replyEmbeds(CommandUtil.errorEmbed("Search results expired. Please run `/play` again."))
                .setEphemeral(true).queue()
            return
        }

        val selectedIndex = event.getValue("track_select")?.getAsString()?.toIntOrNull() ?: 0
        val track = tracks.getOrNull(selectedIndex) ?: tracks[0]

        val guild   = event.guild ?: return
        val manager = registry.get(guildId) ?: run {
            event.replyEmbeds(CommandUtil.errorEmbed("No active session. Please run `/play` again."))
                .setEphemeral(true).queue()
            return
        }

        // Ensure bot is still connected; reconnect if needed
        val botChannel = guild.audioManager.connectedChannel
        val userChannel = event.member?.voiceState?.channel
        if (botChannel == null && userChannel != null) {
            guild.audioManager.openAudioConnection(userChannel)
        }

        manager.scheduler.queue(track)
        event.replyEmbeds(playCommand.buildNowPlayingEmbed(track, manager)).queue()
    }
}
