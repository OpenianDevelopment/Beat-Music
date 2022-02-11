package me.rohank05.beat.command.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.Color;


public class PauseCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        if (!CheckMusicPermission.checkPermission(event)) return;
        AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer;
        EmbedBuilder embed = new EmbedBuilder();
        if (audioPlayer.isPaused()) {
            event.getInteraction().getHook().sendMessageEmbeds(embed.setColor(Color.RED).setDescription("Track is already paused").build());
        } else {
            audioPlayer.setPaused(true);
            event.getInteraction().getHook().sendMessageEmbeds(embed.setColor(Color.RED).setDescription("Track Paused").build());
        }
    }

    @Override
    public String getName() {
        return "pause";
    }
}
