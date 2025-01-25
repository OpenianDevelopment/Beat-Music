package com.therohankumar.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.interfaces.ICommand
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.EmbedUtils
import com.therohankumar.modules.GuildMusicManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.net.URL

class Play: ICommand {
    override val name = "play"

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        event.deferReply().queue()
        if(!event.guildChannel.asTextChannel().canTalk()) {
            val embed = EmbedUtils.createErrorEmbed("Permission Error", "I need permission to send message in this channel")
            event.hook.sendMessageEmbeds(embed).queue()
            return
        }
        val musicManager = AudioPlayerManager.getMusicManager(event.guild!!.idLong)
        val query = event.interaction.getOption("query")!!.asString
        if(musicManager.taskScheduler.textChannel === null) {
            musicManager.taskScheduler.textChannel = event.guildChannel.asTextChannel()
        }
        if(ensureVoiceChannel(event)) {
            event.guild!!.audioManager.sendingHandler = musicManager.sendHandler
            if(isURL(query)) {
                AudioPlayerManager.audioPlayerManager.loadItem(query, Loader(event, musicManager))
            }else{
                AudioPlayerManager.audioPlayerManager.loadItem("ytmsearch:${query}", Loader(event, musicManager))
            }
        }
    }

    private fun ensureVoiceChannel(event: SlashCommandInteractionEvent): Boolean {
        val ourVC = event.guild!!.selfMember.voiceState?.channel
        val theirVC = event.member!!.voiceState?.channel
        if (ourVC === null && theirVC === null) {
            event.hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed("Error", "You need to be in Voice Channel to use this command")).queue()
            return false
        }
        if(ourVC !== null && ourVC !== theirVC) {
            event.hook.sendMessageEmbeds(EmbedUtils.createErrorEmbed("Error", "You need to be in same Voice Channel as me")).queue()
            return false
        }
        event.guild!!.audioManager.openAudioConnection(theirVC)
        return true
    }

    private fun isURL(url: String): Boolean {
        try {
            URL(url).toURI()
            return true
        } catch (e: Exception) {
            return false
        }
    }

    inner class Loader(private val event: SlashCommandInteractionEvent, private val musicManager: GuildMusicManager) : AudioLoadResultHandler {
        override fun trackLoaded(track: AudioTrack) {
            musicManager.taskScheduler.queue(track)
            track.userData = event.user
            val embed = EmbedUtils.createAddedToQueueEmbed(
                trackTitle = track.info.title,
                trackUrl = track.info.uri,
                author = track.info.author,
                durationMillis = track.duration,
                thumbnail = track.info.artworkUrl,
                requestedBy = event.user
            )
            event.hook.sendMessageEmbeds(embed).queue()
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            when {
                playlist.isSearchResult -> {
                    // Handle single track from search
                    val track = playlist.tracks.first()
                    track.userData = event.user
                    musicManager.taskScheduler.queue(track)
                    val embed = EmbedUtils.createAddedToQueueEmbed(
                        trackTitle = track.info.title,
                        trackUrl = track.info.uri,
                        author = track.info.author,
                        durationMillis = track.duration,
                        thumbnail = track.info.artworkUrl,
                        requestedBy = event.user
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                }
                else -> {
                    // Handle actual playlist
                    val tracksToAdd = playlist.tracks.take(100)  // Limit to 100 tracks
                    tracksToAdd.forEach { track ->
                        track.userData = event.user
                        musicManager.taskScheduler.queue(track)
                    }
                    val tracksInfo = tracksToAdd.map { track ->
                        Triple(track.info.title, track.info.uri, track.info.author)
                    }

                    val embed = EmbedUtils.createPlaylistAddedEmbed(
                        playlistName = playlist.name,
                        tracksAdded = tracksToAdd.size,
                        tracks = tracksInfo,
                        thumbnail = tracksToAdd.first().info.artworkUrl,
                        firstTrackUrl = tracksToAdd.first().info.uri,
                        requestedBy = event.user
                    )
                    event.hook.sendMessageEmbeds(embed).queue()
                }
            }
        }

        override fun noMatches() {
            val embed = EmbedUtils.createErrorEmbed(
                title = "No Results Found",
                description = "Could not find any tracks matching your query."
            )
            event.hook.sendMessageEmbeds(embed).queue()
        }

        override fun loadFailed(exception: FriendlyException?) {
            val embed = EmbedUtils.createErrorEmbed(
                title = "Failed to Load Track",
                description = exception?.message ?: "An unknown error occurred while loading the track."
            )
            event.hook.sendMessageEmbeds(embed).queue()
        }
    }

    override fun createSlashCommand(): SlashCommandData {
        return Commands.slash("play", "Search and Play/Queue Song or Playlist")
            .addOption(OptionType.STRING, "query", "Name or URL", true)
    }
}