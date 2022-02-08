package me.rohank05.beat;

import com.jagrosh.jdautilities.command.Command;
import me.duncte123.botcommons.BotCommons;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.channel.voice.GenericVoiceChannelEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Listeners extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listeners.class);
    private final CommandManager manager = new CommandManager();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        LOGGER.info("{} Ready!!", jda.getSelfUser().getAsTag());
        Guild guild = jda.getGuildById("939760380719292416");
        CommandListUpdateAction commands = guild.updateCommands();
        List<CommandPrivilege> permissions = new ArrayList<>();
        permissions.add(CommandPrivilege.enableUser("687893451534106669"));
        commands.addCommands(new CommandData("play", "Play or add songs to the queue").addOption(OptionType.STRING, "song-or-playlist", "Song Name or Song URL or Playlist URL", true).setDefaultEnabled(true));
        commands.addCommands(new CommandData("join", "Join the Voice Channel, You are in").setDefaultEnabled(true));
        commands.addCommands(new CommandData("stop", "Stop the player").setDefaultEnabled(true));
        commands.addCommands(new CommandData("nightcore", "Add nightcore filter to the track").setDefaultEnabled(true));
        commands.queue();
//        guild.retrieveCommands().complete().forEach(command -> command.updatePrivileges(guild, permissions).queue());

    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        manager.run(event);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        /**
         * Check if Bot has left the channel. If it had it will destroy the player
         */
        if (!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
            if (PlayerManager.getINSTANCE() != null)
                if (PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null)
                    PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.destroy();

        /**
         * Check if is the only member left in the channel. If yes then it will pause the song until another member join
         */
        if (event.getChannelLeft().getMembers().size() == 1)
            if (event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember()))
                if (PlayerManager.getINSTANCE() != null)
                    PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setPaused(true);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

        /**
         * Resumes the player when another users join
         */
        if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember()))
            if (event.getChannelJoined().getMembers().size() > 1)
                if (PlayerManager.getINSTANCE() != null)
                    if (PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.isPaused())
                        PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setPaused(false);
    }
}
