package me.rohank05.beat.command.commands;

import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;


public class CheckMusicPermission {
    public static boolean checkPermission(SlashCommandInteractionEvent event){
        if(!event.getMember().getVoiceState().inAudioChannel()) {
            MessageEmbed embed = new EmbedBuilder().setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if(event.getGuild().getSelfMember().getVoiceState().inAudioChannel()){
            if(!event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
                MessageEmbed embed = new EmbedBuilder().setDescription("You need to join same voice channel as me").setColor(Color.RED).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return false;
            }
        }
        return true;
    }
    public static boolean checkRemainPermission(SlashCommandInteractionEvent event){
        if(PlayerManager.getINSTANCE() == null) {
            MessageEmbed embed = new EmbedBuilder().setDescription("No Music is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        if(PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() == null){
            MessageEmbed embed = new EmbedBuilder().setDescription("No Song is being played").setColor(Color.RED).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return false;
        }
        return true;
    }
}
