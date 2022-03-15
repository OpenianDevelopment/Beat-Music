package me.rohank05.utilities.command;

import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class CommandPermissionCheck {
    public static boolean checkBasePermission(SlashCommandInteractionEvent event) {
        if (!event.getMember().getVoiceState().inAudioChannel()) {
            MessageEmbed embed = new EmbedBuilder().setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            if (!event.getGuild().getSelfMember().getVoiceState().getChannel().equals(event.getMember().getVoiceState().getChannel())) {
                MessageEmbed embed = new EmbedBuilder().setDescription("You need to join same voice channel as me").setColor(Color.RED).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermission(SlashCommandInteractionEvent event) {
        if (PlayerManager.getINSTANCE() == null) {
            MessageEmbed embed = new EmbedBuilder().setDescription("No Music is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if (PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() == null) {
            MessageEmbed embed = new EmbedBuilder().setDescription("No Song is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        return true;
    }
}
