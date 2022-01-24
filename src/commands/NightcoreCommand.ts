import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class NightcoreCommand extends BaseCommand {
    constructor() {
        super("nightcore", "Apply nightcore filter");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.nightcore) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Nightcore Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Nightcore Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.nightcore = !player.nightcore;
    }
}
