import { CommandInteraction, MessageEmbed } from "discord.js";
import DiscordClient from "../Client/Client";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { checkMusicPermission } from "../Utils/functions";
import { splitBar } from "string-progressbar";
export default class NowPlayingCommand extends BaseCommand {
    constructor() {
        super("now-playing", "Shows the current playing song");
    }

    async run(client: DiscordClient, interaction: CommandInteraction) {
        if (!(await checkMusicPermission(client, interaction))) return;
        const { player } = client.players.get(interaction.guildId);
        const song = player.queue.current;
        const seek = player.position;
        const left = song.duration - seek;

        let embed = new MessageEmbed()
            .setTitle("Now playing")
            .setDescription(`[${song.title}](${song.url}) [${song.requester}]`)
            .setColor("#554b58")
            .setThumbnail(song.thumbnail);

        if (song.duration > 0) {
            embed.addField(
                "\u200b",
                new Date(seek).toISOString().substr(11, 8) +
                    "[" +
                    splitBar(
                        song.duration == 0 ? seek : song.duration,
                        seek,
                        20
                    )[0] +
                    "]" +
                    (song.duration == 0
                        ? " ◉ LIVE"
                        : new Date(song.duration).toISOString().substr(11, 8)),
                false
            );
            embed.setFooter(
                "Time Remaining: " + new Date(left).toISOString().substr(11, 8)
            );
        }
        await interaction.reply({ embeds: [embed] });
    }
}
