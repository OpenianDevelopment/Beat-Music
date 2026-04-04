# PRD — Beat Bot (Discord Music Bot)

**Version:** 1.1 · **Status:** Draft · **Date:** April 2026

---

## 1. Overview

Beat Bot is a production-grade Discord music bot built with Kotlin + Spring Boot. It streams audio from YouTube and other sources via LavaPlayer 2.2.6 + youtube-source v2 (1.18.0), supports real-time audio filters, implements Discord's mandatory DAVE E2EE protocol via libdave-jvm, persists guild and playback data in PostgreSQL, and exposes a developer-only dashboard for growth and usage monitoring. All user interactions are slash commands only.

---

## 2. Goals

- Reliable, low-latency audio streaming in voice channels
- Full compliance with Discord's mandatory DAVE E2EE protocol (enforced March 1, 2026)
- Rich audio filter support (echo, reverb, tremolo, vibrato, etc.)
- Full play history and event tracking for recommendation model training
- Developer dashboard for guild growth, command usage, and anomaly detection
- Clean, extensible architecture that won't crumble under scale

---

## 3. Tech Stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Kotlin | Coroutines for async ops |
| Framework | Spring Boot 3.x | DI, REST, scheduling |
| Discord lib | JDA **6.3.2** | Required for DAVE support |
| DAVE impl | `libdave-jvm` + `adapter-jda` | JNI-based, Java 8+; recommended for production |
| Audio engine | `dev.arbjerg:lavaplayer:2.2.6` | Maven Central |
| YouTube source | `dev.lavalink.youtube:v2:1.18.0` | Replaces deprecated built-in YT |
| Database | PostgreSQL 16 | Primary data store |
| ORM | Spring Data JPA + Hibernate | Flyway for migrations |
| Cache | Redis | Queue state, session |
| Dashboard | Next.js 15 (separate service) | Dev-only, JWT-gated |
| Build | Gradle (Kotlin DSL) | |
| Deploy | Docker + Docker Compose | |

> **Why JDA 6.3.2?** Discord enforced DAVE (E2EE for all voice) on March 1, 2026. JDA 6.3.0+ is the minimum version that exposes the `DaveSessionFactory` interface required to connect to voice channels. Any older JDA version will fail to connect.

---

## 4. Dependency Configuration

```kotlin
// build.gradle.kts

repositories {
    mavenCentral()
    maven { url = uri("https://maven.lavalink.dev/releases") }
    maven { url = uri("https://maven.lavalink.dev/snapshots") } // libdave-jvm
    maven { url = uri("https://jitpack.io") }                   // jaadec-ext-aac, ibxm-fork
}

dependencies {
    // JDA 6.3.2 — required for DAVE support
    // Keep tink (transport encryption) and opus-java (encoding) — do NOT exclude them
    implementation("net.dv8tion:JDA:6.3.2")

    // DAVE E2EE — libdave-jvm (JNI bindings, Java 8+)
    // Use the JDA adapter which wires DaveSessionFactory automatically
    implementation("moe.kyokobot.libdave:impl-jni:VERSION")
    implementation("moe.kyokobot.libdave:adapter-jda:VERSION")

    // Platform-specific natives — include the one that matches your deploy target
    // For Docker on Linux x86-64 (most common):
    implementation("moe.kyokobot.libdave:natives-linux-x86-64:VERSION")
    // Add others as needed:
    // implementation("moe.kyokobot.libdave:natives-linux-aarch64:VERSION")
    // implementation("moe.kyokobot.libdave:natives-win-x86-64:VERSION")
    // implementation("moe.kyokobot.libdave:natives-macos-x86-64:VERSION")

    // LavaPlayer
    implementation("dev.arbjerg:lavaplayer:2.2.6")

    // youtube-source v2 (LavaPlayer 2.x, includes thumbnail support)
    implementation("dev.lavalink.youtube:v2:1.18.0")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // DB
    runtimeOnly("org.postgresql:postgresql")
}
```

