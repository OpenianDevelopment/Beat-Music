import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class BassCommand extends BaseCommand {
    constructor() {
        super("bass-boost", "Boost Bass of the song");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.bassboost) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("BassBoost Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("BassBoost Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.bassboost = !player.bassboost;
    }
}
