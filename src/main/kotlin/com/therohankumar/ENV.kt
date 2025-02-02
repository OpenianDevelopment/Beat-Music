package com.therohankumar

import io.github.cdimascio.dotenv.dotenv

object ENV {
    private val dotenv = dotenv()
    val DISCORD_TOKEN: String? = dotenv["DISCORD_TOKEN"]
    val IPV6_BLOCK: String? = dotenv.entries().find { it.key == "IPV6_BLOCK" }?.value
    val DISCORD_GUILD: String? = dotenv["DISCORD_GUILD"]
}