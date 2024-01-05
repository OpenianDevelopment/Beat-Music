const { ActivityType, Client, GatewayIntentBits } = require("discord.js"); // Using Intents instead of GatewayIntentBits
const commandHandlers = require("./commands");
const { MoonlinkManager } = require("moonlink.js");

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.GuildVoiceStates,
    ],
}); // Using correct Intents syntax
client.moon = new MoonlinkManager(
    [
        {
            host: "localhost",
            port: 2333,
            secure: false,
            password: "youshallnotpass",
        },
    ],
    {},
    (guild, payload) => {
        client.guilds.cache.get(guild).shard.send(JSON.parse(payload));
    }
);
const activity = {
    name: "The Score",
    type: ActivityType.Listening,
};

const getCommandHandler = (commandName) => {
    return commandHandlers[commandName];
};

client.on("ready", () => {
    console.log("Beat Music is online");
    client.user.setActivity(activity);
    client.moon.init(client.user.id);
});

// Handle interactions using the getCommandHandler function
client.on("interactionCreate", async (interaction) => {
    if (!interaction.isChatInputCommand()) return;

    const commandHandler = getCommandHandler(interaction.commandName);
    if (commandHandler) {
        try {
            await commandHandler(interaction);
        } catch (error) {
            console.error(error);
            await interaction.reply({
                content: "An error occurred while executing the command.",
            });
        }
    }
});

// Event: Node created
client.moon.on("nodeCreate", (node) => {
    console.log(`${node.host} was connected, and the magic is in the air`);
});

client.on("raw", (data) => {
    // Updating the Moonlink.js package with the necessary data
    client.moon.packetUpdate(data);
});

client.login(process.env.TOKEN);
