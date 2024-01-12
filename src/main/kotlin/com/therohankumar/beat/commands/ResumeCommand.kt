package com.therohankumar.beat.commands

import com.therohankumar.beat.jda.Command
import com.therohankumar.beat.jda.CommandContext
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color


class ResumeCommand : Command("resume") {
    override suspend fun CommandContext.invoke() {
        player.resume()
        event.interaction.hook
            .sendMessageEmbeds(EmbedBuilder().setColor(Color.RED).setTitle("Player **Resuming**").build()).queue()
    }
}