package com.therohankumar.beat.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

class Queue {
    private val queue: MutableList<AudioTrack> = mutableListOf()
    val track: List<AudioTrack> get() = queue
    val duration: Long get() = queue.filterNot { it.info.isStream }.sumOf { it.info.length }
    fun add(vararg track: AudioTrack) {
        queue.addAll(track)
    }

    fun take() = queue.removeFirstOrNull()

    fun peek() = queue.firstOrNull()

    fun clear() = queue.clear()

    fun shuffle() {
        queue.shuffle()
    }
}