package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class SkipCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;
        MessageEmbed embed = new EmbedBuilder().setTitle("Song Skipped").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
        PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.playNextTrack();
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
