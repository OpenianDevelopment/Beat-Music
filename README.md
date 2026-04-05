# Beat Music

A Discord music bot built with Kotlin, Spring Boot, and LavaPlayer.

## Requirements

- Java 21
- PostgreSQL (running)
- Redis (running)

## Environment Variables

Create a `.env` file in the project root:

```env
# Required
DISCORD_TOKEN=your_bot_token

# Database
DB_PASSWORD=beatbot

# Optional — leave blank to use guild-scoped commands (instant updates during dev)
DISCORD_GUILD_ID=your_dev_guild_id

# YouTube OAuth2 (recommended to avoid rate limits)
YT_REFRESH_TOKEN=
YT_PO_TOKEN=
```

## Development

```bash
./gradlew bootRun
```

## Deployment

### Build

```bash
./gradlew build
```

The JAR is output to `build/libs/beat-bot-0.0.1-SNAPSHOT.jar`.

### Run

```bash
java -jar build/libs/beat-bot-0.0.1-SNAPSHOT.jar
```

### Run in background (nohup)

```bash
nohup java -jar build/libs/beat-bot-0.0.1-SNAPSHOT.jar > bot.log 2>&1 &
echo $! > bot.pid
```

### View logs

```bash
tail -f bot.log
```

### Stop

```bash
kill $(cat bot.pid)
```

### Redeploy (pull + rebuild + restart)

```bash
git pull && ./gradlew build && kill $(cat bot.pid); nohup java -jar build/libs/beat-bot-0.0.1-SNAPSHOT.jar > bot.log 2>&1 & echo $! > bot.pid
```

### Run as a systemd service (Linux servers)

Create `/etc/systemd/system/beat-bot.service`:

```ini
[Unit]
Description=Beat Music Bot
After=network.target postgresql.service redis.service

[Service]
User=your_user
WorkingDirectory=/path/to/beat-bot
EnvironmentFile=/path/to/beat-bot/.env
ExecStart=/usr/bin/java -jar /path/to/beat-bot/build/libs/beat-bot-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Then:

```bash
sudo systemctl daemon-reload
sudo systemctl enable beat-bot
sudo systemctl start beat-bot
sudo systemctl status beat-bot
```

After a code change:

```bash
git pull && ./gradlew build && sudo systemctl restart beat-bot
```

## Commands

| Command | Description |
|---------|-------------|
| `/play <query>` | Play a song by name or URL. Supports autocomplete suggestions. |
| `/np` | Show the currently playing track. |
| `/queue` | Show the queue. |
| `/skip` | Skip the current track. |
| `/pause` / `/resume` | Pause or resume playback. |
| `/stop` | Stop playback and clear the queue. |
| `/loop` | Cycle loop modes: off → track → queue. |
| `/autoplay` | Toggle autoplay (co-play recommendations). |
| `/recommend` | Show top recommended tracks based on play history. |
| `/seek <time>` | Seek to a timestamp (e.g. `1:30`). |
| `/volume <0-200>` | Set the volume. |
| `/filters` | Open the filter panel (bass boost, reverb, etc.). |
| `/shuffle` | Shuffle the queue. |
| `/remove <position>` | Remove a track from the queue. |
| `/move <from> <to>` | Move a track in the queue. |
| `/clear` | Clear the queue. |
