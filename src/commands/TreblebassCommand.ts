import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class TreblebassCommand extends BaseCommand {
    constructor() {
        super("treblebass", "Apply Treble bass Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.treblebass) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Treble Bass Deactivated");
            await interaction.reply({ embeds: [embed] });
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Treble Bass Activated");
            await interaction.reply({ embeds: [embed] });
        }
        player.treblebass = !player.treblebass;
    }
}
