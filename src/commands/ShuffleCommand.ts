import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class ShuffleCommand extends BaseCommand {
    constructor() {
        super("shuffle", "Shuffle the Queue");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("Queue Shuffled");
        interaction.editReply({ embeds: [embed] }).catch(console.error);
        player.queue.shuffle();
    }
}
