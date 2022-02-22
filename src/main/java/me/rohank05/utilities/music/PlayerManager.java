package me.rohank05.utilities.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagerMap = new HashMap();
    private final AudioPlayerManager audioPlayerManager;
    private TextChannel textChannel;

    public PlayerManager(TextChannel textChannel){
        this.textChannel = textChannel;
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild){
        return this.guildMusicManagerMap.computeIfAbsent(guild.getIdLong(), (guildId)->{
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager, textChannel);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getAudioPlayerSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(SlashCommandInteractionEvent event, String trackUrl){
        final GuildMusicManager guildMusicManager = this.getGuildMusicManager(event.getGuild());
        this.audioPlayerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.trackManager.addToQueue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if(playlist.isSearchResult()){
                    trackLoaded(playlist.getTracks().get(0));
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
