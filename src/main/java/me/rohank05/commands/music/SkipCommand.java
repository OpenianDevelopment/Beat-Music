package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class SkipCommand implements ICommand {
    private final PlayerManager playerManager;
    public SkipCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        MessageEmbed embed = new EmbedBuilder().setTitle("Song Skipped").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
        this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.playNextTrack();
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skip the current playing song";
    }
}
