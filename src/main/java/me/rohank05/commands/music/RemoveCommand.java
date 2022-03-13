package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RemoveCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event)) return;
        int removeIndex = event.getOption("position").getAsInt();
        if(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.queue.size()< removeIndex){
            MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("This track do not exist").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        AudioTrack removedTrack = PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.queue.get(removeIndex - 1);
        PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.queue.remove(removeIndex-1);
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("["+removedTrack.getInfo().title+"]("+removedTrack.getInfo().uri+") removed from the queue").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
        return;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return null;
    }
}
