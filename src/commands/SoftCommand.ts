import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class SoftCommand extends BaseCommand {
    constructor() {
        super("soft", "Apply Soft Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.soft) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Soft filter Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Soft Filter Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.soft = !player.soft;
    }
}
