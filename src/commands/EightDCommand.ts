import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class EightDCommand extends BaseCommand {
    constructor() {
        super("8d", "Apply 8D Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.eightD) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("8D filter Deactivated");
            await interaction.followUp({ embeds: [embed] });
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("8D Filter Activated");
            await interaction.followUp({ embeds: [embed] });
        }
        player.eightD = !player.eightD;
    }
}
