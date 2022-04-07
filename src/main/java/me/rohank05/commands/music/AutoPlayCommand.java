package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import me.rohank05.utilities.music.TrackManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class AutoPlayCommand implements ICommand {
    private final PlayerManager playerManager;
    public AutoPlayCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        TrackManager trackManager = this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager;
        String AutoPlayStatus = trackManager.isAutoPlay() ? "Autoplay Disabled" : "Autoplay Enabled";
        trackManager.setAutoPlay(!trackManager.isAutoPlay());
        MessageEmbed embed = new EmbedBuilder().setTitle(AutoPlayStatus).setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "autoplay";
    }

    @Override
    public String getDescription() {
        return "Enable or Disable autoplaying songs after queue ends";
    }
}
