package me.rohank05.beat.command;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public interface ICommand {
    void run(SlashCommandEvent event);
    String getName();
}
