import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class SkipCommand extends BaseCommand {
    constructor() {
        super("skip", "Skip the current playing song");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(`\`${player.queue.current.title}\` Skipped`);
        player.stop();
        interaction.editReply({ embeds: [embed] }).catch(console.error);
    }
}
