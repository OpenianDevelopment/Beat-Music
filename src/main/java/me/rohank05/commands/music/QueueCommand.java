package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.Bot;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.misc.ButtonPaginator;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
//        ArrayList<MessageEmbed> queueEmbeds = new ArrayList<>();
        String[] queueTracks = new String[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack track = queue.get(i);
            queueTracks[i] = "[" + track.getInfo().title + "](" + track.getInfo().uri + ")" + " By [" + track.getInfo().author + "]";
        }
        String str = "**Current Track**: [" +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title +
                "](" +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri +
                ") By " +
                this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author +
                "\n";

//        for (int i = 0; i <= queueTracks.length; i += 10) {
//            int endItem = Math.min(queueTracks.length, (i + 9));
//            StringBuilder str = new StringBuilder();
//            str.append("**Current Track**: [")
//                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title)
//                    .append("](")
//                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri)
//                    .append(") By ")
//                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author)
//                    .append("\n");
//            for (int j = i; j < endItem; j++) {
//                str.append(queueTracks[j]).append("\n");
//            }
//            MessageEmbed embed = new EmbedBuilder().setDescription(str).setColor(16760143).setThumbnail(event.getGuild().getIconUrl()).setTitle("Queue").build();
//            queueEmbeds.add(embed);
//        }
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
