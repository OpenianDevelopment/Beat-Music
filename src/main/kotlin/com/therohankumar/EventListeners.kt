package com.therohankumar

import com.therohankumar.interfaces.ICommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.therohankumar.modules.AudioPlayerManager
import com.therohankumar.modules.CommandManager
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class EventListeners: ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(EventListeners::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)
    override fun onReady(event: ReadyEvent) {
        logger.info("Bot is online")
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        println(event.jda.guilds.size)
        if(event.guild === null) return
        val command: ICommand? = CommandManager.getCommand(event.name)
        scope.launch {
            command?.execute(event)
        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        if(event.guild.selfMember.voiceState === null || event.guild.selfMember.voiceState?.channel === null) return
        if(event.guild.selfMember.voiceState?.channel === event.channelLeft && event.guild.selfMember.voiceState!!.channel!!.members.size === 1) {
            event.guild.audioManager.closeAudioConnection()
            AudioPlayerManager.destroyMusicManager(event.guild.idLong)
        }
    }
}