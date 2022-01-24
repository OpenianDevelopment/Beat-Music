import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class PopCommand extends BaseCommand {
    constructor() {
        super("pop", "Apply POP filter");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.pop) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("POP Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("POP Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.pop = !player.pop;
    }
}
