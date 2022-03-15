package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.TextChannel;

public class GuildMusicManager {
    public final AudioPlayer audioPlayer;
    public final TrackManager trackManager;
    private final AudioPlayerSendHandler audioPlayerSendHandler;

    public GuildMusicManager(AudioPlayerManager audioPlayerManager, TextChannel textChannel) {
        this.audioPlayer = audioPlayerManager.createPlayer();
        this.trackManager = new TrackManager(audioPlayer, textChannel);
        this.audioPlayer.addListener(this.trackManager);
        this.audioPlayerSendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public AudioPlayerSendHandler getAudioPlayerSendHandler() {
        return this.audioPlayerSendHandler;
    }
}
