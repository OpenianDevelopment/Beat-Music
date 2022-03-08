package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;


import java.util.EnumSet;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class TrackManager extends AudioEventAdapter {
    public final AudioPlayer audioPlayer;
    public final BlockingDeque<AudioTrack> queue;
    private final TextChannel textChannel;
    public final Filters filters;
    public String loop;
    //Constructor
    public TrackManager(AudioPlayer audioPlayer, TextChannel textChannel){
        this.audioPlayer = audioPlayer;
        this.textChannel = textChannel;
        this.filters = new Filters(this.audioPlayer);
        this.queue = new LinkedBlockingDeque<>();
        this.loop = "off";
    }

    //Add tracks to the queue
    public void addToQueue(AudioTrack track){
        if(!this.audioPlayer.startTrack(track, true)){
            this.queue.offer(track);
        }
    }

    //Play next track from the queue
    public void playNextTrack(){
        this.audioPlayer.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext){
            if(this.loop.equals("queue")){
                AudioTrack newTrack = track.makeClone();
                this.queue.offer(newTrack);
            }
            else if(this.loop.equals("track")){
                AudioTrack newTrack = track.makeClone();
                this.queue.addFirst(newTrack);
            }
            playNextTrack();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        TextChannel nowPlayingChannel = textChannel.getJDA().getTextChannelById(textChannel.getIdLong());
        if(nowPlayingChannel.canTalk()){
            if(nowPlayingChannel.getGuild().getSelfMember().hasPermission(nowPlayingChannel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS))){
                MessageEmbed embed = new EmbedBuilder().setTitle("Now Playing").setColor(16760143).setTitle("Now Playing").addField("Title", track.getInfo().title, true).setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/default.jpg").build();
                nowPlayingChannel.sendMessageEmbeds(embed).queue();
            }
            else{
                nowPlayingChannel.sendMessage("**Now Playing**: `"+track.getInfo().title+"`").queue();
            }
        }


    }
}
