package me.rohank05;

import me.rohank05.utilities.command.CommandManager;
import me.rohank05.utilities.database.MongoDBMethod;
import me.rohank05.utilities.music.PlayerManager;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class EventListeners extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(EventListeners.class);
    private PlayerManager playerManager;
    private CommandManager commandManager;

    //Events

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("{} is online", event.getJDA().getSelfUser().getAsTag());
        this.playerManager = new PlayerManager(event.getJDA());
        this.commandManager = new CommandManager(playerManager);
        event.getJDA().getGuilds().forEach(guild -> {
            if (MongoDBMethod.getGuildSettings(guild.getIdLong()) == null)
                MongoDBMethod.initGuildSettings(guild.getIdLong());
        });
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commandManager.run(event);
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember()))
            if (event.getChannelJoined().getMembers().size() > 1)
                if (this.playerManager != null)
                    if (this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.isPaused())
                        this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.setPaused(false);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        /*
          Check if Bot has left the channel. If it had it will destroy the player
         */
        if (!Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel())
            if (this.playerManager != null)
                if (this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null) {
                    this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.destroy();

                }

        /*
          Check if is the only member left in the channel. If yes then it will pause the song until another member join
         */
        if (event.getChannelLeft().getMembers().size() == 1)
            if (event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember()))
                if (this.playerManager != null){
                    this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.setPaused(true);
                    this.playerManager.getGuildMusicManager(event.getGuild()).audioPlayer.checkCleanup(300000L);
                }
    }


    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("play")) return;
        String value = event.getInteraction().getFocusedOption().getValue();
        Response httpResponse;
        try {
            httpResponse = getSearchSuggestion(value);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (!httpResponse.isSuccessful()) return;
        if (httpResponse.body() == null) return;
        DataObject responseBody;
        try {
            responseBody = DataObject.fromJson(httpResponse.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Optional<DataArray> optContents = responseBody.optArray("contents");
        if (optContents.isEmpty()) return;
        DataObject renderer = optContents.get().getObject(0).getObject("searchSuggestionsSectionRenderer");
        DataArray contents = renderer.optArray("contents").orElseGet(DataArray::empty);
        List<Command.Choice> results = new ArrayList<>();
        contents.stream(DataArray::getObject).forEach(content -> {
            String result = content.getObject("searchSuggestionRenderer").getObject("navigationEndpoint").getObject("searchEndpoint").getString("query");
            if (result.length() <= 100) {
                results.add(new Command.Choice(result, result));
            }
        });
        event.replyChoices(results).queue();

    }

    private Response getSearchSuggestion(String query) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\"input\":\"" + query + "\",\"context\":{\"client\":{\"deviceMake\":\"\",\"deviceModel\":\"\",\"userAgent\":\"Mozilla/5.0\",\"clientName\":\"WEB_REMIX\",\"clientVersion\":\"1.20220330.01.00\",\"osName\":\"Windows\",\"osVersion\":\"10.0\",\"originalUrl\":\"https://music.youtube.com/\"}}}");
        Request request = new Request.Builder()
                .url("https://music.youtube.com/youtubei/v1/music/get_search_suggestions")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Host", "music.youtube.com")
                .addHeader("Referer", "https://music.youtube.com")
                .build();
        return client.newCall(request).execute();

    }
}
