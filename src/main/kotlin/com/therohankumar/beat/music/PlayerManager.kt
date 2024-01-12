package com.therohankumar.beat.music

import org.springframework.stereotype.Service

@Service
class PlayerRegistry(val playerBeans: Player.Beans) {
    private val players: MutableMap<Long, Player> = mutableMapOf()
    fun get(guildId: Long) = players.computeIfAbsent(guildId) { Player(playerBeans) }
}