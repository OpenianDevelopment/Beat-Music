import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { Guild, WebhookClient } from "discord.js";

const webhook = new WebhookClient({
    url: process.env.WEDHOOK!,
});

export default class GuildDeleteEvent extends BaseEvent {
    constructor() {
        super("guildDelete");
    }
    async run(client: DiscordClient, guild: Guild) {
        await webhook.send({
            content: `**Guild Deleted**: ${guild.id} - ${guild.name}`,
        });
    }
}
