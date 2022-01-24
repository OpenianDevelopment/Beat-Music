import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";


export default class StopCommand extends BaseCommand {
    constructor() {
        super("stop", "Stops the player");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("Player Stopped");
        interaction.editReply({ embeds: [embed] }).catch(console.error);
        player.disconnect();
        player.destroy();
        client.players.delete(interaction.guildId);

    }
}
