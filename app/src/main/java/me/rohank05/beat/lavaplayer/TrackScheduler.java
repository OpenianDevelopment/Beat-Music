package me.rohank05.beat.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {
    public final AudioPlayer player;
    public final ArrayList<AudioTrack> queue;
    public final TextChannel channel;

    public TrackScheduler(AudioPlayer player, TextChannel channel) {
        this.player = player;
        this.queue = new ArrayList<>();
        this.channel = channel;
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.add(track);
        }
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.remove(0), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        if (channel.getGuild().getSelfMember().getPermissions(channel).contains(Permission.MESSAGE_EMBED_LINKS)) {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Now Playing")
                    .addField("Track Name", "[" + track.getInfo().title + "](" + track.getInfo().uri + ")", true)
                    .addField("Length", String.format("%02d min %02d sec",
                            track.getDuration() / TimeUnit.MINUTES.toMillis(1),
                            track.getDuration() % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)
                    ), true)
                    .setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/default.jpg")
                    .build();
            this.channel.sendMessageEmbeds(embed).queue();
        } else {
            this.channel.sendMessage(track.getInfo().title + " has started playing").queue();
        }
    }

}
