package me.rohank05.utilities.command;

import me.rohank05.commands.music.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final List<ICommand> commandList = new ArrayList<>();

    public CommandManager(){
        //Control commands
        addCommand(new PlayCommand());
        addCommand(new PauseCommand());
        addCommand(new SkipCommand());
        addCommand(new LeaveCommand());
        addCommand(new LoopCommand());
        addCommand(new StopCommand());
        addCommand(new QueueCommand());

        //Filters
        addCommand(new NightcoreCommand());
        addCommand(new EightDCommand());
        addCommand(new ResetFilterCommand());
        addCommand(new VibratoCommand());
        addCommand(new TremoloCommand());
    }

    private void addCommand(ICommand command){
        commandList.add(command);
    }

    @Nullable
    private ICommand getCommand(String commandName){
        for(ICommand command : this.commandList)
            if(command.getName().equals(commandName))
                return command;
        return null;
    }

    public void run(SlashCommandInteractionEvent event){
        ICommand command = getCommand(event.getName());
        event.deferReply().queue();
        if (!event.isFromGuild()) {
            event.getInteraction().getHook().sendMessage("This command can only be used in a server").queue();
            return;
        }
        if(command != null) command.run(event);
    }
}
