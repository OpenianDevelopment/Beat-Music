package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class LeaveCommand implements ICommand {
    private final PlayerManager playerManager;
    public LeaveCommand(PlayerManager playerManager){
        this.playerManager = playerManager;

    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getSelfMember().getVoiceState()).inAudioChannel()) return;
        event.getGuild().getAudioManager().closeAudioConnection();
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("Leaving Channel").build();
        playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.destroy();
        playerManager.deleteGuildMusicManager(event.getGuild());
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Leaves the channel";
    }
}
