package me.rohank05.commands.music;

import me.rohank05.utilities.command.CommandPermissionCheck;
import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class LoopCommand implements ICommand {
    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!CommandPermissionCheck.checkBasePermission(event)) return;
        if (!CommandPermissionCheck.checkPermission(event)) return;
        String LoopSubcommand = Objects.requireNonNull(event.getSubcommandName()).toLowerCase();
        PlayerManager.getINSTANCE().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).trackManager.loop = LoopSubcommand;
        EmbedBuilder embedBuilder = new EmbedBuilder().setColor(16760143);
        switch (LoopSubcommand) {
            case "off" -> embedBuilder.setDescription("Loop Off!!");
            case "queue" -> embedBuilder.setDescription("Looping Queue!");
            case "track" -> embedBuilder.setDescription("Looping Track!");
        }
        event.getInteraction().getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public String getName() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "loop the queue/track";
    }
}
