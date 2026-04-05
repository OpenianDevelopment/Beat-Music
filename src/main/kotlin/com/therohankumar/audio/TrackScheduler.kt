package com.therohankumar.audio

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import java.util.Collections
import java.util.LinkedList

class TrackScheduler(
    val player: AudioPlayer,
    var onQueueEmpty: () -> Unit = {},
    var onTrackStart: (AudioTrack) -> Unit = {},
    var onTrackSkipped: (AudioTrack, Long) -> Unit = { _, _ -> }
) : AudioEventAdapter() {

    val queue: LinkedList<AudioTrack> = LinkedList()
    var loopMode: LoopMode = LoopMode.OFF
    var autoplay: Boolean = false

    /**
     * Attempts to start [track] immediately. If the player is busy, adds it to the queue.
     */
    fun queue(track: AudioTrack) {
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    /**
     * Advances to the next track according to the active loop mode.
     */
    fun nextTrack() {
        when (loopMode) {
            LoopMode.TRACK -> {
                player.playingTrack?.makeClone()?.let { player.startTrack(it, false) }
            }
            LoopMode.QUEUE -> {
                player.playingTrack?.makeClone()?.let { queue.offer(it) }
                val next = queue.poll()
                if (next != null) player.startTrack(next, false) else signalEmpty()
            }
            LoopMode.OFF -> {
                val next = queue.poll()
                if (next != null) player.startTrack(next, false) else signalEmpty()
            }
        }
    }

    fun shuffle() {
        Collections.shuffle(queue)
    }

    private fun signalEmpty() {
        player.stopTrack()
        onQueueEmpty()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        onTrackStart(track)
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        if (endReason == AudioTrackEndReason.STOPPED || endReason == AudioTrackEndReason.REPLACED) {
            onTrackSkipped(track, track.position)
        }
        if (endReason.mayStartNext) {
            nextTrack()
        }
    }

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: com.sedmelluq.discord.lavaplayer.tools.FriendlyException) {
        nextTrack()
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        nextTrack()
    }
}
