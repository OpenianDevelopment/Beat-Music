package com.therohankumar.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import net.dv8tion.jda.api.interactions.commands.Command
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class YtMusicSuggestionService(private val objectMapper: ObjectMapper) {

    private val log = LoggerFactory.getLogger(YtMusicSuggestionService::class.java)

    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build()

    fun suggest(input: String): List<Command.Choice> {
        log.info("[Autocomplete] suggest() called with input='{}'", input)

        if (input.isBlank() || input.length < 2) {
            log.info("[Autocomplete] Skipping — input too short")
            return emptyList()
        }
        if (input.startsWith("http://") || input.startsWith("https://")) {
            log.info("[Autocomplete] Skipping — input is URL")
            return emptyList()
        }

        return try {
            val body = buildRequestBody(input)
            log.info("[Autocomplete] Sending request body: {}", String(body))

            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://music.youtube.com/youtubei/v1/music/get_search_suggestions?prettyPrint=false"))
                .timeout(Duration.ofSeconds(2))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .header("Content-Type", "application/json")
                .header("X-Youtube-Client-Name", "67")
                .header("X-Youtube-Client-Version", "1.20260331.01.00")
                .header("Origin", "https://music.youtube.com")
                .header("Referer", "https://music.youtube.com/")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .build()

            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            log.info("[Autocomplete] HTTP {} — body preview: {}", response.statusCode(), response.body().take(300))

            if (response.statusCode() != 200) {
                log.warn("[Autocomplete] Non-200 status: {}", response.statusCode())
                return emptyList()
            }

            val choices = parseChoices(objectMapper.readTree(response.body()))
            log.info("[Autocomplete] Returning {} choices", choices.size)
            choices
        } catch (e: Exception) {
            log.error("[Autocomplete] Exception for '{}': {}", input, e.message, e)
            emptyList()
        }
    }

    private fun buildRequestBody(input: String): ByteArray {
        val escapedInput = objectMapper.writeValueAsString(input)
        return """{"context":{"client":{"clientName":"WEB_REMIX","clientVersion":"1.20260331.01.00","hl":"en"}},"input":$escapedInput}""".toByteArray(Charsets.UTF_8)
    }

    private fun parseChoices(root: JsonNode): List<Command.Choice> {
        val choices = mutableListOf<Command.Choice>()

        for (section in root.path("contents")) {
            val contents = section.path("searchSuggestionsSectionRenderer").path("contents")
            for (item in contents) {
                if (choices.size >= 25) break
                when {
                    item.has("searchSuggestionRenderer") -> {
                        val suggestion = buildSuggestionText(item["searchSuggestionRenderer"]["suggestion"]["runs"])
                        if (suggestion.isNotBlank())
                            choices += Command.Choice(suggestion.take(100), suggestion.take(100))
                    }
                    item.has("musicResponsiveListItemRenderer") -> {
                        val renderer = item["musicResponsiveListItemRenderer"]
                        val videoId   = renderer.path("playlistItemData").path("videoId").asText(null) ?: continue
                        val title     = renderer.path("flexColumns").firstOrNull()
                            ?.path("musicResponsiveListItemFlexColumnRenderer")
                            ?.path("text")?.path("runs")?.firstOrNull()
                            ?.path("text")?.asText(null) ?: continue
                        val artist    = extractArtist(renderer.path("flexColumns"))
                        val label     = if (artist.isNotBlank()) "$title · $artist" else title
                        val url       = "https://www.youtube.com/watch?v=$videoId"
                        choices += Command.Choice(label.take(100), url.take(100))
                    }
                }
            }
        }
        return choices
    }

    private fun buildSuggestionText(runs: JsonNode): String =
        runs.joinToString("") { it.path("text").asText("") }

    private fun extractArtist(flexColumns: JsonNode): String {
        if (flexColumns.size() < 2) return ""
        val runs = flexColumns[1]
            .path("musicResponsiveListItemFlexColumnRenderer")
            .path("text").path("runs")
        // runs format: "Song", " • ", "Artist Name", " • ", "plays"
        // Artist is after the first " • " separator
        val texts = runs.map { it.path("text").asText("") }
        val sepIdx = texts.indexOf(" • ")
        return if (sepIdx >= 0 && sepIdx + 1 < texts.size) texts[sepIdx + 1] else ""
    }
}
