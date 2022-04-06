package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Objects;


public class VolumeCommand implements ICommand {
    private final PlayerManager playerManager;
    public VolumeCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        int volume = Objects.requireNonNull(event.getOption("number")).getAsInt();
        this.playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).audioPlayer.setVolume(volume);
        event.getInteraction().getHook()
                .sendMessageEmbeds(new EmbedBuilder().setColor(Color.RED).setTitle("Volume set to" + volume).build()).queue();
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "change the volume of the player";
    }
}
