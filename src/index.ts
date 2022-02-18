import DiscordClient from "./Client/Client";
import { registerCommands, registerEvents } from "./Utils/registry";
import { connect } from "mongoose";
import { setSongCount } from "./Utils/database/functions";
import { MessageEmbed, WebhookClient } from "discord.js";
require("dotenv").config();
const client = new DiscordClient({
    intents: ["GUILDS", "GUILD_VOICE_STATES"],
});
const err_log_webhook = new WebhookClient({
    url: process.env.ERROR_LOG_WEBHOOK!,
});

process.on("uncaughtException", function (error) {
    console.log("Uncaught Exception:", error);
    const errmsg = error.message;
    err_log_webhook.send({
        embeds: [
            new MessageEmbed().setDescription(
                "UNCAUGHT: " +
                    errmsg.slice(
                        0,
                        errmsg.length >= 800
                            ? 800 - errmsg.length
                            : errmsg.length
                    )
            ),
        ],
    });
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
