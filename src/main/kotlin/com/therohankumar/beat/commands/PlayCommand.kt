package com.therohankumar.beat.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.beat.jda.Command
import com.therohankumar.beat.jda.CommandContext
import com.therohankumar.beat.music.Player
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component
import java.net.URI


@Component
class PlayCommand(val apm: AudioPlayerManager) : Command("play") {
    override suspend fun CommandContext.invoke() {
        if (!ensureVoiceChannel(event)) return
        var query: String = event.getOption("song").toString()
        if (!checkValidUrl(query)) query = "ytmsearch:$query"
        player.lastChannel = event.channel.asTextChannel()
        apm.loadItem(query, Loader(event, player))
    }

    fun CommandContext.ensureVoiceChannel(event: SlashCommandInteractionEvent): Boolean {
        val ourVc = event.guild?.selfMember?.voiceState?.channel?.asVoiceChannel()
        val theirVc = event.member?.voiceState?.channel?.asVoiceChannel()

        if (ourVc == null && theirVc == null) {
            event.reply("You need to be in a voice channel").queue()
            return false
        }

        if (ourVc != theirVc && theirVc != null) {
            val canTalk = theirVc.canTalk()
            if (!canTalk) {
                event.reply("I need permission to connect and speak in ${theirVc.name}").queue()
                return false
            }

            event.guild!!.audioManager.openAudioConnection(theirVc)
            event.guild!!.audioManager.sendingHandler = player
            return true
        }

        return ourVc != null
    }

    fun checkValidUrl(url: String): Boolean {
        return try {
            URI(url).toURL()
            true
        } catch (e: Exception) {
            false
        }
    }

    inner class Loader(
        private val event: SlashCommandInteractionEvent,
        private val player: Player
    ) : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            val embed = EmbedBuilder()
                .setTitle("Added to Queue")
                .addField(
                    "Track Name",
                    (("[" + track.info.title).toString() + "](" + track.info.uri).toString() + ")",
                    true
                )
                .addField("Added By", event.user.name, true)
                .setColor(16760143)
                .build()
            event.interaction.hook.sendMessageEmbeds(embed)
            track.userData = event.user
            player.add(track)
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            if (playlist.isSearchResult) {
                val track = playlist.tracks.component1()
                val embed = EmbedBuilder()
                    .setTitle("Added to Queue")
                    .addField(
                        "Track Name",
                        (("[" + track.info.title).toString() + "](" + track.info.uri).toString() + ")",
                        true
                    )
                    .addField("Added By", event.user.name, true)
                    .setColor(16760143)
                    .build()
                event.interaction.hook.sendMessageEmbeds(embed).queue()
                track.userData = event.user
                player.add(track)
            } else {
                val tracks = playlist.tracks
                val embed = EmbedBuilder().setDescription(
                    "Enqueuing `"
                            + tracks.size
                            + "` songs from `"
                            + playlist.name
                            + "` Added By `" + event.user.name + "`"
                )
                    .setColor(16760143).build()
                event.interaction.hook.sendMessageEmbeds(embed).queue()
                for (track in tracks) {
                    track.userData = event.user
                    player.add(track)
                }
            }
        }

        override fun noMatches() {
            val embed = EmbedBuilder().setColor(16760143).setTitle("Failed to get the song").build()
            event.interaction.hook.sendMessageEmbeds(embed).queue()
        }

        override fun loadFailed(exception: FriendlyException?) {
            val embed = EmbedBuilder().setColor(16760143)
                .setTitle("Something went wrong while playing this track. Playing the next song").build()
            event.interaction.hook.sendMessageEmbeds(embed).queue()
        }
    }

}