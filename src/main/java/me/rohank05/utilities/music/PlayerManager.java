package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.lava.extensions.youtuberotator.YoutubeIpRotatorSetup;
import com.sedmelluq.lava.extensions.youtuberotator.planner.NanoIpRoutePlanner;
import com.sedmelluq.lava.extensions.youtuberotator.tools.ip.Ipv6Block;
import me.rohank05.Config;
import me.rohank05.SpotifyConfig;
import me.rohank05.SpotifySourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        SpotifyConfig spotifyConfig = new SpotifyConfig(Config.get("SPOTIFY_ID"), Config.get("SPOTIFY_SECRET"));
        YoutubeAudioSourceManager youtubeAudioSourceManager = new YoutubeAudioSourceManager(true, "rohan.shuvam@gmail.com", "$uPerNova123");
        this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(spotifyConfig, this.audioPlayerManager));
        this.audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
//        new YoutubeIpRotatorSetup(new NanoIpRoutePlanner(Collections.singletonList(new Ipv6Block("2a02:c207:2061:129::/64")), true)).forSource(youtubeAudioSourceManager).setup();
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return this.guildMusicManagerMap.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, textChannel);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioPlayerSendHandler());
            return guildMusicManager;
        });
    }

    public void deleteGuildMusicManager(Guild guild) {
        this.guildMusicManagerMap.remove(guild.getIdLong());
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl) {
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(event.getGuild());
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
                                    + String.valueOf(audioTracks.size())
                                    + "` songs from `"
                                    + playlist.getName()
                                    + "` Added By `" + event.getUser().getAsTag() + "`").build())
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
