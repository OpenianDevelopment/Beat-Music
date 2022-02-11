package me.rohank05.beat.command.commands;

import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


public class SkipCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        if (!CheckMusicPermission.checkPermission(event)) return;
        MessageEmbed embed = new EmbedBuilder().setDescription("**Song Skipped**").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
        PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).scheduler.nextTrack();
    }

    @Override
    public String getName() {
        return "skip";
    }
}
