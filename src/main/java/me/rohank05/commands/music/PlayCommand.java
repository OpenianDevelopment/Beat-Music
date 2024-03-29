package me.rohank05.commands.music;

import com.mongodb.DBObject;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.database.MongoDBMethod;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import static me.rohank05.Config.getDB;

import java.awt.*;
import java.net.URL;
import java.util.Objects;

public class PlayCommand implements ICommand {
    private final PlayerManager playerManager;
    public PlayCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getSelfMember().getVoiceState()).inAudioChannel()) {
            if (!event.getGuild().getAudioManager().isConnected()) {
                event.getGuild().getAudioManager().openAudioConnection(event.getGuild().getSelfMember().getVoiceState().getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            }
        } else {
            try {
                event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
                event.getGuild().getAudioManager().setSelfDeafened(true);
            } catch (Exception e) {
                MessageEmbed embed = new EmbedBuilder().setColor(Color.RED).setDescription(e.getMessage()).build();
                event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
                return;
            }
        }
        String track = Objects.requireNonNull(event.getOption("song")).getAsString();
        if (!isURL(track))
            track = "ytmsearch:" + track;
        playerManager.loadAndPlay(event, track);
        if(!playerManager.getGuildMusicManager(event.getGuild()).trackManager.hasTextChannel()){
            playerManager.getGuildMusicManager(event.getGuild()).trackManager.setTextChannel(event.getChannel().getId());
            if(getDB()) {
                DBObject guildSettings = MongoDBMethod.getGuildSettings(Objects.requireNonNull(event.getGuild()).getIdLong());
                if (guildSettings == null) return;
                Boolean tfs = (Boolean) guildSettings.get("24/7");
                playerManager.getGuildMusicManager(event.getGuild()).trackManager.setTfs(tfs);
            }
        }
    }

    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Play a song from given query or url";
    }

    private boolean isURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
