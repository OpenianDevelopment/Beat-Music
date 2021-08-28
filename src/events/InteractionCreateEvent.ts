import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { Interaction } from "discord.js";

export default class InteractionCreateEvent extends BaseEvent {
    constructor() {
        super("interactionCreate");
    }
    async run(client: DiscordClient, interaction: Interaction) {}
}
