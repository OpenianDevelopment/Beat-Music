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
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;
        AudioTrack currentTrack = PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.audioPlayer.getPlayingTrack();
        if (currentTrack == null) return;
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setTitle("Now Playing").addField("Track Name", "[" + currentTrack.getInfo().title + "](" + currentTrack.getInfo().uri + ")", true).addField("By", currentTrack.getInfo().author, true).setThumbnail(currentTrack.getInfo().artworkUrl).build();
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
