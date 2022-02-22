package me.rohank05.utilities.music;

import com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioTrack;
import com.github.topislavalinkplugins.topissourcemanagers.applemusic.AppleMusicSourceManager;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifyConfig;
import com.github.topislavalinkplugins.topissourcemanagers.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioSourceManager.ISRC_PATTERN;
import static com.github.topislavalinkplugins.topissourcemanagers.ISRCAudioSourceManager.QUERY_PATTERN;

@SuppressWarnings("unchecked")
public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagerMap = new HashMap();
    private final AudioPlayerManager audioPlayerManager;
    private TextChannel textChannel;

    public PlayerManager(TextChannel textChannel) {
        this.textChannel = textChannel;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(Config.get("SPOTIFY_ID"));
        spotifyConfig.setClientSecret(Config.get("SPOTIFY_SECRET"));
        spotifyConfig.setCountryCode("US");
        String[] providers = {
                "ytmsearch:\"" + ISRC_PATTERN + "\"",
                "ytmsearch:" + QUERY_PATTERN
        };
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(providers, spotifyConfig, this.audioPlayerManager));
        this.audioPlayerManager.registerSourceManager(new AppleMusicSourceManager(providers, "us", this.audioPlayerManager));
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return this.guildMusicManagerMap.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, textChannel);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioPlayerSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(event.getGuild());
        this.audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.trackManager.addToQueue(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Added to Queue")
                        .addField("Track Name", "[" + track.getInfo().title + "](" + track.getInfo().uri + ")", true)
                        .setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/default.jpg")
                        .build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> audioTracks = playlist.getTracks();
                if (playlist.isSearchResult()) {
                    trackLoaded(audioTracks.get(0));
                }
                else {
                    event.getInteraction().getHook().sendMessageEmbeds(new EmbedBuilder().setDescription("Enqueuing `"
                                    + String.valueOf(audioTracks.size())
                                    + "` songs from `"
                                    + playlist.getName()
                                    + "`").build())
                            .queue();
                    for (final AudioTrack track : audioTracks) {
                        guildMusicManager.trackManager.addToQueue(track);
                    }
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }

    public static PlayerManager getINSTANCE(TextChannel channel) {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager(channel);
        }
        return INSTANCE;
    }

    public static PlayerManager getINSTANCE() {
        return INSTANCE;
    }
}
