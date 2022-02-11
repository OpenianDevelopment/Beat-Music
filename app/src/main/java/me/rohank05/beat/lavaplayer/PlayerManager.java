package me.rohank05.beat.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.beat.Config;
import me.rohank05.beat.lavaplayer.sourcemod.spotify.SpotifyConfig;
import me.rohank05.beat.lavaplayer.sourcemod.spotify.SpotifySourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;
    TextChannel channel;

    public PlayerManager(TextChannel channel) {
        this.channel = channel;
        this.musicManagers = new HashMap();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        SpotifyConfig spotifyConfig = new SpotifyConfig();
        spotifyConfig.setClientId(Config.get("SPOTIFY_ID"));
        spotifyConfig.setClientSecret(Config.get("SPOTIFY_SECRET"));
        spotifyConfig.setCountryCode("US");
        this.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, spotifyConfig, this.audioPlayerManager));
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, channel);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(event.getGuild());
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Added to Queue")
                        .addField("Track Name", "[" + track.getInfo().title + "](" + track.getInfo().uri + ")", true)
                        .setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/default.jpg")
                        .build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                final List<AudioTrack> tracks = playlist.getTracks();
                if (playlist.isSearchResult()) {
                    this.trackLoaded(tracks.get(0));
                } else {
                    event.getInteraction().getHook().sendMessage("Enqueuing `"
                                    + String.valueOf(tracks.size())
                                    + "` songs from `"
                                    + playlist.getName()
                                    + "`")
                            .queue();
                    for (final AudioTrack track : tracks) {
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
                MessageEmbed embed = new EmbedBuilder()
                        .setDescription("No Result for the track")
                        .setColor(16760143)
                        .build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
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
