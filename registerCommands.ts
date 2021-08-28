import { ApplicationCommandData, Client } from "discord.js";
require("dotenv").config();
const client = new Client({
    intents: [],
});
client.login(process.env.TOKEN);

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
    {
        name: "pause",
        description: "Pause the music",
    },
    {
        name: "stop",
        description: "Stop the queue and delete it",
    },
    {
        name: "skip",
        description: "Skip the current playing song",
    },
    {
        name: "queue",
        description: "Display the queue",
    },
    {
        name: "remove",
        description: "Remove a song from the queue",
        options: [
            {
                name: "position",
                description: "postion of the song which needs to be removed",
                type: "INTEGER",
                required: true,
            },
        ],
    },
    {
        name: "shuffle",
        description: "Shuffles the queue",
    },
    {
        name: "loop",
        description: "loop the queue",
    },
    {
        name: "now-playing",
        description: "Show the current playing song",
    },
    {
        name: "resume",
        description: "Resumes the paused song",
    },
    {
        name: "leave",
        description: "Leaves the channel",
    },
    {
        name: "volume",
        description: "change the volume of the palyer",
        options: [
            {
                name: "number",
                description: "how much volume you want",
                type: "INTEGER",
                required: true,
            },
        ],
    },
    {
        name: "bass-boost",
        description: "Boost the bass of the song",
    },
    {
        name: "nightcore",
        description: "Add nightcore filter to the song",
    },
    {
        name: "pop",
        description: "Add Pop filter to the song",
    },
    {
        name: "soft",
        description: "Add soft filter to the song",
    },
    {
        name: "vaporwave",
        description: "Add vaporwave filter the the song",
    },
    {
        name: "treblebass",
        description: "Add treblebass filter to the song",
    },
    {
        name: "help",
        description: "Get List of All Commands",
    },
    {
        name: "reset-filter",
        description: "Reset all filters",
    },
    {
        name: "help",
        description: "list all commands",
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
