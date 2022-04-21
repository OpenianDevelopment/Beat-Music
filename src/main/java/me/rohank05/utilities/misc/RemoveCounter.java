package me.rohank05.utilities.misc;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.utilities.music.TrackManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RemoveCounter {
    private final EventWaiter waiter;
    private final TrackManager trackManager;
    private boolean interactionStopped = false;
    private int countReaction = 0;

    public RemoveCounter(EventWaiter waiter, TrackManager trackManager){
        this.waiter = waiter;
        this.trackManager = trackManager;
    }

    public void processSkip(Message m, int amtUser, int song){
        m.addReaction("✅").queue(e-> waitForEvent(m, amtUser, song));
    }

    private void waitForEvent(Message m, int amtUser, int song){
        final long messageId = m.getIdLong();
        waiter.waitForEvent(MessageReactionAddEvent.class,
                event -> {
                    if(interactionStopped) return false;
                    if(Objects.requireNonNull(event.getUser()).isBot()) return false;
                    return messageId == event.getMessageIdLong();
                },
                event -> {
                    if(Objects.equals(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel(), Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel())){
                        countReaction++;
                        if(countReaction>=amtUser){
                            AudioTrack track = trackManager.queue.get(song -1);
                            trackManager.queue.remove(song - 1);
                            interactionStopped = true;
                            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("Song Removed: [" + track.getInfo().title + "](" + track.getInfo().uri + ")").build();
                            m.editMessageEmbeds(embed).queue();
                        }
                        waitForEvent(m, amtUser, song);
                    }
                },
                120,
                TimeUnit.SECONDS,
                () -> interactionStopped = true
        );
    }
}
