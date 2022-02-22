package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.net.URL;

public class PlayCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if(!CommandPermissionCheck.checkBasePermission(event)) return;
        if (event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            if (!event.getGuild().getAudioManager().isConnected()) {
                event.getGuild().getAudioManager().openAudioConnection(event.getGuild().getSelfMember().getVoiceState().getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
        } else {
            try {
                event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            } catch (Exception e) {
                MessageEmbed embed = new EmbedBuilder().setColor(Color.RED).setDescription(e.getMessage()).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }
        }
        String track = event.getOption("song").getAsString();
        if(!isURL(track))
            track = "ytmsearch:"+track;
        PlayerManager.getINSTANCE(event.getTextChannel()).loadAndPlay(event, track);
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return null;
    }

    private boolean isURL(String url){
        try {
            new URL(url).toURI();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}
