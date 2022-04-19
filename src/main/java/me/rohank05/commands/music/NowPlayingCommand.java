package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class NowPlayingCommand implements ICommand {
    private final PlayerManager playerManager;
    public NowPlayingCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        AudioTrack currentTrack = this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.audioPlayer.getPlayingTrack();
        if (currentTrack == null) return;

        long minutes = (currentTrack.getPosition() / 1000) / 60;
        long seconds = (currentTrack.getPosition() / 1000) % 60;
        MessageEmbed embed = new EmbedBuilder()
                .setColor(16760143).setTitle("Now Playing")
                .addField("Track Name", "[" + currentTrack.getInfo().title + "](" + currentTrack.getInfo().uri + ")", true)
                .addField("By", currentTrack.getInfo().author, true)
                .addField("Position:" , (
                        currentTrack.getPosition() / 1000) / 60
                        + ":"
                        + (currentTrack.getPosition() / 1000) % 60
                        + "/"
                        + (currentTrack.getDuration() /1000) / 60
                        + ":"
                        + (currentTrack.getDuration() /1000) % 60
                , false)
                .setThumbnail(currentTrack.getInfo().artworkUrl)
                .build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "now-playing";
    }

    @Override
    public String getDescription() {
        return "Show the current playing song";
    }
}
