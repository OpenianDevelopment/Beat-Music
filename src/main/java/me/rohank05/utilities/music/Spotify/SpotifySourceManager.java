package me.rohank05.utilities.music.Spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.readNullableText;
import static com.sedmelluq.discord.lavaplayer.tools.DataFormatTools.writeNullableText;
import static java.lang.Thread.sleep;

public class SpotifySourceManager implements AudioSourceManager {

    private final Pattern SPOTIFY_URL_PATTERN = Pattern.compile("(https?://)?(www\\.)?open\\.spotify\\.com/(user/[a-zA-Z0-9-_]+/)?(?<type>track|album|playlist|artist)/(?<identifier>[a-zA-Z0-9-_]+)");
    private static final Logger logger = LoggerFactory.getLogger(SpotifySourceManager.class);
    public SpotifyApi spotify;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private final AudioPlayerManager audioPlayerManager;

    public SpotifySourceManager(SpotifyConfig spotifyConfig, AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
        this.spotify = new SpotifyApi.Builder().setClientId(spotifyConfig.clientId).setClientSecret(spotifyConfig.clientSecret).build();
        this.clientCredentialsRequest = this.spotify.clientCredentials().build();
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    try {
                        var clientCredentials = this.clientCredentialsRequest.execute();
                        this.spotify.setAccessToken(clientCredentials.getAccessToken());
                        sleep(clientCredentials.getExpiresIn() * 1000);
                    } catch (IOException | SpotifyWebApiException | ParseException e) {
                        logger.error("Failed to update the spotify access token. Retrying in 1 minute ", e);
                        sleep(60 * 1000);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to update spotify Token", e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return audioPlayerManager;
    }


    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        Matcher matcher = SPOTIFY_URL_PATTERN.matcher(reference.identifier);
        if (!matcher.find()) {
            return null;
        }
        try {
            String identifier = matcher.group("identifier");
            switch (matcher.group("type")) {
                case "track" -> {
                    return this.getTrack(identifier);
                }
                case "album" -> {
                    return this.getAlbum(identifier);
                }
                case "playlist" -> {
                    return this.getPlaylist(identifier);
                }
                case "artist" -> {
                    return this.getArtistTrack(identifier);
                }
            }
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public SpotifyTrack getTrack(String id) throws IOException, ParseException, SpotifyWebApiException {
        Track spotifyTrack = this.spotify.getTrack(id).build().execute();
        AudioTrackInfo audioTrack = new AudioTrackInfo(spotifyTrack.getName(), spotifyTrack.getArtists()[0].getName(),spotifyTrack.getDurationMs(), spotifyTrack.getId(), false, "https://open.spotify.com/track/" + spotifyTrack.getId());
        return new SpotifyTrack(audioTrack, spotifyTrack.getExternalIds().getExternalIds().getOrDefault("isrc", null), this);
    }

    public AudioItem getAlbum(String id) throws IOException, ParseException, SpotifyWebApiException {
        Album album = this.spotify.getAlbum(id).build().execute();
        ArrayList<AudioTrack> audioTracks = new ArrayList<>();
        Paging<TrackSimplified> paging = null;
        do{
            paging = this.spotify.getAlbumsTracks(id).limit(50).offset(paging == null ? 0 : paging.getOffset() + 50).build().execute();
            for(var spotifyTrack : paging.getItems()){
                AudioTrackInfo audioTrack = new AudioTrackInfo(spotifyTrack.getName(), spotifyTrack.getArtists()[0].getName(), spotifyTrack.getDurationMs(), spotifyTrack.getId(), false, "https://open.spotify.com/track/" + spotifyTrack.getId());
                audioTracks.add(new SpotifyTrack(audioTrack, null, this));
            }
        }
        while(paging.getNext() != null);
        return new BasicAudioPlaylist(album.getName(), audioTracks, null, false);
    }

    public AudioItem getPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException {
        Playlist spotifyPlaylist = this.spotify.getPlaylist(id).build().execute();
        List<AudioTrack> audioTracks = new ArrayList<>();
        Paging<PlaylistTrack> paging = null;
        do {
            paging = this.spotify.getPlaylistsItems(id).limit(100).offset(paging == null ? 0 : paging.getOffset() + 100).build().execute();
            for (PlaylistTrack playlistTrack : paging.getItems()) {
                Track spotifyTrack = (Track) playlistTrack.getTrack();
                AudioTrackInfo audioTrack = new AudioTrackInfo(spotifyTrack.getName(), spotifyTrack.getArtists()[0].getName(), spotifyTrack.getDurationMs(), spotifyTrack.getId(), false, "https://open.spotify.com/track/" + spotifyTrack.getId());
                audioTracks.add(new SpotifyTrack(audioTrack, spotifyTrack.getExternalIds().getExternalIds().getOrDefault("isrc", null), this));
            }
        } while(paging.getNext() != null);
        return new BasicAudioPlaylist(spotifyPlaylist.getName(), audioTracks, null, false);
    }

    public AudioItem getArtistTrack(String id) throws IOException, ParseException, SpotifyWebApiException {
        String artistName = this.spotify.getArtist(id).build().execute().getName();
        List<AudioTrack> audioTracks = new ArrayList<>();
        Track[] spotifyTracks = this.spotify.getArtistsTopTracks(id, CountryCode.IN).build().execute();
        for(Track spotifyTrack: spotifyTracks){
            AudioTrackInfo audioTrack = new AudioTrackInfo(spotifyTrack.getName(), artistName, spotifyTrack.getDurationMs(), spotifyTrack.getId(), false, spotifyTrack.getUri());
            audioTracks.add(new SpotifyTrack(audioTrack, spotifyTrack.getExternalIds().getExternalIds().getOrDefault("isrc", null), this));
        }
        return new BasicAudioPlaylist(artistName+"'s Songs", audioTracks, null, false);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        SpotifyTrack spotifyTrack = (SpotifyTrack) track;
        writeNullableText(output, spotifyTrack.getIsrc());
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new SpotifyTrack(trackInfo, readNullableText(input), this);
    }

    @Override
    public void shutdown() {

    }
}
