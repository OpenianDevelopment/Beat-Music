package me.rohank05.beat;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.command.commands.*;


import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class CommandManager {
    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager(){
        addCommand(new ShutdownCommand());
        // Baisc Feature
        addCommand(new PlayCommand());
        addCommand(new StopCommand());
        addCommand(new SkipCommand());
        addCommand(new QueueCommand());
        addCommand(new ResumeCommand());
        addCommand(new PauseCommand());
        // Filter Commands
        addCommand(new NightcoreCommand());
        addCommand(new EightDCommand());
        addCommand(new ResetFilterCommand());
    }
    private void addCommand(ICommand cmd){
        boolean nameFound = this.commands.stream().anyMatch((it)-> it.getName().equalsIgnoreCase(cmd.getName()));
        if(nameFound){
            throw new IllegalArgumentException("Command Name Already Exist");
        }
        commands.add(cmd);
    }

    @Nullable
    private ICommand getCommand(String search){
        for(ICommand cmd: this.commands){
            if(cmd.getName().equals(search)){
                return cmd;
            }
        }
        return null;
    }

    void run(@NotNull SlashCommandInteractionEvent event){
        ICommand cmd = this.getCommand(event.getName());
        event.getInteraction().deferReply().queue();
        if(!event.isFromGuild()){
            event.getInteraction().getHook().sendMessage("This command can only be used in a server").queue();
            return;
        }
        if(cmd!= null) cmd.run(event);
    }
}
