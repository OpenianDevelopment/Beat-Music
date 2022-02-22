package me.rohank05.utilities.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ICommand {
    void run(SlashCommandInteractionEvent event);

    String getName();

    String getDescription();
}
