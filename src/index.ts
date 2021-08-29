import DiscordClient from "./Client/Client";
import { registerCommands, registerEvents } from "./Utils/registry";
import { connect } from "mongoose";
import { setSongCount } from "./Utils/database/functions";

const client = new DiscordClient({
    intents: ["GUILDS", "GUILD_VOICE_STATES"],
});

connect("mongodb://localhost:27017/Beat")
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
    await client.login(
        "ODgxMDUwMzEzODcwNjg0MTgw.YSnMCw.t59Th_FoJxPuaclvzi3q7age15I"
    );
})();
