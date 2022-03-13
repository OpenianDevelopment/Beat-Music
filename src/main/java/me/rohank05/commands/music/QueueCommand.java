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


public class QueueCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event)) return;
        List<AudioTrack> queue = new ArrayList<>(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.queue);
        if(queue.isEmpty()){
            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("There is no other song in queue").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        ArrayList<MessageEmbed> queueEmbeds = new ArrayList<>();
        String[] queueTracks = new String[queue.size()];
        for(int i=0; i< queue.size(); i++){
            AudioTrack track = queue.get(i);
            queueTracks[i] = "**"+(i+1)+"**. ["+ track.getInfo().title+"]("+track.getInfo().uri+")"+ " By ["+track.getInfo().author+"]";
        }
        for(int i =0; i<= queueTracks.length; i+=10){
            int startItem = i;
            int endItem = queueTracks.length > (i + 9) ? (i + 9) : queueTracks.length;
            StringBuilder str = new StringBuilder();
            str.append("**Current Track**: [")
                    .append(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title)
                    .append("](")
                    .append(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri)
                    .append(") By ")
                    .append(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author)
                    .append("\n");
            for (int j = startItem; j < endItem; j++) {
                str.append(queueTracks[j]).append("\n");
            }
            MessageEmbed embed = new EmbedBuilder().setDescription(str).setColor(16760143).setThumbnail(event.getGuild().getIconUrl()).setTitle("Queue").build();
            queueEmbeds.add(embed);
        }
        event.getInteraction().getHook().sendMessageEmbeds(queueEmbeds.get(0)).queue(message -> {
            PageManager.getINSTANCE().paginate(message, queueEmbeds, event.getUser().getIdLong(), (long) 50000);
        });
    }

    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
