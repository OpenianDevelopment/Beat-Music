import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission, PageInteraction } from "../Utils/functions";
import { Queue } from "erela.js/structures/Queue";

export default class QueueCommand extends BaseCommand {
    constructor() {
        super("queue", "Get Queue List");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const embeds = generateQueueEmbed(interaction, player.queue);
        if (!embeds.length) {
            client.commands.get("now-playing")?.run(client, interaction);
            return;
        }
        if (embeds.length === 1) {
            interaction.editReply({ embeds: [embeds[0]] }).catch(console.error);
        } else {
            await PageInteraction(embeds, interaction);
        }
    }
}
function generateQueueEmbed(interaction: CommandInteraction, queue: Queue) {
    const embeds = [];
    let k = 10;
    let p = 1;
    for (let i = 0; i < queue.length; i += 10) {
        const currentQueue = queue.slice(i, k);
        let j = i;
        k += 10;
        const info = currentQueue
            .map(
                (track) =>
                    `${++j} - [${track.title}](${track.uri}) [${
                        track.requester
                    }]`
            )
            .join("\n");

        const embed = new MessageEmbed()
            .setTitle("Song Queue\n")
            .setThumbnail(interaction.guild!.iconURL({ dynamic: true }) || "")
            .setColor("#FFBD4F")
            .setDescription(
                `**Current Song** - [${queue.current?.title}](${queue.current?.uri}) [${queue.current?.requester}]\n\n${info}`
            )
            .setTimestamp()
            .setFooter(`Page: ${p}/${Math.ceil(queue.length / 10)}`);
        embeds.push(embed);
        p++;
    }
    return embeds;
}
