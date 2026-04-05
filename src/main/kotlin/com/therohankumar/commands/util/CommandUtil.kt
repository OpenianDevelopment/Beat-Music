package com.therohankumar.commands.util

import com.therohankumar.audio.GuildMusicManager
import com.therohankumar.audio.LoopMode
import com.therohankumar.audio.MusicManagerRegistry
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.awt.Color

object CommandUtil {

    val EMBED_COLOR   = Color(0x5865F2)   // Discord blurple
    val SUCCESS_COLOR = Color(0x57F287)   // Discord green
    val WARNING_COLOR = Color(0xFEE75C)   // Discord yellow
    val ERROR_COLOR   = Color(0xED4245)   // Discord red

    // ── Generic embeds ───────────────────────────────────────────────────────

    fun embed(title: String, description: String? = null, color: Color = EMBED_COLOR): MessageEmbed =
        EmbedBuilder().setTitle(title).setDescription(description).setColor(color).build()

    fun successEmbed(title: String, description: String? = null): MessageEmbed =
        EmbedBuilder().setTitle("✅  $title").setDescription(description).setColor(SUCCESS_COLOR).build()

    fun errorEmbed(message: String): MessageEmbed =
        EmbedBuilder()
            .setDescription("**✗  $message**")
            .setColor(ERROR_COLOR)
            .build()

    // ── Now Playing / Added to Queue ─────────────────────────────────────────

    fun nowPlayingEmbed(track: AudioTrack, manager: GuildMusicManager, queued: Boolean = false): MessageEmbed {
        val info     = track.info
        val position = if (queued) 0L else track.position
        val duration = track.duration
        val isLive   = info.isStream

        val progressLine = if (isLive) {
            "🔴  **LIVE**"
        } else {
            "${progressBar(position, duration)}  `${formatDuration(position)} / ${formatDuration(duration)}`"
        }

        val statusParts = mutableListOf<String>()
        if (manager.player.isPaused)         statusParts += "⏸ Paused"
        if (manager.scheduler.loopMode == LoopMode.TRACK) statusParts += "🔂 Looping track"
        if (manager.scheduler.loopMode == LoopMode.QUEUE) statusParts += "🔁 Looping queue"
        if (manager.scheduler.autoplay)      statusParts += "✨ Autoplay on"

        val queueSize = manager.scheduler.queue.size

        val embed = EmbedBuilder()
            .setColor(if (queued) EMBED_COLOR else SUCCESS_COLOR)
            .setTitle(if (queued) "Added to Queue" else "Now Playing")
            .setDescription("### [${info.title}](${info.uri})")
            .addField("", "by **${info.author}**", false)
            .addField("Duration", if (isLive) "🔴 Live" else formatDuration(duration), true)

        if (queued) {
            embed.addField("Position", "#${queueSize}", true)
        }

        if (!queued && !isLive) {
            embed.addField("", progressLine, false)
        }

        if (statusParts.isNotEmpty()) {
            embed.addField("", statusParts.joinToString("  ·  "), false)
        }

        if (!info.artworkUrl.isNullOrBlank()) {
            embed.setThumbnail(info.artworkUrl)
        }

        return embed.build()
    }

    // ── Guard helpers ────────────────────────────────────────────────────────

    fun requireVoice(event: SlashCommandInteractionEvent, registry: MusicManagerRegistry): GuildMusicManager? {
        val guild = event.guild ?: run {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue()
            return null
        }

        val userChannel = event.member?.voiceState?.channel ?: run {
            event.replyEmbeds(errorEmbed("You need to be in a voice channel first.")).setEphemeral(true).queue()
            return null
        }

        val self = guild.selfMember
        val audioManager = guild.audioManager

        if (audioManager.isConnected && audioManager.connectedChannel?.idLong != userChannel.idLong) {
            event.replyEmbeds(errorEmbed("I'm already playing in <#${audioManager.connectedChannel?.idLong}>. Join that channel to add songs."))
                .setEphemeral(true).queue()
            return null
        }

        if (!self.hasPermission(userChannel, Permission.VOICE_CONNECT)) {
            event.replyEmbeds(errorEmbed("I don't have permission to join **${userChannel.name}**."))
                .setEphemeral(true).queue()
            return null
        }
        if (!self.hasPermission(userChannel, Permission.VOICE_SPEAK)) {
            event.replyEmbeds(errorEmbed("I don't have permission to speak in **${userChannel.name}**."))
                .setEphemeral(true).queue()
            return null
        }

        val vc = userChannel as? VoiceChannel
        if (vc != null && vc.userLimit > 0 && vc.members.size >= vc.userLimit) {
            if (!self.hasPermission(Permission.ADMINISTRATOR)) {
                event.replyEmbeds(errorEmbed("**${vc.name}** is full.")).setEphemeral(true).queue()
                return null
            }
        }

        val manager = registry.getOrCreate(guild)
        if (!audioManager.isConnected) {
            audioManager.openAudioConnection(userChannel)
        }
        manager.cancelIdleDisconnect()
        return manager
    }

    fun requireSameVoice(event: SlashCommandInteractionEvent, registry: MusicManagerRegistry): GuildMusicManager? {
        val guild = event.guild ?: run {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue()
            return null
        }

        val manager = registry.get(guild.idLong) ?: run {
            event.replyEmbeds(errorEmbed("Nothing is playing right now.")).setEphemeral(true).queue()
            return null
        }

        if (manager.player.playingTrack == null) {
            event.replyEmbeds(errorEmbed("Nothing is playing right now.")).setEphemeral(true).queue()
            return null
        }

        val botChannel  = guild.audioManager.connectedChannel
        val userChannel = event.member?.voiceState?.channel

        if (botChannel != null && userChannel?.idLong != botChannel.idLong) {
            event.replyEmbeds(errorEmbed("You need to be in <#${botChannel.idLong}> to use this command."))
                .setEphemeral(true).queue()
            return null
        }

        return manager
    }

    fun requirePlaying(event: SlashCommandInteractionEvent, registry: MusicManagerRegistry): GuildMusicManager? {
        val guild = event.guild ?: run {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue()
            return null
        }
        val manager = registry.get(guild.idLong) ?: run {
            event.replyEmbeds(errorEmbed("Nothing is playing right now.")).setEphemeral(true).queue()
            return null
        }
        if (manager.player.playingTrack == null) {
            event.replyEmbeds(errorEmbed("Nothing is playing right now.")).setEphemeral(true).queue()
            return null
        }
        return manager
    }

    // ── Formatters ───────────────────────────────────────────────────────────

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours   = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
        else "%d:%02d".format(minutes, seconds)
    }

    fun parseTimestamp(input: String): Long? {
        val parts = input.trim().split(":").map { it.toLongOrNull() ?: return null }
        return when (parts.size) {
            1    -> parts[0] * 1000
            2    -> (parts[0] * 60 + parts[1]) * 1000
            3    -> (parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000
            else -> null
        }
    }

    fun progressBar(position: Long, duration: Long, length: Int = 17): String {
        if (duration <= 0) return "▱".repeat(length)
        val filled = ((position.toDouble() / duration) * length).toInt().coerceIn(0, length - 1)
        return "▰".repeat(filled) + "⬤" + "▱".repeat(length - filled - 1)
    }
}
