package me.rohank05.utilities.music.Spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SpotifyTrack extends DelegatedAudioTrack {

    private final SpotifySourceManager spotifySourceManager;
    private final String isrc;
    public SpotifyTrack(AudioTrackInfo audioTrackInfo, String isrc, SpotifySourceManager spotifySourceManager){
        super(audioTrackInfo);
        this.spotifySourceManager = spotifySourceManager;
        this.isrc = isrc;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {

        String query = "ytmsearch:";
        if(this.isrc != null){
            query += this.isrc;
        }else{
            query+= getInfo().title;
        }
        AudioItem audioItem = loadItem(query);
        if(audioItem instanceof AudioPlaylist){
            audioItem = ((AudioPlaylist) audioItem).getTracks().get(0);
        }
        if(audioItem instanceof InternalAudioTrack){
            processDelegate((InternalAudioTrack) audioItem, executor);
            return;
        }
        throw new FriendlyException("No matching Spotify track found", FriendlyException.Severity.COMMON, new SpotifyTrackNotFoundException());
    }

    public AudioItem loadItem(String query) throws ExecutionException, InterruptedException {
        CompletableFuture<AudioItem> completableFuture = new CompletableFuture<>();
        this.spotifySourceManager.getAudioPlayerManager().loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                completableFuture.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                completableFuture.complete(playlist);
            }

            @Override
            public void noMatches() {
                completableFuture.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                completableFuture.completeExceptionally(exception);
            }
        });
        completableFuture.join();
        return completableFuture.get();
    }

    public String getIsrc() {
        return isrc;
    }

}
