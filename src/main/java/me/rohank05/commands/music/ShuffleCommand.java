package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Collections;
import java.util.Objects;

public class ShuffleCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;
        if (PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.queue.size() < 1) return;
        Collections.shuffle(PlayerManager.getINSTANCE().getGuildMusicManager(event.getGuild()).trackManager.queue);
        MessageEmbed embed = new EmbedBuilder().setColor(16760143).setDescription("Queue Shuffled").build();
        event.getInteraction().getHook().sendMessageEmbeds(embed).queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffles the queue";
    }
}
