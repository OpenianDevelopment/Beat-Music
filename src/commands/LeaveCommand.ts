import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class LeaveCommand extends BaseCommand {
    constructor() {
        super("leave", "Leave the channel");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        player.disconnect();
        player.destroy();
        client.players.delete(interaction.guildId);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("Leaving VC");
        await interaction.reply({ embeds: [embed] });
    }
}
