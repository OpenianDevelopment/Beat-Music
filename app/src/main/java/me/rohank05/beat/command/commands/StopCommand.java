package me.rohank05.beat.command.commands;
import me.rohank05.beat.command.ICommand;
import me.rohank05.beat.lavaplayer.GuildMusicManager;
import me.rohank05.beat.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.EnumSet;

public class StopCommand implements ICommand{
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
        }else{
            MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("I need to be in voiceChannel for this to work").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getINSTANCE().getMusicManager(event.getGuild());
        musicManager.scheduler.player.stopTrack();
        musicManager.scheduler.queue.clear();
        MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("Player Stopped and Queue Cleared").setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "stop";
    }
}
