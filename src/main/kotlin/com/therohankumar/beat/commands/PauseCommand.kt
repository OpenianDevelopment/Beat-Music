package com.therohankumar.beat.commands

import com.therohankumar.beat.jda.Command
import com.therohankumar.beat.jda.CommandContext
import net.dv8tion.jda.api.EmbedBuilder


class PauseCommand : Command("pause") {
    override suspend fun CommandContext.invoke() {
        player.pause()
        event.interaction.hook
            .sendMessageEmbeds(EmbedBuilder().setColor(16760143).setTitle("Player **Paused**").build()).queue()
    }
}