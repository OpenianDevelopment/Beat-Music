package me.rohank05;

import me.rohank05.utilities.command.CommandManager;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListeners extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListeners.class);
    private final CommandManager commandManager = new CommandManager();

    //Events

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("{} is online", event.getJDA().getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commandManager.run(event);
    }
}
