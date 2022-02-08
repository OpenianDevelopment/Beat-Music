package me.rohank05.beat.command.commands;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;



import java.net.URL;
import java.util.EnumSet;



public class PlayCommand implements ICommand {
    @Override
    public void run(SlashCommandEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final Member member = event.getMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        final TextChannel channel = event.getTextChannel();
        final AudioManager audioManager = event.getGuild().getAudioManager();
        String track = event.getOption("song-or-playlist").getAsString();
        if (!memberVoiceState.inVoiceChannel()) {
            MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        if (selfVoiceState.inVoiceChannel()) {
            if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
                MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("You need to be in same channel as me").setColor(16760143).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }
        }else {
            EnumSet<Permission> permissionList = self.getPermissions(memberVoiceState.getChannel());
            if(!permissionList.contains(Permission.VOICE_CONNECT) || !permissionList.contains(Permission.VOICE_SPEAK)){
                MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("I don't have permission to connect and speak in this channel").setColor(16760143).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }
            audioManager.openAudioConnection(memberVoiceState.getChannel());
        }
        if(!isURL(track)){
            track = "ytsearch:" + track;
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
