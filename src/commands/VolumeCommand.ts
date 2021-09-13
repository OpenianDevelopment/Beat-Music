import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class VolumeCommand extends BaseCommand {
    constructor() {
        super("volume", "Change Volume");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const volume = interaction.options.data[0].value as number;

        const { player } = client.players.get(interaction.guildId);
        player.setVolume(volume);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(`Volume set to ${volume}%`);
        await interaction.followUp({ embeds: [embed] });
    }
}
