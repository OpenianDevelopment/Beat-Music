package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackManager extends AudioEventAdapter {
    public final AudioPlayer audioPlayer;
    public final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private final TextChannel textChannel;
    public final Filters filters;
    //Constructor
    public TrackManager(AudioPlayer audioPlayer, TextChannel textChannel){
        this.audioPlayer = audioPlayer;
        this.textChannel = textChannel;
        this.filters = new Filters(this.audioPlayer);
    }

    //Add tracks to the queue
    public void addToQueue(AudioTrack track){
        if(this.audioPlayer.startTrack(track, true)){
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
            playNextTrack();
        }
    }
}
