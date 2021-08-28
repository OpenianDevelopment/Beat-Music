import DiscordClient from "./Client/Client";
import { registerCommands, registerEvents } from "./Utils/registry";

const client = new DiscordClient({
    intents: ["GUILDS", "GUILD_VOICE_STATES"],
});

(async () => {
    await registerCommands(client, "../commands");
    await registerEvents(client, "../events");
    await client.login(
        "ODgxMDUwMzEzODcwNjg0MTgw.YSnMCw.gaKxzDguU79QHATHjSS2dnrB3AI"
    );
})();
