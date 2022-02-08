package me.rohank05.beat.command.commands;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.GuildMusicManager;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Collections;

public class NightcoreCommand implements ICommand{
    @Override
    public void run(SlashCommandEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final Member member = event.getMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inVoiceChannel()) {
            MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        if (selfVoiceState.inVoiceChannel()) {
            if (!memberVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
                MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("You need to be in same channel as me").setColor(16760143).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }
        }
        GuildMusicManager manager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
        AudioPlayer player = manager.audioPlayer;
        player.setFilterFactory(((track, format, output) -> {
            TimescalePcmAudioFilter audioFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            audioFilter.setPitch(1.2999999523162842).setRate(1).setSpeed(1.2999999523162842);
            return Collections.singletonList(audioFilter);
        }));
        event.getInteraction().getHook().sendMessage("Added Nightcore filter").queue();
    }

    @Override
    public String getName() {
        return "nightcore";
    }
}
