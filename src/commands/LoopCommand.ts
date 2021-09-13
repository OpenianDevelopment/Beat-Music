import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class LoopCommand extends BaseCommand {
    constructor() {
        super("loop", "Loop the Queue");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.queueRepeat) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Loop is off");
            await interaction.followUp({ embeds: [embed] });
        } else {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Loop is on");
            await interaction.followUp({ embeds: [embed] });
        }
        player.setQueueRepeat(!player.queueRepeat);
    }
}
