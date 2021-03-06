package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class BassBoostCommand implements ICommand {
    private final PlayerManager playerManager;
    public BassBoostCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;

        playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.filters.setBassBoost(playerManager.getGuildMusicManager(event.getGuild()).trackManager.filters.isBassBoost());
        playerManager.getGuildMusicManager(event.getGuild()).trackManager.filters.updateFilter();
        String isActivated = playerManager.getGuildMusicManager(event.getGuild()).trackManager.filters.isBassBoost() ? "Enabled" : "Disabled";
        MessageEmbed embed = new EmbedBuilder().setTitle("BassBoost filter **" + isActivated + "**").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "bass-boost";
    }

    @Override
    public String getDescription() {
        return "Boost the bass of the song";
    }
}
