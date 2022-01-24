import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class TremoloCommand extends BaseCommand {
    constructor() {
        super("tremolo", "Apply Tremolo Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.tremolo) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Tremolo Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Tremolo Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.tremolo = !player.tremolo;
    }
}
