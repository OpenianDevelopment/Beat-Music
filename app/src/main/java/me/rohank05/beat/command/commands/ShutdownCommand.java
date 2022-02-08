package me.rohank05.beat.command.commands;

import me.duncte123.botcommons.BotCommons;
import me.rohank05.beat.command.ICommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.Interaction;

public class ShutdownCommand implements ICommand {
    @Override
    public void run(SlashCommandEvent event) {
        Interaction interaction = event.getInteraction();
        interaction.getHook().sendMessage("Good Bye!!!").queue();
        BotCommons.shutdown(event.getJDA());
    }

    @Override
    public String getName() {
        return "shutdown";
    }
}
