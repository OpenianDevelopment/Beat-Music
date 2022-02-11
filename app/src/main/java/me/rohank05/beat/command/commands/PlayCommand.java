package me.rohank05.beat.command.commands;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


import java.awt.*;
import java.net.URL;


public class PlayCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CheckMusicPermission.checkPermission(event)) return;

        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            if(!event.getGuild().getAudioManager().isConnected()){
                event.getGuild().getAudioManager().openAudioConnection(event.getGuild().getSelfMember().getVoiceState().getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
        }else {
            try{
                event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
            catch (Exception e){
                MessageEmbed embed = new EmbedBuilder().setColor(Color.RED).setDescription(e.getMessage()).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }

        }
        String track = event.getOption("song").getAsString();
        if(!isURL(track)){
            track = "ytmsearch:" + track;
        }
        PlayerManager.getINSTANCE(event.getTextChannel()).loadAndPlay(event, track);
    }

    @Override
    public String getName() {
        return "play";
    }

    private Boolean isURL(String url){
        try{
            new URL(url).toURI();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