> **Note:** Replace `VERSION` with the latest libdave-jvm release tag from [KyokoBot/libdave-jvm](https://github.com/KyokoBot/libdave-jvm). Published to `maven.lavalink.dev/snapshots` (not yet on Maven Central).

---

## 5. DAVE E2EE Integration

### 5.1 What is DAVE?

DAVE (Discord's Audio & Video End-to-End Encryption) is Discord's mandatory E2EE protocol for all voice connections, enforced since March 1, 2026. It uses:

- **MLS (Message Layer Security)** for group key exchange
- **Per-sender symmetric keys** — ratcheted and rotated on every participant join/leave
- **RTP frame-level encryption** — only the participants on the call can decrypt audio; Discord's own SFU cannot

Non-DAVE bots are rejected by Discord's voice gateway as of March 2026. Beat Bot must implement it to function at all.

### 5.2 JDA + libdave-jvm Wiring

JDA 6.3.0+ introduces `DaveSessionFactory` as an opt-in interface. `libdave-jvm` provides a JNI-based implementation backed by Discord's official C++ `libdave`. The `adapter-jda` module exposes a ready-to-use `DaveSessionFactory` implementation.

```kotlin
// config/JdaConfig.kt

@Configuration
class JdaConfig {

    @Bean
    fun jda(daveSessionFactory: DaveSessionFactory): JDA {
        return JDABuilder.createDefault(System.getenv("DISCORD_TOKEN"))
            .setAudioModuleConfig(
                AudioModuleConfig()
                    .withDaveSessionFactory(daveSessionFactory)
            )
            .addEventListeners(/* your listeners */)
            .build()
    }

    @Bean
    fun daveSessionFactory(): DaveSessionFactory {
        // libdave-jvm's adapter-jda provides LibDaveSessionFactory
        // Handles MLS group creation, epoch transitions, and per-frame encryption
        return LibDaveSessionFactory()
    }
}
```

### 5.3 How DAVE Interacts with LavaPlayer

DAVE operates at the **RTP frame level inside JDA's voice pipeline**. LavaPlayer sits above this layer — it produces Opus-encoded audio frames, passes them to JDA's `AudioSendHandler`, and JDA handles encryption before RTP transmission. No changes are needed in LavaPlayer itself.

```
LavaPlayer AudioPlayer
       ↓  Opus frames
AudioPlayerSendHandler  (implements AudioSendHandler)
       ↓  provide20MsAudio()
JDA AudioSendSystem
       ↓  encrypt via DAVE (libdave-jvm / libdave C++)
Discord SFU  (receives encrypted RTP — cannot decrypt)
       ↓  forwarded to listeners
Other call participants decrypt with per-sender MLS key
```

### 5.4 Key Rotation Events

DAVE rotates encryption keys on every participant change (join or leave). JDA handles the voice gateway opcode handshake (`dave_protocol_prepare_epoch`, opcode 24) internally with the provided `DaveSessionFactory`. No application-level code is required to handle epoch transitions — libdave-jvm manages MLS group state automatically.

### 5.5 Deployment Considerations

| Concern | Handling |
|---|---|
| Native library loading | libdave-jvm loads via JNI at startup; include the correct `natives-*` artifact for the target OS/arch |
| Docker base image | Use a glibc-based image (e.g. `eclipse-temurin:21-jre`), not Alpine (musl), unless you include the `natives-linux-musl-*` artifact |
| Java version | libdave-jvm requires Java 8+; we target Java 21 LTS |
| Multi-arch builds | Include multiple `natives-*` artifacts if deploying to both ARM and x86 nodes |

---

## 6. Slash Commands

### 6.1 Playback

| Command | Options | Description |
|---|---|---|
| `/play` | `query: String` | Search YouTube or pass URL; queues track |
| `/pause` | — | Pause current track |
| `/resume` | — | Resume paused track |
| `/stop` | — | Stop playback, clear queue |
| `/skip` | `amount: Int? = 1` | Skip N tracks |
| `/seek` | `position: String` (e.g. `1:30`) | Seek to timestamp |
| `/volume` | `level: Int (1–200)` | Set volume |
| `/nowplaying` | — | Embed with track info + thumbnail |
| `/loop` | `mode: [off, track, queue]` | Loop control |

### 6.2 Queue Management

| Command | Options | Description |
|---|---|---|
| `/queue` | `page: Int? = 1` | Paginated queue list |
| `/shuffle` | — | Shuffle queue |
| `/clear` | — | Clear queue (keep current) |
| `/remove` | `position: Int` | Remove track at position |
| `/move` | `from: Int, to: Int` | Reorder track |
| `/autoplay` | — | Toggle recommendation-based autoplay |

### 6.3 Filters

| Command | Options | Description |
|---|---|---|
| `/filter echo` | `delay: Float, decay: Float` | Echo effect |
| `/filter reverb` | `roomSize: Float, damping: Float` | Reverb/reverberation |
| `/filter tremolo` | `frequency: Float, depth: Float` | Amplitude oscillation |
| `/filter vibrato` | `frequency: Float, depth: Float` | Pitch oscillation |
| `/filter bassboost` | `gain: Float (-6 to +6 dB)` | Low-shelf EQ |
| `/filter nightcore` | — | Speed + pitch up preset |
| `/filter vaporwave` | — | Speed + pitch down preset |
| `/filter karaoke` | `level: Float, monoLevel: Float` | Vocal reduction |
| `/filter distortion` | `sinOffset, cosOffset, tanOffset` | Waveform distortion |
| `/filter rotation` | `hz: Float` | 8D audio rotation |
| `/filter reset` | — | Clear all filters |
| `/filter list` | — | Show active filters + values |

Filters are applied via LavaPlayer's `AudioPlayer.setFilterFactory()`. Presets (nightcore, vaporwave) are stored filter param bundles.

### 6.4 Source & Search

| Command | Options | Description |
|---|---|---|
| `/search` | `query: String, source: [yt, sc, bc]` | Interactive search results (5 options) |
| `/playlist load` | `url: String` | Load full playlist |
| `/recommend` | — | Suggest next tracks from rec model |

---

## 7. Audio Architecture

### 7.1 Player Manager Strategy

- One `DefaultAudioPlayerManager` instance (singleton Spring bean)
- One `AudioPlayer` per guild, stored in `GuildMusicManager`
- `GuildMusicManager` holds: `AudioPlayer`, `TrackScheduler` (queue), active filters
- On guild voice leave → player paused, cleanup scheduled after 5 min idle

### 7.2 Filter Pipeline

LavaPlayer 2.x exposes filter support via `AudioPlayer.setFilterFactory()`. Each filter wraps an `IFilteredAudioFrame` processor:

```kotlin
data class FilterConfig(
    val echo: EchoFilter? = null,
    val tremolo: TremoloFilter? = null,
    val vibrato: VibratoFilter? = null,
    val equalizer: EqualizerFilter? = null,
    val rotation: RotationFilter? = null,
    val distortion: DistortionFilter? = null,
    val karaoke: KaraokeFilter? = null,
    val timescale: TimescaleFilter? = null // nightcore / vaporwave
)
```

Filters are composed and applied in order. Active configs are persisted per guild in Redis for session recovery.

### 7.3 YouTube Source Clients (ordered by preference)

```
MUSIC_WITH_THUMBNAIL → ANDROID_VR_WITH_THUMBNAIL → WEB_WITH_THUMBNAIL → WEBEMBEDDED
```

OAuth2 refresh token and poToken are configured via environment variables and applied at startup. OAuth2 uses a **burner account**, not the developer's primary account.

### 7.4 YouTube Source Initialization

```kotlin
val playerManager = DefaultAudioPlayerManager()

// Register youtube-source v2 (with thumbnail support)
val ytSourceManager = dev.lavalink.youtube.YoutubeAudioSourceManager(
    /* allowSearch = */ true,
    MusicWithThumbnail(), WebWithThumbnail(), AndroidVrWithThumbnail()
)
playerManager.registerSourceManager(ytSourceManager)

// Register remaining remote sources, explicitly excluding built-in YT
AudioSourceManagers.registerRemoteSources(
    playerManager,
    com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager::class.java
)
```

---

## 8. PostgreSQL Schema

### Core Tables

```sql
-- Guild registration
CREATE TABLE guilds (
    id           BIGINT PRIMARY KEY,          -- Discord guild snowflake
    name         VARCHAR(100),
    joined_at    TIMESTAMPTZ DEFAULT NOW(),
    left_at      TIMESTAMPTZ,
    is_active    BOOLEAN DEFAULT TRUE,
    prefix_prefs JSONB DEFAULT '{}'
);

-- Play events (core telemetry + rec training)
CREATE TABLE play_events (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        BIGINT REFERENCES guilds(id),
    user_id         BIGINT NOT NULL,
    track_id        VARCHAR(255) NOT NULL,
    track_title     VARCHAR(500),
    track_author    VARCHAR(255),
    track_duration  INT,                         -- ms
    source          VARCHAR(50),
    played_at       TIMESTAMPTZ DEFAULT NOW(),
    skipped_at_ms   INT,                         -- null if played to end
    filter_config   JSONB,
    requested_by    BIGINT
);

-- Queue snapshots (for recovery)
CREATE TABLE queue_snapshots (
    guild_id     BIGINT PRIMARY KEY REFERENCES guilds(id),
    snapshot     JSONB NOT NULL,
    updated_at   TIMESTAMPTZ DEFAULT NOW()
);

-- User preferences
CREATE TABLE user_prefs (
    user_id      BIGINT PRIMARY KEY,
    default_vol  SMALLINT DEFAULT 100,
    preferred_filters JSONB DEFAULT '[]',
    updated_at   TIMESTAMPTZ DEFAULT NOW()
);

-- Command usage (for dashboard analytics)
CREATE TABLE command_logs (
    id           BIGSERIAL PRIMARY KEY,
    guild_id     BIGINT REFERENCES guilds(id),
    user_id      BIGINT,
    command      VARCHAR(100),
    args         JSONB,
    executed_at  TIMESTAMPTZ DEFAULT NOW(),
    success      BOOLEAN,
    error_msg    TEXT
);

-- Track catalogue (deduped metadata cache)
CREATE TABLE tracks (
    track_id     VARCHAR(255) PRIMARY KEY,
    title        VARCHAR(500),
    author       VARCHAR(255),
    duration_ms  INT,
    thumbnail    VARCHAR(512),
    source       VARCHAR(50),
    first_seen   TIMESTAMPTZ DEFAULT NOW(),
    play_count   BIGINT DEFAULT 0
);
```

### Indexes

```sql
CREATE INDEX idx_play_events_guild    ON play_events(guild_id, played_at DESC);
CREATE INDEX idx_play_events_user     ON play_events(user_id, played_at DESC);
CREATE INDEX idx_play_events_track    ON play_events(track_id);
CREATE INDEX idx_command_logs_guild   ON command_logs(guild_id, executed_at DESC);
CREATE INDEX idx_command_logs_command ON command_logs(command, executed_at DESC);
```

Migrations managed by **Flyway** (`db/migration/V1__init.sql`, etc.).

---

## 9. Recommendation System (Data Foundation)

The goal in v1 is to **collect the right data**, not ship a full ML model. The autoplay feature in v1 uses collaborative filtering (co-play frequency). v2 will introduce an embedding-based model.

### Signals Collected

| Signal | Table | Column |
|---|---|---|
| Track played (full) | `play_events` | `skipped_at_ms IS NULL` |
| Track skipped early | `play_events` | `skipped_at_ms < duration * 0.3` |
| Filters active at play time | `play_events` | `filter_config` |
| Co-plays within a session | `play_events` | `guild_id + 30-min window` |
| User re-requests same track | `play_events` | `user_id + track_id count` |

### v1 Autoplay Logic

```
1. Get last 10 played tracks in guild
2. Query play_events WHERE track_id IN (...) GROUP BY next_track_id
3. Score by: co-play count × recency weight
4. Return top 5 candidates → queue next
```

---

## 10. Developer Dashboard

### 10.1 Access

- Separate Next.js 15 App Router service
- Auth: JWT signed by bot backend, `DEVELOPER_USER_IDS` env whitelist
- Not exposed to regular users; bot API endpoint for dashboard is internal

### 10.2 Pages & Widgets

**Overview**
- Total guilds (active / all-time)
- New guilds last 7 / 30 days (line chart)
- Commands per hour (bar chart)
- Active voice sessions right now

**Guild Explorer**
- Search guild by ID or name
- Guild detail: joined date, top tracks, top users, daily play count
- Flag inactive guilds (>30 days no plays)

**Track Analytics**
- Top 50 most played tracks (global)
- Top tracks by guild
- Skip rate by track (high skip rate = bad recommendation candidate)
- Filter usage distribution

**Command Metrics**
- Command call count (by command, by day)
- Error rate per command
- P50/P95 response time (from `command_logs`)

**Recommendation Insights**
- Autoplay acceptance rate (did user skip the recommended track?)
- Co-play graph (top N track pairs)
- Model training data size over time

### 10.3 Dashboard API Endpoints (Spring Boot)

```
GET  /internal/dashboard/overview
GET  /internal/dashboard/guilds?page=&q=
GET  /internal/dashboard/guilds/{id}
GET  /internal/dashboard/tracks/top
GET  /internal/dashboard/commands/stats?from=&to=
GET  /internal/dashboard/recommendations/insights
```

All require `Authorization: Bearer <dev-jwt>` header.

---

## 11. Non-Functional Requirements

| Requirement | Target |
|---|---|
| Audio latency | < 200ms from command to audio start |
| Command response | < 500ms acknowledgment (deferred reply for heavy ops) |
| Concurrent guilds | 500+ with single instance (scale via sharding) |
| DAVE compliance | Mandatory — bot will not connect to voice without it (enforced March 2026) |
| Queue persistence | Redis TTL 6h; snapshot to Postgres on clean shutdown |
| YouTube reliability | Multi-client fallback (ANDROID_VR → WEB → WEBEMBEDDED) |
| Dashboard auth | JWT, developer allowlist, no public exposure |
| Data retention | `play_events` kept indefinitely for rec model; `command_logs` 90 days |

---

## 12. Project Structure

```
beat-bot/
├── src/main/kotlin/dev/beatbot/
│   ├── BeatBotApplication.kt
│   ├── config/
│   │   ├── LavaPlayerConfig.kt          # PlayerManager bean, YT source init
│   │   ├── JdaConfig.kt                 # JDA 6.3.2 + DAVE wiring + slash cmd reg
│   │   └── RedisConfig.kt
│   ├── audio/
│   │   ├── GuildMusicManager.kt
│   │   ├── TrackScheduler.kt
│   │   ├── AudioPlayerSendHandler.kt
│   │   └── filters/
│   │       ├── FilterConfig.kt
│   │       ├── EchoFilter.kt
│   │       ├── TremoloFilter.kt
│   │       ├── VibratoFilter.kt
│   │       ├── RotationFilter.kt
│   │       ├── DistortionFilter.kt
│   │       ├── TimescaleFilter.kt       # nightcore / vaporwave
│   │       └── FilterPresets.kt
│   ├── commands/
│   │   ├── PlayCommand.kt
│   │   ├── QueueCommand.kt
│   │   ├── FilterCommand.kt
│   │   ├── NowPlayingCommand.kt
│   │   └── RecommendCommand.kt
│   ├── recommendation/
│   │   ├── RecommendationService.kt
│   │   └── CoPlayRepository.kt
│   ├── entity/
│   │   ├── Guild.kt
│   │   ├── PlayEvent.kt
│   │   ├── CommandLog.kt
│   │   └── Track.kt
│   ├── repository/
│   │   ├── GuildRepository.kt
│   │   ├── PlayEventRepository.kt
│   │   └── CommandLogRepository.kt
│   └── dashboard/
│       ├── DashboardController.kt
│       └── DashboardAuthFilter.kt
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__init.sql
├── dashboard/                           # Next.js 15 app
│   ├── app/
│   └── package.json
├── docker-compose.yml
└── build.gradle.kts
```

---

## 13. Delivery Phases

| Phase | Scope | Duration |
|---|---|---|
| Phase 1 — Core Bot | JDA 6.3.2 + DAVE wiring, basic slash commands, YT + SC sources, PostgreSQL schema, Redis queue | 3 weeks |
| Phase 2 — Filters | All filter commands, preset system, filter state in Redis | 1.5 weeks |
| Phase 3 — Analytics | `play_events` + `command_logs` recording, autoplay v1 (co-play) | 1 week |
| Phase 4 — Dashboard | Next.js dashboard, all pages, JWT auth | 2 weeks |
| Phase 5 — Rec v2 | Embedding-based model trained on `play_events` | Future |

> **Phase 1 priority:** DAVE must be wired before any voice functionality is tested. Start with `JdaConfig.kt` → `LibDaveSessionFactory` → `AudioModuleConfig` → verify bot can join a voice channel. Everything else is blocked on this.
