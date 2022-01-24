import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class VibratoCommand extends BaseCommand {
    constructor() {
        super("vibrato", "Apply Vibrato Filter");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.vibrato) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Vibrato Deactivated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Vibrato Activated");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
        player.vibrato = !player.vibrato;
    }
}
