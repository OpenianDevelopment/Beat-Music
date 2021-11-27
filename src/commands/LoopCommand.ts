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
        const WhatToLoop = interaction.options.getSubcommand(true);
        switch (WhatToLoop) {
            case "queue": {
                const queuetxt = !player.queueRepeat
                    ? "Looping the queue"
                    : "Stopped looping the queue";
                const embed = new MessageEmbed()
                    .setColor("#FFBD4F")
                    .setDescription(queuetxt);
                await interaction.followUp({ embeds: [embed] });
                player.setQueueRepeat(!player.queueRepeat);
                break;
            }
            case "track": {
                const tracktxt = !player.trackRepeat
                    ? "Looping the current track"
                    : "Stopped looping the current track";
                const embed = new MessageEmbed()
                    .setColor("#FFBD4F")
                    .setDescription(tracktxt);
                await interaction.followUp({ embeds: [embed] });
                player.setTrackRepeat(!player.trackRepeat);
                break;
            }
        }
        return;
    }
}
