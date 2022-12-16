package me.rohank05.utilities.misc;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.rohank05.utilities.music.TrackManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SkipCounter {
    private final EventWaiter waiter;

    private final TrackManager trackManager;
    private boolean interactionStopped = false;
    private int countReaction = 0;

    public SkipCounter(EventWaiter waiter, TrackManager trackManager){
        this.waiter = waiter;
        this.trackManager = trackManager;
    }

    public void processSkip(Message m, int amtUser){
        m.addReaction(Emoji.fromUnicode("U+2705")).queue(e-> waitForEvent(m, amtUser));
    }

    private void waitForEvent(Message m, int amtUser){
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
                            this.trackManager.audioPlayer.stopTrack();
                            interactionStopped = true;
                            MessageEmbed embed = new EmbedBuilder()
                                    .setDescription("Song Skipped")
                                            .setColor(16760143)
                                                    .build();
                            m.editMessageEmbeds(embed).queue();
                        }
                        waitForEvent(m, amtUser);
                    }
                },
                120,
                TimeUnit.SECONDS,
                () -> interactionStopped = true
                );
    }
}
