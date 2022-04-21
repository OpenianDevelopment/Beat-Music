package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.Bot;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.misc.RemoveCounter;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class RemoveCommand implements ICommand {
    private final PlayerManager playerManager;
    public RemoveCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        int removeIndex = Objects.requireNonNull(event.getOption("position")).getAsInt();
        if (this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.queue.size() < removeIndex) {
            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("This track do not exist").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        AudioTrack removedTrack = this.playerManager.getGuildMusicManager(event.getGuild()).trackManager.queue.get(removeIndex - 1);
        if(removedTrack.getUserData(User.class).getIdLong() == event.getUser().getIdLong()){
            this.playerManager.getGuildMusicManager(event.getGuild()).trackManager.queue.remove(removeIndex - 1);
            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("Song Removed: [" + removedTrack.getInfo().title + "](" + removedTrack.getInfo().uri + ")").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        RemoveCounter removeCounter = new RemoveCounter(Bot.eventWaiter, this.playerManager.getGuildMusicManager(event.getGuild()).trackManager);
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("Vote to remove song: [" + removedTrack.getInfo().title + "](" + removedTrack.getInfo().uri + ")").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue(message -> removeCounter.processSkip(message, Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()).getMembers().size()/2, removeIndex));
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove a song from the queue";
    }
}
