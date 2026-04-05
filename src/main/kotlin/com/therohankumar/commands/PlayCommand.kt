package com.therohankumar.commands

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.therohankumar.audio.MusicManagerRegistry
import com.therohankumar.audio.SearchResultStore
import com.therohankumar.commands.util.CommandUtil
import com.therohankumar.service.YtMusicSuggestionService
import net.dv8tion.jda.api.components.label.Label
import net.dv8tion.jda.api.components.radiogroup.RadioGroup
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.modals.Modal
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Component
class PlayCommand(
    private val registry: MusicManagerRegistry,
    private val playerManager: AudioPlayerManager,
    private val searchResultStore: SearchResultStore,
    private val suggestionService: YtMusicSuggestionService
) : SlashCommand {

    private val log = LoggerFactory.getLogger(PlayCommand::class.java)

    override val name = "play"

    override fun build(): SlashCommandData = Commands.slash(name, "Play a song from YouTube or a URL")
        .addOptions(OptionData(OptionType.STRING, "query", "Song name or URL", true)
            .setAutoComplete(true))

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        log.info("[Autocomplete] onAutoComplete fired — focused='{}' value='{}'",
            event.focusedOption.name, event.focusedOption.value)
        if (event.focusedOption.name != "query") return
        val input = event.focusedOption.value
        // Run on virtual thread — don't block the JDA event dispatch thread
        Thread.ofVirtual().start {
            val choices = suggestionService.suggest(input)
            event.replyChoices(choices).queue(
                { log.info("[Autocomplete] Replied with {} choices", choices.size) },
                { err -> log.error("[Autocomplete] replyChoices failed: {}", err.message) }
            )
        }
    }

    override fun execute(event: SlashCommandInteractionEvent) {
        val query = event.getOption("query")!!.asString
        val manager = CommandUtil.requireVoice(event, registry) ?: return
        val isUrl = query.startsWith("http://") || query.startsWith("https://")

        if (isUrl) {
            // Direct URL — no picker needed, play immediately
            event.deferReply().queue()
            playerManager.loadItemOrdered(manager, query, directHandler(event, manager, event.user.idLong))
            return
        }

        // Search — block for up to 2.5s then show modal with top 5 results
        val future = CompletableFuture<List<AudioTrack>>()
        playerManager.loadItemOrdered(manager, "ytmsearch:$query", object : AudioLoadResultHandler {
            override fun playlistLoaded(playlist: AudioPlaylist) {
                future.complete(playlist.tracks.take(5))
            }
            override fun trackLoaded(track: AudioTrack) { future.complete(listOf(track)) }
            override fun noMatches() { future.complete(emptyList()) }
            override fun loadFailed(e: FriendlyException) { future.completeExceptionally(e) }
        })

        val tracks = try {
            future.get(2500, TimeUnit.MILLISECONDS)
        } catch (e: TimeoutException) {
            event.deferReply().queue()
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("Search timed out. Try again.")).queue()
            return
        } catch (e: Exception) {
            event.deferReply().queue()
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("Failed to search: ${e.message}")).queue()
            return
        }

        if (tracks.isEmpty()) {
            event.deferReply().queue()
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("No results found for: `$query`")).queue()
            return
        }

        if (tracks.size == 1) {
            // Only one result — play directly without picker
            event.deferReply().queue()
            val track = tracks[0].also { it.userData = event.user.idLong }
            manager.scheduler.queue(track)
            event.hook.editOriginalEmbeds(buildNowPlayingEmbed(track, manager)).queue()
            return
        }

        // Tag each track with the requesting user ID
        tracks.forEach { it.userData = event.user.idLong }

        // Store results so the modal handler can retrieve them
        searchResultStore.put(event.user.idLong, tracks)

        val radioGroup = RadioGroup.create("track_select")
        tracks.forEachIndexed { i, track ->
            val title = track.info.title.take(97).let { if (it.length < track.info.title.length) "$it…" else it }
            val desc  = "${track.info.author} · ${CommandUtil.formatDuration(track.duration)}"
            radioGroup.addOption(title, i.toString(), desc)
        }
        radioGroup.setSelectedValue("0") // default to first option

        val modal = Modal.create("play:${event.guild!!.idLong}:${event.user.idLong}", "Results for: ${query.take(30)}")
            .addComponents(Label.of("Choose a track", radioGroup.build()))
            .build()

        event.replyModal(modal).queue()
    }

    /** Handler for direct URL playback (no picker). */
    private fun directHandler(
        event: SlashCommandInteractionEvent,
        manager: com.therohankumar.audio.GuildMusicManager,
        userId: Long
    ) = object : AudioLoadResultHandler {

        override fun trackLoaded(track: AudioTrack) {
            track.userData = userId
            manager.scheduler.queue(track)
            event.hook.editOriginalEmbeds(buildNowPlayingEmbed(track, manager)).queue()
        }

        override fun playlistLoaded(playlist: AudioPlaylist) {
            val track = (playlist.selectedTrack ?: playlist.tracks.firstOrNull()) ?: run {
                event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("No tracks found.")).queue()
                return
            }
            track.userData = userId
            manager.scheduler.queue(track)
            event.hook.editOriginalEmbeds(buildNowPlayingEmbed(track, manager)).queue()
        }

        override fun noMatches() {
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("Nothing found at that URL.")).queue()
        }

        override fun loadFailed(exception: FriendlyException) {
            event.hook.editOriginalEmbeds(CommandUtil.errorEmbed("Failed to load: ${exception.message}")).queue()
        }
    }

    fun buildNowPlayingEmbed(track: AudioTrack, manager: com.therohankumar.audio.GuildMusicManager) =
        CommandUtil.nowPlayingEmbed(track, manager, queued = manager.player.playingTrack != track)
}
