package me.rohank05.commands.music;

import me.rohank05.Bot;
import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.misc.SkipCounter;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class SkipCommand implements ICommand {
    private final PlayerManager playerManager;
    public SkipCommand(PlayerManager playerManager){
        this.playerManager = playerManager;
    }
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event, this.playerManager)) return;
        MessageEmbed embed = new EmbedBuilder().setTitle("Vote to skip").setColor(16760143).build();
        SkipCounter skipCounter = new SkipCounter(Bot.eventWaiter, event.getJDA(), Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getSelfMember().getVoiceState()).getChannel(), this.playerManager.getGuildMusicManager(event.getGuild()).trackManager);
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue(m->skipCounter.processSkip(m, Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()).getMembers().size()/2));
    }

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skip the current playing song";
    }
}
