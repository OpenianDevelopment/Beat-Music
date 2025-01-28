package com.therohankumar.modules

import com.therohankumar.ENV
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.io.File
import java.nio.file.Paths
import kotlin.system.exitProcess

object Utilities {
    fun commandCheck(event: SlashCommandInteractionEvent): Boolean {
        event.deferReply().queue()
        if(!AudioPlayerManager.musicManagerExist(guildId = event.guild!!.idLong)) {
            val embed = EmbedUtils.createErrorEmbed("Not Playing Anything", "You need to start playing song first")
            event.hook.sendMessageEmbeds(embed).queue()
            return false
        }
        return true
    }
    // ENV check to make sure its exist and not blank
    fun envCheck() {
        val envFilePath = File(Paths.get("").toAbsolutePath().toString()+"/.env")
        if (!envFilePath.exists()){
            println("Missing .env File\nMake sure your .env file exist.\nExpected .env path ${envFilePath.absolutePath}}")
            exitProcess(0)
        }
        if (ENV.DISCORD_TOKEN == null){
            println("Empty Discord Token\nMake sure you put a token in the .env file.")
            exitProcess(0)
        }
    }
}