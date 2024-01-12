package com.therohankumar.beat.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.springframework.stereotype.Component
import java.nio.Buffer
import java.nio.ByteBuffer

class Player(val beans: Beans) : AudioEventAdapter(), AudioSendHandler {

    @Component
    class Beans(
        val apm: AudioPlayerManager
    )

    private val player = beans.apm.createPlayer().apply {
        addListener(this@Player)
        volume = 100
    }
    private val queue = Queue()
    private val buffer: ByteBuffer = ByteBuffer.allocate(1024)
    private val frame: MutableAudioFrame = MutableAudioFrame().apply { setBuffer(buffer) }

    var volume: Int
        get() = player.volume
        set(value) {
            player.volume = value
        }

    val tracks: List<AudioTrack>
        get() {
            val tracks = queue.track.toMutableList()
            player.playingTrack?.let { tracks.add(0, it) }
            return tracks
        }

    val remaningDuration: Long
        get() {
            var duration = 0L
            if (player.playingTrack != null && !player.playingTrack.info.isStream)
                player.playingTrack?.let { duration = it.info.length - it.position }
            return duration + queue.duration
        }
    
    var isRepeating: Boolean = false
    var lastChannel: TextChannel? = null
    var autoPlay: Boolean = false

    fun add(vararg track: AudioTrack): Boolean {
        queue.add(*track)
        if (player.playingTrack == null) {
            player.playTrack(queue.take()!!)
            return true
        }
        return false
    }

    fun skip() = player.stopTrack()

    fun pause() {
        player.isPaused = true
    }

    fun resume() {
        player.isPaused = false
    }

    fun shuffle() {
        queue.shuffle()
    }

    fun stop() {
        queue.clear()
        player.stopTrack()
    }

    override fun canProvide(): Boolean {
        return player.provide(frame)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val embed: MessageEmbed = EmbedBuilder().setTitle("Now Playing")
            .setThumbnail(track.info.artworkUrl)
            .setColor(16760143)
            .setTitle("Now Playing")
            .addField("Title", "[" + track.info.title + "](" + track.info.uri + ")", true).build()
        lastChannel!!.sendMessageEmbeds(embed).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (isRepeating && endReason.mayStartNext) {
            queue.add(track.makeClone())
        }
        val new = queue.take() ?: return
        player.playTrack(new)

    }

    override fun provide20MsAudio(): ByteBuffer {
        (buffer as Buffer).flip()
        return buffer
    }

    override fun isOpus(): Boolean = true

}