package me.rohank05.beat.command.commands;

import me.rohank05.beat.Filter.Merger;
import me.rohank05.beat.command.*;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ResetFilterCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        Merger merger = Merger.getINSTANCE();
        merger.setEightDEnabled(false);
        merger.setNightcoreEnabled(false);
        PlayerManager.getINSTANCE().getMusicManager(event.getGuild())
                .audioPlayer.setFilterFactory(null);
        MessageEmbed embed = new EmbedBuilder()
                .setColor(16760143).setDescription("All filters has been disabled").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();

    }

    @Override
    public String getName() {
        return "reset-filter";
    }
}
