package me.rohank05.beat.command.commands;

import me.rohank05.beat.command.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class JoinCommand implements ICommand {
    @Override
    public void run(SlashCommandEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final Member member = event.getMember();
        final GuildVoiceState voiceState = self.getVoiceState();
        final GuildVoiceState memberState = member.getVoiceState();

        if(voiceState.inVoiceChannel()){
            MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("I am already in a Voice Channel").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        if(!memberState.inVoiceChannel()){
            MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("You need to join a voice channel first").setColor(16760143).build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        VoiceChannel memberChannel = memberState.getChannel();
        audioManager.openAudioConnection(memberChannel);
        MessageEmbed embed = new EmbedBuilder().setAuthor(self.getNickname(), self.getEffectiveAvatarUrl()).setDescription("Connecting to \uD83D\uDD0A"+ memberChannel.getName()).setColor(16760143).build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "join";
    }
}
