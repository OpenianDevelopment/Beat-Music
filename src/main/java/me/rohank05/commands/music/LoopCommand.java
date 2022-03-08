package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Locale;

public class LoopCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event)) return;
        String LoopSubcommand = event.getSubcommandName().toLowerCase();
        PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.loop = LoopSubcommand;
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(16760143);
        if(LoopSubcommand.equals("off")){
            embedBuilder.setDescription("Loop Off!!");
        }
        else if(LoopSubcommand.equals("queue"))
            embedBuilder.setDescription("Looping Queue!");
        else if(LoopSubcommand.equals("track"))
            embedBuilder.setDescription("Looping Track!");
        event.getInteraction().getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
