package me.rohank05.beat.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public final TextChannel channel;

    public TrackScheduler(AudioPlayer player, TextChannel channel){
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.channel = channel;
    }
    public void queue(AudioTrack track){
        if(!this.player.startTrack(track, true)){
            this.queue.offer(track);
        }
    }

    public void nextTrack(){
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(endReason.mayStartNext){
            nextTrack();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        this.channel.sendMessage(track.getInfo().title + " has started playing").queue();
    }

}
