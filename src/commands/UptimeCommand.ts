import { CommandInteraction, GuildMember, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";

export default class UptimeCommand extends BaseCommand {
    constructor() {
        super("uptime", "Return bot's start time");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        const time = (Date.now() - client.uptime!).toString();
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(
                `**Shard:** ${
                    interaction.guild?.shardId || 0
                }\n**Started:** <t:${time.substring(0, time.length - 3)}:R>`
            )
            .setTimestamp();
        await interaction.followUp({
            embeds: [embed],
        });
        return;
    }
}
