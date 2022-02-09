package me.rohank05.beat.command.commands;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;

public class NightcoreCommand implements ICommand{
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CheckMusicPermission.checkPermission(event)) return;
        if(!CheckMusicPermission.checkRemainPermission(event)) return;
        PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setFilterFactory((track, format, output)->{
            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            audioFilter.setSpeed(1.29).setRate(1).setPitch(1.29); //1.5x normal speed
            return Collections.singletonList(audioFilter);
        });
        event.getInteraction().getHook().sendMessage("Added").queue();

    }

    @Override
    public String getName() {
        return "nightcore";
    }
}
