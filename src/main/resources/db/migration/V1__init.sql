-- Guild registration
CREATE TABLE guilds (
    id           BIGINT PRIMARY KEY,
    name         VARCHAR(100),
    joined_at    TIMESTAMPTZ DEFAULT NOW(),
    left_at      TIMESTAMPTZ,
    is_active    BOOLEAN DEFAULT TRUE,
    prefix_prefs JSONB DEFAULT '{}'
);

-- Play events — core telemetry + recommendation training data
CREATE TABLE play_events (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        BIGINT REFERENCES guilds(id),
    user_id         BIGINT NOT NULL,
    track_id        VARCHAR(255) NOT NULL,
    track_title     VARCHAR(500),
    track_author    VARCHAR(255),
    track_duration  INT,
    source          VARCHAR(50),
    played_at       TIMESTAMPTZ DEFAULT NOW(),
    skipped_at_ms   INT,
    filter_config   JSONB,
    requested_by    BIGINT
);

-- Queue snapshots for recovery on restart
CREATE TABLE queue_snapshots (
    guild_id     BIGINT PRIMARY KEY REFERENCES guilds(id),
    snapshot     JSONB NOT NULL,
    updated_at   TIMESTAMPTZ DEFAULT NOW()
);

-- User preferences
CREATE TABLE user_prefs (
    user_id           BIGINT PRIMARY KEY,
    default_vol       SMALLINT DEFAULT 100,
    preferred_filters JSONB DEFAULT '[]',
    updated_at        TIMESTAMPTZ DEFAULT NOW()
);

-- Command usage for dashboard analytics
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

-- Deduped track metadata cache
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

-- Indexes
CREATE INDEX idx_play_events_guild    ON play_events(guild_id, played_at DESC);
CREATE INDEX idx_play_events_user     ON play_events(user_id, played_at DESC);
CREATE INDEX idx_play_events_track    ON play_events(track_id);
CREATE INDEX idx_command_logs_guild   ON command_logs(guild_id, executed_at DESC);
CREATE INDEX idx_command_logs_command ON command_logs(command, executed_at DESC);
