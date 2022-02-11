package me.rohank05.beat.command.commands;

import me.rohank05.beat.Filter.Merger;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


public class EightDCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkPermission(event)) return;
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        Merger.getINSTANCE().setEightDEnabled(!Merger.getINSTANCE().isEightDEnabled());
        PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setFilterFactory(((track, format, output) ->
                Merger.getINSTANCE().enableFilter(output, format)
        ));
        String isEnabled = Merger.getINSTANCE().isEightDEnabled() ? "Activated" : "Deactivated";
        MessageEmbed embed = new EmbedBuilder()
                .setColor(16760143).setDescription("8D filter is **" + isEnabled + "**").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();

    }

    @Override
    public String getName() {
        return "8d";
    }
}
