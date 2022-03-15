package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandManager;
import me.rohank05.utilities.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class HelpCommand implements ICommand {
    private final CommandManager manager;

    public HelpCommand(CommandManager manager) {
        this.manager = manager;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        List<ICommand> commandList = manager.getCommandList();
        StringBuilder commandString = new StringBuilder();
        for (ICommand command : commandList) {
            commandString.append("`").append(command.getName()).append("` - ").append(command.getDescription()).append("\n");
        }
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription(commandString).setTitle("Help").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "list all commands";
    }
}
