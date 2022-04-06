package me.rohank05.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Objects;

public class ResumeCommand implements ICommand {
    private final PlayerManager playerManager;
    public ResumeCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if(!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;

        AudioPlayer audioPlayer = this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).audioPlayer;
        if(audioPlayer.isPaused()) {
            audioPlayer.setPaused(false);
            event.getInteraction().getHook()
                    .sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Player **Resuming**").build()).queue();
        }
        else {
            event.getInteraction().getHook()
                    .sendMessageEmbeds(new EmbedBuilder().setColor(16760143).setTitle("Player is not **Paused**").build()).queue();
        }
    }

    @Override
    public String getName() {
        return "resume";
    }

    @Override
    public String getDescription() {
        return "Resumes the paused song";
    }
}
