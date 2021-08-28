import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class BassCommand extends BaseCommand {
    constructor() {
        super("bassboost", "Boost Bass of the song");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.bassboost) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("BassBoost Deactivated");
            await interaction.reply({ embeds: [embed] });
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("BassBoost Activated");
            await interaction.reply({ embeds: [embed] });
        }
        player.bassboost = !player.bassboost;
    }
}
