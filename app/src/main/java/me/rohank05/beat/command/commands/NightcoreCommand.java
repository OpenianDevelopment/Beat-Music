package me.rohank05.beat.command.commands;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import me.rohank05.beat.Filter.Merger;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class NightcoreCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CheckMusicPermission.checkPermission(event)) return;
        if (!CheckMusicPermission.checkRemainPermission(event)) return;
        Merger.getINSTANCE().setNightcoreEnabled(!Merger.getINSTANCE().isNightcoreEnabled());
        PlayerManager.getINSTANCE().getMusicManager(event.getGuild())
                .audioPlayer.setFilterFactory(((track, format, output) ->
                        Merger.getINSTANCE().enableFilter(output, format)
                ));
        String isEnabled = Merger.getINSTANCE().isNightcoreEnabled() ? "Activated" : "Deactivated";
        MessageEmbed embed = new EmbedBuilder()
                .setColor(16760143).setDescription("Nightcore filter is **"+ isEnabled+"**").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "nightcore";
    }
}
