package me.rohank05.utilities.command;

import me.rohank05.commands.music.*;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final List<ICommand> commandList = new ArrayList<>();

    public CommandManager(PlayerManager playerManager) {
        //Help
        addCommand(new HelpCommand(this));
        //Control commands
        addCommand(new PlayCommand(playerManager));
        addCommand(new PauseCommand(playerManager));
        addCommand(new SkipCommand(playerManager));
        addCommand(new LeaveCommand(playerManager));
        addCommand(new LoopCommand(playerManager));
        addCommand(new StopCommand(playerManager));
        addCommand(new QueueCommand(playerManager));
        addCommand(new RemoveCommand(playerManager));
        addCommand(new ShuffleCommand(playerManager));
        addCommand(new NowPlayingCommand(playerManager));
        addCommand(new ResumeCommand(playerManager));
        addCommand(new VolumeCommand(playerManager));
        addCommand(new AutoPlayCommand(playerManager));

        //Filters
        addCommand(new NightcoreCommand(playerManager));
        addCommand(new EightDCommand(playerManager));
        addCommand(new ResetFilterCommand(playerManager));
        addCommand(new VibratoCommand(playerManager));
        addCommand(new TremoloCommand(playerManager));
        addCommand(new BassBoostCommand(playerManager));
        addCommand(new EchoCommand(playerManager));
    }

    private void addCommand(ICommand command) {
        commandList.add(command);
    }

    public List<ICommand> getCommandList() {
        return commandList;
    }

    @Nullable
    private ICommand getCommand(String commandName) {
        for (ICommand command : this.commandList)
            if (command.getName().equals(commandName))
                return command;
        return null;
    }


    public void run(@NotNull SlashCommandInteractionEvent event) {
        ICommand command = getCommand(event.getName());
        if (!event.isFromGuild()) {
            event.getInteraction().getHook().sendMessage("This command can only be used in a server").queue();
            return;
        }
        if (command != null){
            event.deferReply().queue();
            command.run(event);
        }
    }
}
