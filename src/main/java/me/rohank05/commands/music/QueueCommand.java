package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.Bot;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.misc.ButtonPaginator;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class QueueCommand implements ICommand {
    private final PlayerManager playerManager;
    public QueueCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        List<AudioTrack> queue = new ArrayList<>(this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.queue);
        if (queue.isEmpty()) {
            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("There is no other song in queue").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        String[] queueTracks = new String[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack track = queue.get(i);
            queueTracks[i] = "[" + track.getInfo().title + "](" + track.getInfo().uri + ")" + " By [" + track.getInfo().author + "]\n Added By: `"+ track.getUserData(User.class).getAsTag() +"`";
        }
        String str = "**Current Track**: [" +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title +
                "](" +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri +
                ") By " +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author +
                "\n";
        ButtonPaginator buttonPaginator = new ButtonPaginator(Bot.eventWaiter, 50000L, queueTracks, event.getJDA(), Set.of(event.getUser().getIdLong()), 10, true, str, new Color(16760132));
        event.getInteraction().getHook().sendMessage("Queue").queue(message -> buttonPaginator.paginate(message, 1));
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return "Display the queue";
    }
}
