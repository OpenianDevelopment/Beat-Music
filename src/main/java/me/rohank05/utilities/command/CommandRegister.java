package me.rohank05.utilities.command;

import me.rohank05.EventListeners;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRegister extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListeners.class);

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("{} is online", event.getJDA().getSelfUser().getAsTag());
        event.getJDA().updateCommands().queue();
        CommandListUpdateAction cmds = event.getJDA().updateCommands();
        try {
            //noinspection ResultOfMethodCallIgnored
            cmds
                    .addCommands(
                            Commands.slash(
                                    "play",
                                    "Play a song from given query or url"
                                    ).addOption(
                                            OptionType.STRING,
                                            "song",
                                            "Song name or Playlist url",
                                            true
                            ),
                            Commands.slash(
                                    "pause",
                                    "Pause the music"
                            ),
                            Commands.slash(
                                    "stop",
                                    "Stop the queue and delete it"
                            ),
                            Commands.slash(
                                    "skip",
                                    "Skip the current playing song"
                            ),
                            Commands.slash(
                                    "queue",
                                    "Display the queue"
                            ),
                            Commands.slash(
                                    "remove",
                                    "Remove a song from the queue"
                            ).addOption(
                                    OptionType.INTEGER,
                                    "position",
                                    "position of the song which needs to be removed",
                                    true
                            ),
                            Commands.slash(
                                    "shuffle",
                                    "Shuffles the queue"
                            ),
                            Commands.slash(
                                    "loop",
                                    "loop the queue/track"
                            ).addSubcommands(new SubcommandData(
                                            "queue",
                                            "Loop the queue"
                                    ), new SubcommandData(
                                            "track",
                                            "Loop the current track"
                                    ), new SubcommandData(
                                            "off",
                                            "Switch off loop"
                                    )
                            ),
                            Commands.slash(
                                    "now-playing",
                                    "Show the current playing song"
                            ),
                            Commands.slash(
                                    "resume",
                                    "Resumes the paused song"
                            ),
                            Commands.slash(
                                    "leave",
                                    "Leaves the channel"
                            ),
                            Commands.slash(
                                    "volume",
                                    "change the volume of the player"
                            ).addOption(
                                    OptionType.INTEGER,
                                    "number",
                                    "how much volume you want",
                                    true
                            ),
                            Commands.slash(
                                    "bass-boost",
                                    "Boost the bass of the song"
                            ),
                            Commands.slash(
                                    "nightcore",
                                    "Add nightcore filter to the song"
                            ),
                            Commands.slash(
                                    "pop",
                                    "Add Pop filter to the song"
                            ),
                            Commands.slash(
                                    "soft",
                                    "Add soft filter to the song"
                            ),
                            Commands.slash(
                                    "echo",
                                    "Add Echo filter to the song"
                            ),
                            Commands.slash(
                                    "treblebass",
                                    "Add treblebass filter to the song"
                            ),
                            Commands.slash(
                                    "reset-filter",
                                    "Reset all filters"
                            ),
                            Commands.slash(
                                    "tremolo",
                                    "Add Tremolo filter to the song"
                            ),
                            Commands.slash(
                                    "vibrato",
                                    "Add Vibrato filter to the song"
                            ),
                            Commands.slash(
                                    "8d",
                                    "Add 8D filter to the song"
                            ),
                            Commands.slash(
                                    "autoplay",
                                    "Toggle autoplay song after queue end"
                            ),
                            Commands.slash(
                                    "247",
                                    "Toggle 24/7 feature"
                            )
                    );
        }catch (Exception exception){
            exception.printStackTrace();
            System.exit(1);
        }finally {
            cmds.queue((e)->System.exit(0));
        }
    }
}
