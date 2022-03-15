package me.rohank05;

import me.rohank05.utilities.command.CommandManager;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventListeners extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListeners.class);
    private final CommandManager commandManager = new CommandManager();

    //Events

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("{} is online", event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commandManager.run(event);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember()))
            if (event.getChannelJoined().getMembers().size() > 1)
                if (PlayerManager.getINSTANCE() != null)
                    if (PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.isPaused())
                        PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.setPaused(false);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        /**
         * Check if Bot has left the channel. If it had it will destroy the player
         */
        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel())
            if (PlayerManager.getINSTANCE() != null)
                if (PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null) {
                    PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.destroy();
                    PlayerManager.getINSTANCE().deleteGuildMusicManager(event.getGuild());
                }

        /**
         * Check if is the only member left in the channel. If yes then it will pause the song until another member join
         */
        if (event.getChannelLeft().getMembers().size() == 1)
            if (event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember()))
                if (PlayerManager.getINSTANCE() != null)
                    PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.setPaused(true);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        PageManager.getINSTANCE().initListener(event);
    }


}
