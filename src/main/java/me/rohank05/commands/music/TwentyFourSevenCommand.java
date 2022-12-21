package me.rohank05.commands.music;

import com.mongodb.DBObject;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.database.MongoDBMethod;
import me.rohank05.utilities.music.PlayerManager;
import me.rohank05.utilities.music.TrackManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.Objects;

import static me.rohank05.Config.getDB;

public class TwentyFourSevenCommand implements ICommand {
    private final PlayerManager playerManager;

    public TwentyFourSevenCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER)) {
            MessageEmbed embed = new EmbedBuilder().setColor(Color.RED).setDescription("You need `Manage Server` permission to use this command").build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            return;
        }
        if(getDB()){
            DBObject guildSettings = MongoDBMethod.getGuildSettings(Objects.requireNonNull(event.getGuild()).getIdLong());
            if (guildSettings == null) return;
            Boolean tfs = (Boolean) guildSettings.get("24/7");
            MongoDBMethod.set247(event.getGuild().getIdLong(), !tfs);

            String enabled = tfs ? "disabled" : "enabled";
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(16760143)
                    .setDescription("24/7 has been " + enabled)
                    .build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
            if (!this.playerManager.hasGuildMusicManager(event.getGuild())) return;
            TrackManager trackManager = this.playerManager.getGuildMusicManager(event.getGuild()).trackManager;
            trackManager.setTfs(!tfs);
        }else {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(16760143)
                    .setDescription("24/7 has been cannot be enable without mongodb installed")
                    .build();
            event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
        }
    }

    @Override
    public String getName() {
        return "247";
    }

    @Override
    public String getDescription() {
        return "Toggle 24/7 feature";
    }
}
