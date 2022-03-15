package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.Filters;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class TremoloCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;
        Filters filters = PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.filters;
        filters.setTremolo(!filters.isTremolo());
        filters.updateFilter();
        String isActivated = filters.isTremolo() ? "Enabled" : "Disabled";
        MessageEmbed embed = new EmbedBuilder().setTitle("Tremolo filter **" + isActivated + "**").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "tremolo";
    }

    @Override
    public String getDescription() {
        return "Add Tremolo filter to the song";
    }
}
