import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class VaporwaveCommand extends BaseCommand {
    constructor() {
        super("vaporwave", "Apply Vaporwave Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.vaporwave) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Vaporwave filter Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Vaporwave Filter Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.vaporwave = !player.vaporwave;
    }
}
