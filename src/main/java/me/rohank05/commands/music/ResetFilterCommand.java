package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.Filters;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class ResetFilterCommand implements ICommand {
    private final PlayerManager playerManager;
    public ResetFilterCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        Filters filters = this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.filters;
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
        return "Reset all filters";
    }
}
