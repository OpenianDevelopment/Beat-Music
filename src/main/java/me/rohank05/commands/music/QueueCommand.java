package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.PageManager;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


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
        ArrayList<MessageEmbed> queueEmbeds = new ArrayList<>();
        String[] queueTracks = new String[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack track = queue.get(i);
            queueTracks[i] = "**" + (i + 1) + "**. [" + track.getInfo().title + "](" + track.getInfo().uri + ")" + " By [" + track.getInfo().author + "]";
        }
        for (int i = 0; i <= queueTracks.length; i += 10) {
            int endItem = Math.min(queueTracks.length, (i + 9));
            StringBuilder str = new StringBuilder();
            str.append("**Current Track**: [")
                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title)
                    .append("](")
                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri)
                    .append(") By ")
                    .append(this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author)
                    .append("\n");
            for (int j = i; j < endItem; j++) {
                str.append(queueTracks[j]).append("\n");
            }
            MessageEmbed embed = new EmbedBuilder().setDescription(str).setColor(16760143).setThumbnail(event.getGuild().getIconUrl()).setTitle("Queue").build();
            queueEmbeds.add(embed);
        }
        event.getInteraction().getHook().sendMessageEmbeds(queueEmbeds.get(0)).queue(message -> PageManager.getINSTANCE().paginate(message, queueEmbeds, event.getUser().getIdLong(), (long) 500000));
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
