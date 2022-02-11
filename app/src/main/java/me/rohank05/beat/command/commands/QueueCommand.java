package me.rohank05.beat.command.commands;


import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;
import me.rohank05.beat.paginator.PageManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.ArrayList;


public class QueueCommand implements ICommand {

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        if (!CheckMusicPermission.checkPermission(event)) return;
        ArrayList<AudioTrack> queue = PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).scheduler.queue;
        if (queue.isEmpty()) {
            MessageEmbed embed = new EmbedBuilder()
                    .setDescription("Queue is empty")
                    .setColor(16760143)
                    .build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        ArrayList<MessageEmbed> embeds = new ArrayList<>();
        String[] track = new String[queue.size()];
        for (int i = 0; i < queue.size(); i++) {
            AudioTrack song = queue.get(i);
            track[i] = "**" + i + "** [" + song.getInfo().title + "](" + song.getInfo().uri + ")" + " by " + song.getInfo().author;
        }

        for (int i = 0; i <= track.length; i += 10) {
            int startItem = i;
            int endItem = track.length > (i + 9) ? (i + 9) : track.length;
            StringBuilder str = new StringBuilder();
            str.append("**Current Track**: [")
                    .append(PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title)
                    .append("](")
                    .append(PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri)
                    .append(") By ")
                    .append(PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().author)
                    .append("\n");
            for (int j = startItem; j < endItem; j++) {
                str.append(track[j]).append("\n");
            }
            MessageEmbed embed = new EmbedBuilder().setDescription(str).setColor(event.getMember().getColor()).setThumbnail(event.getGuild().getIconUrl()).setTitle("Queue").build();
            embeds.add(embed);
        }
        Button forward = Button.of(ButtonStyle.PRIMARY, "1", "forward", Emoji.fromMarkdown("➡️"));
        Button backward = Button.of(ButtonStyle.PRIMARY, "2", "backward", Emoji.fromMarkdown("⬅️"));
        Message message = new MessageBuilder().setActionRows(ActionRow.of(backward, forward)).setEmbeds(embeds.get(0)).build();
        event.getInteraction().getHook().sendMessage(message)
                .queue(buttonMessage -> {
                    PageManager.getINSTANCE().setPaginator(buttonMessage.getIdLong(), embeds, 0, event.getUser().getIdLong());
                    new Thread(() -> {
                        try {
                            Thread.sleep(60000);
                            PageManager.getINSTANCE().getPaginator().remove(buttonMessage.getIdLong());
                            buttonMessage.editMessageComponents();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }).start();
                });

    }


    @Override
    public String getName() {
        return "queue";
    }
}
