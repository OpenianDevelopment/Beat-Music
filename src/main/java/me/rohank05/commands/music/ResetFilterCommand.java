package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.Filters;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ResetFilterCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event)) return;
        Filters filters = PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.filters;
        filters.resetFilter();
        filters.updateFilter();
        event.getInteraction().getHook()
                .sendMessageEmbeds(new EmbedBuilder()
                        .setTitle("Disabled all filters").setColor(16760143).build()).queue();
    }

    @Override
    public String getName() {
        return "reset-filter";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
