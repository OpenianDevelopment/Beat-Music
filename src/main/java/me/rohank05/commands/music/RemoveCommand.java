package me.rohank05.commands.music;

import me.rohank05.utilities.command.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RemoveCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {

    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
