import DiscordClient from "./Client/Client";
import { registerCommands, registerEvents } from "./Utils/registry";
import { connect } from "mongoose";
import { setSongCount } from "./Utils/database/functions";
require("dotenv").config();
const client = new DiscordClient({
    intents: ["GUILDS", "GUILD_VOICE_STATES"],
});

connect(process.env.MONGO_URL!)
    .then(() => {
        console.log("Connected to DB");
    })
    .catch((err) => {
        throw err;
    });

(async () => {
    await setSongCount(client);
    await registerCommands(client, "../commands");
    await registerEvents(client, "../events");
    await client.login(process.env.TOKEN);
})();
