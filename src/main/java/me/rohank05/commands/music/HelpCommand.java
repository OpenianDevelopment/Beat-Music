package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandManager;
import me.rohank05.utilities.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

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
        Button support = Button.link("https://discord.com/invite/a4zkCjg", "Support Server");
        Button vote = Button.link("https://top.gg/bot/881050313870684180/vote", "Vote");
        Button invite = Button.link("https://discord.com/oauth2/authorize?client_id=881050313870684180&scope=bot%20applications.commands", "Invite");
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription(commandString).setTitle("Help").build();
        Message helpMessage = (Message) new MessageCreateBuilder().addComponents((LayoutComponent) support,(LayoutComponent) invite,(LayoutComponent) vote).setEmbeds(embed).build();
        event.getInteraction().getHook().sendMessage((MessageCreateData) helpMessage).queue();
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
