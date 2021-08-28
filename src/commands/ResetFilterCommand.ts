import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class ResetFilterCommand extends BaseCommand {
    constructor() {
        super("reset-filter", "Reset Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        player.reset();
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("Filters Removed");
        await interaction.reply({ embeds: [embed] });
    }
}
