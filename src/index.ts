import DiscordClient from "./Client/Client";
import { registerCommands, registerEvents } from "./Utils/registry";
require("dotenv").config;
const client = new DiscordClient({
    intents: ["GUILDS", "GUILD_VOICE_STATES"],
});

(async () => {
    await registerCommands(client, "../commands");
    await registerEvents(client, "../events");
    await client.login(process.env.TOKEN);
})();
