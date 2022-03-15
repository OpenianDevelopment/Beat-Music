package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Objects;

public class PauseCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;

        AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).audioPlayer;
        if (audioPlayer.isPaused())
            event.getInteraction().getHook()
                    .sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Player is already paused").build()).queue();
        else {
            audioPlayer.setPaused(true);
            event.getInteraction().getHook()
                    .sendMessageEmbeds(new EmbedBuilder().setColor(16760143).setTitle("Player **Paused**").build()).queue();
        }
    }

    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pause the music";
    }
}
