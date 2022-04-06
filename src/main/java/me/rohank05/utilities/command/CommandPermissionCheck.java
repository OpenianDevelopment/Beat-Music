package me.rohank05.utilities.command;

import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Objects;

public class CommandPermissionCheck {
    public static boolean checkBasePermission(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            MessageEmbed embed = new EmbedBuilder().setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if (Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getSelfMember().getVoiceState()).inAudioChannel()) {
            if (!Objects.equals(event.getGuild().getSelfMember().getVoiceState().getChannel(), event.getMember().getVoiceState().getChannel())) {
                MessageEmbed embed = new EmbedBuilder().setDescription("You need to join same voice channel as me").setColor(Color.RED).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermission(SlashCommandInteractionEvent event, PlayerManager playerManager) {
        if (playerManager == null) {
            MessageEmbed embed = new EmbedBuilder().setDescription("No Music is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if (playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).audioPlayer.getPlayingTrack() == null) {
            MessageEmbed embed = new EmbedBuilder().setDescription("No Song is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        return true;
    }
}
