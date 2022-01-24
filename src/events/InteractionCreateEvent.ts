import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { CommandInteraction, Interaction } from "discord.js";

export default class InteractionCreateEvent extends BaseEvent {
    constructor() {
        super("interactionCreate");
    }
    async run(client: DiscordClient, interaction: Interaction) {

        if (!interaction.inGuild()) return;
        if (!interaction.isCommand()) return;
        await interaction.deferReply().catch(console.error);
        const command = client.commands.get(interaction.commandName);
        if (command) {
            console.log(interaction);
            command.run(client, interaction as CommandInteraction);
        }
    }
}
