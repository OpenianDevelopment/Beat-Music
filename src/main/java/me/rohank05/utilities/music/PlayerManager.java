package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.Config;
import me.rohank05.SpotifyConfig;
import me.rohank05.SpotifySourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SuppressWarnings("unchecked")
public class PlayerManager {
    private final Map<Long, GuildMusicManager> guildMusicManagerMap = new HashMap();
    public final AudioPlayerManager audioPlayerManager;
    private final JDA jda;
    public YoutubeAudioSourceManager youtubeAudioSourceManager;


    public PlayerManager(JDA jda) {
        this.jda = jda;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        SpotifyConfig spotifyConfig = new SpotifyConfig(Config.get("SPOTIFY_ID"), Config.get("SPOTIFY_SECRET"));

        this.youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, Config.get("GMAIL_ID"), Config.get("GMAIL_PASSWORD"));
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager( spotifyConfig, audioPlayerManager));
        this.audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
 //       new YoutubeIpRotatorSetup(new NanoIpRoutePlanner(Collections.singletonList(new Ipv6Block(Config.get("IP_V6"))), true)).forSource(youtubeAudioSourceManager).setup();
        
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return this.guildMusicManagerMap.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, jda, this);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioPlayerSendHandler());
            return guildMusicManager;
        });
    }

    public boolean hasGuildMusicManager(Guild guild){
        return guildMusicManagerMap.containsKey(guild.getIdLong());
    }

    public void deleteGuildMusicManager(Guild guild) {
        this.guildMusicManagerMap.remove(guild.getIdLong());
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        this.audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(event.getUser());
                guildMusicManager.trackManager.addToQueue(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Added to Queue")
                        .addField("Track Name", "[" + track.getInfo().title + "](" + track.getInfo().uri + ")", true)
                        .addField("Added By", event.getUser().getAsTag(), true)
                        .setThumbnail(track.getInfo().artworkUrl)
                        .setColor(16760143)
                        .build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> audioTracks = playlist.getTracks();
                if (playlist.isSearchResult()) {
                    trackLoaded(audioTracks.get(0));
                } else {
                    event.getInteraction().getHook().sendMessageEmbeds(new EmbedBuilder().setDescription("Enqueuing `"
                                    + audioTracks.size()
                                    + "` songs from `"
                                    + playlist.getName()
                                    + "` Added By `" + event.getUser().getAsTag() + "`")
                            .setColor(16760143).build())
                            .queue();
                    for (final AudioTrack track : audioTracks) {
                        track.setUserData(event.getUser());
                        guildMusicManager.trackManager.addToQueue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                MessageEmbed embed = new EmbedBuilder().setColor(16760143).setTitle("Failed to get the song").build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MessageEmbed embed = new EmbedBuilder().setColor(16760143).setTitle("Something went wrong while playing this track. Playing the next song").build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                guildMusicManager.trackManager.playNextTrack();
            }
        });
    }

    public AudioItem getTrack(String query) throws ExecutionException, InterruptedException {
        CompletableFuture<AudioItem> audioTrackCompletableFuture = new CompletableFuture<>();
        this.audioPlayerManager.loadItem(query, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                audioTrackCompletableFuture.complete(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                audioTrackCompletableFuture.complete(playlist);
            }

            @Override
            public void noMatches() {
                audioTrackCompletableFuture.complete(null);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                audioTrackCompletableFuture.completeExceptionally(exception);
            }
        });
        audioTrackCompletableFuture.join();
        return audioTrackCompletableFuture.get();
    }

}
