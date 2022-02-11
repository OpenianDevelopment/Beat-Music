package me.rohank05.beat.command.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;


public class ResumeCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        if (!CheckMusicPermission.checkPermission(event)) return;
        AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer;
        EmbedBuilder embed = new EmbedBuilder();
        if (audioPlayer.isPaused()) {
            audioPlayer.setPaused(false);
            event.getInteraction().getHook().sendMessageEmbeds(embed.setColor(Color.RED).setDescription("Track Resumed").build());
        } else {
            event.getInteraction().getHook().sendMessageEmbeds(embed.setColor(Color.RED).setDescription("Track is not Paused").build());
        }

    }

    @Override
    public String getName() {
        return "resume";
    }
}
