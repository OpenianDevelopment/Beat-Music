package com.therohankumar.modules

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(private val audioPlayer: AudioPlayer): AudioEventAdapter() {
    val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()
    var textChannel: TextChannel? = null
    fun queue(track: AudioTrack) {
        if(audioPlayer.playingTrack == null) {
            audioPlayer.startTrack(track, false)
        }
        else {
            queue.offer(track)
        }
    }

    fun shuffleTrack(){
        val shuffle = queue.shuffled()
        queue.clear()
        queue.addAll(shuffle)
    }

    fun shiftTrack(currentPosittion: Int, newPosition: Int) {
        val queueList = queue.toList()
        val songToMove = queueList[currentPosittion]
        val newQueueList = ArrayList(queueList).apply {
            remove(songToMove)
            val boundedPos = newPosition.coerceIn(0, size)
            add(boundedPos, songToMove)
        }

        queue.clear()
        queue.addAll(newQueueList)
    }

    fun nextTrack() {
        if(queue.isNotEmpty()) audioPlayer.startTrack(queue.poll(), false)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val embed = EmbedUtils.createNowPlayingEmbed(
            trackTitle = track.info.title,
            trackUrl = track.info.uri,
            author = track.info.author,
            durationMillis = track.duration,
            thumbnail = track.info.artworkUrl,
            requestedBy = track.userData as User
        )
        textChannel?.sendMessageEmbeds(embed)?.queue()
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException) {
        val embed = EmbedUtils.createErrorEmbed("Track Exception", exception.localizedMessage)
        textChannel?.sendMessageEmbeds(embed)?.queue()
    }
}
