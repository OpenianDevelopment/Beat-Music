package me.rohank05.beat;

import com.jagrosh.jdautilities.command.Command;
import me.duncte123.botcommons.BotCommons;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Listeners extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listeners.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        LOGGER.info("{} Ready!!", jda.getSelfUser().getAsTag());
        Guild guild = jda.getGuildById("939760380719292416");
        CommandListUpdateAction commands = guild.updateCommands();
        List<CommandPrivilege> permissions =new ArrayList<>();
        permissions.add(CommandPrivilege.enableUser("687893451534106669"));
        commands.addCommands(new CommandData("shutdown", "Shutdown the bot").setDefaultEnabled(false));
//        commands.queue();
//        guild.retrieveCommands().complete().forEach(command -> command.updatePrivileges(guild, permissions).queue());

    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        String commandName = event.getName();
        Interaction interaction = event.getInteraction();
        switch (commandName){
            case "shutdown":
                interaction.reply("Good Bye!!!").queue();
                BotCommons.shutdown(event.getJDA());
                break;
            default: System.out.print("Not Found!!!");
        }
    }
}
