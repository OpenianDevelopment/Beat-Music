import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { Guild, WebhookClient } from "discord.js";

const webhook = new WebhookClient({
    url: process.env.WEDHOOK!,
});

export default class GuildCreateEvent extends BaseEvent {
    constructor() {
        super("guildCreate");
    }
    async run(client: DiscordClient, guild: Guild) {
        await webhook.send({
            content: `**Guild Added**: ${guild.id} - ${guild.name}`,
        });
    }
}
