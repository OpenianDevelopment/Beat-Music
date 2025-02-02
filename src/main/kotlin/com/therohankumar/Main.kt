package com.therohankumar

import com.therohankumar.modules.CommandManager
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import com.therohankumar.modules.Utilities.envCheck

fun main() {
    envCheck()
    CommandManager.registerAllCommand()
    val jda = JDABuilder.createDefault(ENV.DISCORD_TOKEN).enableIntents(GatewayIntent.GUILD_VOICE_STATES).enableCache(CacheFlag.VOICE_STATE).addEventListeners(
        EventListeners(), AudioFilterSelectListeners()
    ).build()
    jda.awaitReady()
    if(ENV.DISCORD_GUILD == null || ENV.DISCORD_GUILD == "") {
        jda.updateCommands().addCommands(CommandManager.getAllSlashCommand()).queue()
    }
    else{
        jda.getGuildById(ENV.DISCORD_GUILD)?.updateCommands()?.addCommands(CommandManager.getAllSlashCommand())?.queue()
    }
}