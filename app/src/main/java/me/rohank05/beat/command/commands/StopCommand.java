package me.rohank05.beat.command.commands;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.GuildMusicManager;
import me.rohank05.beat.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        if (!CheckMusicPermission.checkPermission(event)) return;
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();
        MessageEmbed embed = new EmbedBuilder().setDescription("Player Stopped and Queue Cleared").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "stop";
    }
}
