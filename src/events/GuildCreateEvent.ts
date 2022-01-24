import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { Guild, MessageEmbed, WebhookClient } from "discord.js";
const guild_log_webhook = new WebhookClient({url: process.env.GUILD_LOG_WEBHOOK!})
export default class GuildCreateEvent extends BaseEvent{
    constructor() {
        super("guildCreate");
    }
    async run(client: DiscordClient, guild: Guild){
        const embed = new MessageEmbed().setColor("#FFBD4F").setDescription(`${guild.name} has added the bot`)
        guild_log_webhook.send({embeds:[embed]}).catch(console.error)
    }
}