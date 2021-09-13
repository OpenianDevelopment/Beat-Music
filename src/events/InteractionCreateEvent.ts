import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { CommandInteraction, Interaction } from "discord.js";

export default class InteractionCreateEvent extends BaseEvent {
    constructor() {
        super("interactionCreate");
    }
    async run(client: DiscordClient, interaction: Interaction) {
        if (!interaction.isCommand()) return;
        if (!interaction.inGuild()) return;
        const command = client.commands.get(interaction.commandName);
        if (command) {
            await interaction.deferReply();
            command.run(client, interaction as CommandInteraction);
        }
    }
}
