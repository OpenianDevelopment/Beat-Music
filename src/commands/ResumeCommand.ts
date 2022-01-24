import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class ResumeCommand extends BaseCommand {
    constructor() {
        super("resume", "Resume the player");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        if (player.paused) {
            player.pause(false);
            const embed = new MessageEmbed()
                .setColor("#ffbd4f")
                .setDescription("Player Resumed");
           interaction.editReply({ embeds: [embed] }).catch(console.error);
        } else {
            const embed = new MessageEmbed()
                .setColor("#ffbd4f")
                .setDescription("Player is not paused");
            interaction.editReply({ embeds: [embed] }).catch(console.error);
        }
    }
}
