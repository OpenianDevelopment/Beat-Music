package me.rohank05.commands.music;

import me.rohank05.utilities.command.ICommand;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PlaylistCommand implements ICommand {
    private final PlayerManager playerManager;
    private SlashCommandInteractionEvent event;

    public PlaylistCommand(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        this.event = event;
        String option = event.getSubcommandName();
        switch (option) {
            case "create":
                createPlaylist(event.getOption("name").getAsString());
                event.getHook().sendMessage("Playlist created").queue();
                break;
            case "play":
                playPlaylist(event.getOption("name").getAsString());
                break;
            case "add-song":
                addSong(event.getOption("playlist").getAsString(), event.getOption("song").getAsString());
                break;
            case "remove-song":
                removeSong(event.getOption("playlist").getAsString(), event.getOption("song-id").getAsInt());
                break;
            case "delete":
                deletePlaylist(event.getOption("name").getAsString());
        }
    }

    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public String getDescription() {
        return "Playlist features like `play`, `add`, `remove`, `create`";
    }

    private void createPlaylist(String name) {

    }

    private void playPlaylist(String name) {

    }

    private void addSong(String name, String url) {

    }

    private void removeSong(String name, int id) {

    }

    private void deletePlaylist(String name) {

    }
}
