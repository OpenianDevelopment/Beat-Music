# Beat Bot — Development Plan

**Last Updated:** April 4, 2026

---

## Phase 1 — Core Bot (3 weeks)

> **Priority:** DAVE wiring must be verified before anything else. The bot cannot join a voice channel without it.

### Milestone 1.1 — Project Scaffold ✅
- [x] Initialize Gradle project (Kotlin DSL)
- [x] Configure `build.gradle.kts` with all dependencies (JDA 6.3.2, libdave-jvm 0.1.2, LavaPlayer 2.2.6, youtube-source v2, Spring Boot, Postgres, Redis)
- [x] Set up repository layout (`com.therohankumar` package)
- [x] `application.yml` with env var placeholders (token, DB, Redis, OAuth2)
- [x] `docker-compose.yml` (bot + Postgres 16 + Redis), `Dockerfile` (eclipse-temurin:21-jre)
- [x] Flyway migration `V1__init.sql` (all 6 tables + indexes)

### Milestone 1.2 — DAVE + JDA Wiring ⚠️ ✅ (code complete)
- [x] `JdaConfig.kt` — `LibDaveSessionFactory` bean (`moe.kyokobot.libdave.jda`)
- [x] Wire `DaveSessionFactory` into `AudioModuleConfig` on JDA builder
- [ ] **TODO (runtime):** Verify bot can join a voice channel (smoke test in a dev server)
- [ ] **TODO (runtime):** Confirm DAVE handshake succeeds (opcode 24 / `dave_protocol_prepare_epoch`)

### Milestone 1.3 — LavaPlayer Setup ✅
- [x] `LavaPlayerConfig.kt` — singleton `DefaultAudioPlayerManager` bean
- [x] Register `YoutubeAudioSourceManager` (youtube-source v2) with client order:
  `MUSIC_WITH_THUMBNAIL → ANDROID_VR_WITH_THUMBNAIL → WEB_WITH_THUMBNAIL`
- [x] Register remaining remote sources (excluding deprecated built-in YT source)
- [x] `AudioPlayerSendHandler.kt` implementing `AudioSendHandler`
- [x] `GuildMusicManager.kt` — per-guild player + scheduler + idle disconnect
- [x] `TrackScheduler.kt` — queue logic, loop modes (OFF / TRACK / QUEUE)
- [x] Idle cleanup: disconnect after 5 min idle via `ScheduledExecutorService`
- [x] `MusicManagerRegistry.kt` — Spring singleton managing per-guild instances

### Milestone 1.4 — Slash Commands (Playback) ✅
- [x] Command registration on JDA ready event (guild-specific if `DISCORD_GUILD_ID` set, else global)
- [x] `/play` — YouTube search + URL; queue track; shows thumbnail + duration embed
- [x] `/pause`, `/resume`, `/stop`
- [x] `/skip` (optional `amount`)
- [x] `/seek` (timestamp string e.g. `1:30`)
- [x] `/volume` (1–200)
- [x] `/nowplaying` — embed with track title, author, duration, thumbnail, progress bar
- [x] `/loop` — off / track / queue mode toggle

### Milestone 1.5 — Queue Commands ✅
- [x] `/queue` — paginated embed (10 per page)
- [x] `/shuffle`, `/clear`, `/remove`, `/move`
- [x] `/autoplay` toggle (stub — recommendation logic wired in Phase 3)

### Milestone 1.6 — Persistence Foundation ✅
- [x] `Guild.kt`, `PlayEvent.kt`, `CommandLog.kt`, `Track.kt` JPA entities
- [x] Repositories: `GuildRepository`, `PlayEventRepository`, `CommandLogRepository`, `TrackRepository`
- [x] Auto-register guild on bot join event (`GuildEventListener`)
- [x] Mark guild inactive on bot leave event
- [x] Command logging on every slash command (via `SlashCommandListener`, virtual threads)
- [x] Redis config (`StringRedisTemplate` + `ObjectMapper`)
- [ ] **TODO (Phase 3):** Queue state serialization with 6h TTL + snapshot to Postgres on shutdown

---

## Phase 2 — Audio Filters (1.5 weeks)

### Milestone 2.1 — Filter Infrastructure
- [ ] `FilterConfig.kt` data class (all filter params nullable)
- [ ] `FilterConfig` serialization to/from JSON for Redis persistence
- [ ] Wire `AudioPlayer.setFilterFactory()` with composed filter chain
- [ ] Active filter config stored per-guild in Redis; restored on reconnect

### Milestone 2.2 — Individual Filters
- [ ] `EchoFilter.kt` — delay, decay
- [ ] `TremoloFilter.kt` — frequency, depth
- [ ] `VibratoFilter.kt` — frequency, depth
- [ ] `RotationFilter.kt` — hz (8D audio)
- [ ] `DistortionFilter.kt` — sinOffset, cosOffset, tanOffset
- [ ] `TimescaleFilter.kt` — speed + pitch (used for presets)
- [ ] Equalizer filter — low-shelf for bassboost (gain -6 to +6 dB)
- [ ] `KaraokeFilter.kt` — level, monoLevel

