import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";

export default class RemoveCommand extends BaseCommand {
    constructor() {
        super("remove", "Remove a Song from the Queue");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const position = interaction.options.data[0].value as number;
        if (player.queue.length < position) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("❗ Song do not exist in the queue");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
            return;
        }
        const song = player.queue[position - 1];
        player.queue.remove(position);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(
                `[${song.title}](${song.uri}) removed from the queue`
            );
        interaction.editReply({ embeds: [embed] }).catch(console.error);
    }
}
