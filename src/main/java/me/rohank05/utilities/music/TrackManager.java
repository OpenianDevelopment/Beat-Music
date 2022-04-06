package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.EnumSet;
import java.util.List;
import java.util.Stack;


public class TrackManager extends AudioEventAdapter {
    public final AudioPlayer audioPlayer;
    public final Stack<AudioTrack> queue;
    private String textChannel;
    private final JDA jda;
    public final Filters filters;
    public String loop;
    private boolean autoPlay = true;
    private final PlayerManager playerManager;

    //Constructor
    public TrackManager(AudioPlayer audioPlayer, JDA jda, PlayerManager playerManager) {
        this.jda = jda;
        this.audioPlayer = audioPlayer;
        this.filters = new Filters(this.audioPlayer);
        this.queue = new Stack<>();
        this.loop = "off";
        this.playerManager = playerManager;
    }

    public void setTextChannel(String textChannel) {
        this.textChannel = textChannel;
    }

    public boolean hasTextChannel() {
        return this.textChannel != null;
    }

    //Add tracks to the queue
    public void addToQueue(AudioTrack track) {
        if (!this.audioPlayer.startTrack(track, true)) {
            this.queue.push(track);
        }

    }

    //Play next track from the queue
    public void playNextTrack() {
        if (this.queue.isEmpty()) {
            this.audioPlayer.destroy();
            return;
        }
        this.audioPlayer.startTrack(this.queue.firstElement(), false);
        this.queue.remove(0);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        if (endReason.mayStartNext) {
            if (this.loop.equals("queue")) {
                AudioTrack newTrack = track.makeClone();
                this.queue.push(newTrack);
            } else if (this.loop.equals("track")) {
                AudioTrack newTrack = track.makeClone();
                this.queue.add(0, newTrack);
            }
            if (this.queue.isEmpty()) {
                if (this.autoPlay) {
                    String trackID = track.getIdentifier();
                    String mixUrl = "https://music.youtube.com/watch?v=" + trackID + "&list=RD" + trackID;
                    this.playerManager.audioPlayerManager.loadItem(mixUrl, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            final List<AudioTrack> audioTracks = playlist.getTracks();
                            TextChannel channel = jda.getTextChannelById(textChannel);

                            audioTracks.remove(0);
                            for (final AudioTrack track : audioTracks) {
                                addToQueue(track);
                            }
                        }

                        @Override
                        public void noMatches() {}

                        @Override
                        public void loadFailed(FriendlyException exception) {}
                    });
                }
            }
            playNextTrack();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

        System.out.println(track.getInfo().identifier);
        System.out.println(track.getIdentifier());
        System.out.println(track.getInfo().uri);
        TextChannel nowPlayingChannel = jda.getTextChannelById(textChannel);

        if (nowPlayingChannel == null) return;
        if (nowPlayingChannel.canTalk()) {
            if (nowPlayingChannel.getGuild().getSelfMember().hasPermission(nowPlayingChannel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS))) {
                MessageEmbed embed = new EmbedBuilder().setTitle("Now Playing").setColor(16760143).setTitle("Now Playing").addField("Title", "[" + track.getInfo().title + "](" + track.getInfo().uri + ")", true).setThumbnail(track.getInfo().artworkUrl).build();
                nowPlayingChannel.sendMessageEmbeds(embed).queue();
            } else {
                nowPlayingChannel.sendMessage("**Now Playing**: `" + track.getInfo().title + "`").queue();
            }
        }
    }
}
