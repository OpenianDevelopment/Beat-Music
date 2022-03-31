import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import {
    CommandInteraction,
    GuildMember,
    Message,
    MessageEmbed,
} from "discord.js";

export default class PingCommand extends BaseCommand {
    constructor() {
        super("ping", "Returns Ping");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        const msg = (await interaction.followUp({
            content: `Pinging...`,
        })) as Message;
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(
                `**Shard:** ${
                    interaction.guild?.shardId || 0
                }\n**Socket:** ${client.ws.ping.toFixed(2)}ms\n**Latency:** ${(
                    msg.createdTimestamp - interaction.createdTimestamp
                ).toFixed(2)}ms`
            )
            .setTimestamp();
        await msg.edit({
            content: null,
            embeds: [embed],
        });
        return;
    }
}