### Milestone 2.3 — Presets & Commands
- [ ] `FilterPresets.kt` — nightcore (speed+pitch up) and vaporwave (speed+pitch down) param bundles
- [ ] `/filter echo`, `/filter reverb`, `/filter tremolo`, `/filter vibrato`
- [ ] `/filter bassboost`, `/filter nightcore`, `/filter vaporwave`
- [ ] `/filter karaoke`, `/filter distortion`, `/filter rotation`
- [ ] `/filter reset` — clear all active filters
- [ ] `/filter list` — embed showing active filters + current values

---

## Phase 3 — Analytics & Autoplay (1 week)

### Milestone 3.1 — Event Recording
- [ ] Record `PlayEvent` on track start (guild, user, track metadata, source, filter config)
- [ ] Update `skipped_at_ms` on skip (populate if < 30% through = early skip signal)
- [ ] Record `CommandLog` entry on every slash command (success/failure, args, timing)
- [ ] Upsert `Track` catalogue entry on each play (dedup by track_id, increment play_count)

### Milestone 3.2 — Autoplay v1 (Co-Play)
- [ ] `RecommendationService.kt` — co-play frequency query
  ```
  1. Get last 10 played tracks in guild
  2. Query play_events WHERE track_id IN (...) GROUP BY next track in session
  3. Score by co-play count × recency weight
  4. Return top 5 candidates
  ```
- [ ] `CoPlayRepository.kt` — native SQL query for co-play graph
- [ ] Wire autoplay: when queue empties + autoplay enabled → fetch recommendation → enqueue
- [ ] `/recommend` command — show top 5 suggestions without auto-queuing
- [ ] `/search` command — interactive 5-option select menu (YT / SC / BC sources)

---

## Phase 4 — Developer Dashboard (2 weeks)

### Milestone 4.1 — Backend API
- [ ] `DashboardAuthFilter.kt` — JWT validation + `DEVELOPER_USER_IDS` env whitelist
- [ ] `DashboardController.kt`:
  - `GET /internal/dashboard/overview`
  - `GET /internal/dashboard/guilds?page=&q=`
  - `GET /internal/dashboard/guilds/{id}`
  - `GET /internal/dashboard/tracks/top`
  - `GET /internal/dashboard/commands/stats?from=&to=`
  - `GET /internal/dashboard/recommendations/insights`
- [ ] JWT signing endpoint for dev login (bot generates token, dashboard validates)

### Milestone 4.2 — Next.js Dashboard
- [ ] Initialize Next.js 15 App Router project under `dashboard/`
- [ ] JWT-gated auth (login via bot-issued token)
- [ ] **Overview page:** total guilds (active/all-time), new guilds 7/30d line chart, commands/hour bar chart, active voice sessions
- [ ] **Guild Explorer:** search by ID/name, guild detail view (joined date, top tracks, top users, daily play count), inactive guild flagging (>30 days no plays)
- [ ] **Track Analytics:** top 50 global, top by guild, skip rate by track, filter usage distribution
- [ ] **Command Metrics:** call counts by command/day, error rate, P50/P95 from command_logs
- [ ] **Recommendation Insights:** autoplay acceptance rate, co-play graph, training data size over time
- [ ] Wire into `docker-compose.yml` as separate service

---

## Phase 5 — Recommendation v2 (Future)

> Deferred until sufficient `play_events` data is collected (target: ~6 months post-launch).

- [ ] Embedding-based model trained on `play_events`
- [ ] Replace co-play frequency scoring in `RecommendationService`
- [ ] Model serving infrastructure (TBD)

---

## Cross-Cutting Concerns

These apply across all phases and should be addressed as each milestone is built:

- **Error handling:** All slash commands use deferred replies for ops > 200ms; surface user-friendly error embeds on failure
- **Logging:** SLF4J structured logs with guild_id + command context on every operation
- **Config:** All secrets via env vars; no hardcoded tokens or credentials
- **DAVE native libs:** Include `natives-linux-x86-64` for Docker; add `natives-linux-aarch64` if deploying to ARM nodes. Use `eclipse-temurin:21-jre` (glibc), not Alpine
- **YouTube OAuth2:** Use a burner account; configure refresh token + poToken via env vars at startup
- **Data retention:** `play_events` kept indefinitely; `command_logs` pruned after 90 days (scheduled job)

---

## Open Questions

- [ ] What libdave-jvm version to pin? Check [KyokoBot/libdave-jvm](https://github.com/KyokoBot/libdave-jvm) for latest release tag before starting Phase 1
- [ ] SoundCloud / Bandcamp source credentials needed for Phase 1 `/search`? (Can stub SC/BC and ship YT-only first)
- [ ] Dashboard hosting — same Docker Compose host or separate deployment?
- [ ] Sharding strategy for 500+ guilds — single instance first, add JDA sharding when needed?
