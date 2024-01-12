package com.therohankumar.beat.jda

import com.therohankumar.beat.music.Player
import com.therohankumar.beat.music.PlayerRegistry
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

class CommandContext(
    val beans: Beans,
    val event: SlashCommandInteractionEvent
) {
    @Component
    class Beans(
        val players: PlayerRegistry
    ) {
        lateinit var commandManager: CommandManager
    }

    val player: Player by lazy { beans.players.get(event.guild!!.idLong) }
    
}