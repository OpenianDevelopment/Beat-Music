import { ApplicationCommandData, Client } from "discord.js";
const client = new Client({
    intents: [],
});
client.login("ODgxMDUwMzEzODcwNjg0MTgw.YSnMCw.gaKxzDguU79QHATHjSS2dnrB3AI");

const commands: ApplicationCommandData[] = [
    {
        name: "play",
        description: "Play a song from given query or url",
        options: [
            {
                name: "song",
                description: "Song name or Playlist url",
                type: "STRING",
                required: true,
            },
        ],
    },
];

client.on("ready", () => {
    commands.forEach((command) => {
        client
            .application!.commands.create(command)
            .then(console.log)
            .catch(console.error);
    });
});
