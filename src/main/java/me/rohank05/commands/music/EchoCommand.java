package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class EchoCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event)) return;

        PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.filters.setEcho(!PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.filters.isEcho());
        PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.filters.updateFilter();
        String isActivated = PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.filters.isEcho() ? "Enabled" : "Disabled";
        MessageEmbed embed = new EmbedBuilder().setTitle("Echo filter **"+isActivated+"**").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "echo";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
