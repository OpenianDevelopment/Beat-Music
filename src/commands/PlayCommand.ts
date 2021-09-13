import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, GuildMember, MessageEmbed } from "discord.js";

export default class PlayCommand extends BaseCommand {
    constructor() {
        super("play", "Play a Song");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        const member = interaction.member as GuildMember;
        const guild = interaction.guild!;
        const songQuery = interaction.options.data[0].value as string;
        const voiceChannel = member.voice.channel;
        if (!voiceChannel) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription("❗ You need to join a voice channel first");
            await interaction.followUp({ embeds: [embed], ephemeral: true });
            return;
        }
        if (
            client.players.has(guild.id) &&
            voiceChannel !== guild.me!.voice.channel
        ) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription("❗ You need to join the same channel as me");
            await interaction.followUp({ embeds: [embed], ephemeral: true });
            return;
        }
        const ClientPermissions = voiceChannel.permissionsFor(client.user!);
        if (
            (ClientPermissions && !ClientPermissions.has("CONNECT")) ||
            (ClientPermissions && !ClientPermissions.has("SPEAK")) ||
            (ClientPermissions && !ClientPermissions.has("VIEW_CHANNEL"))
        ) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription(
                    "❗ I don't have `CONNECT` or `SPEAK` or `VIEW CHANNEL` permission in that voice channel"
                );
            await interaction.followUp({ embeds: [embed], ephemeral: true });
            return;
        }

        const SpotifyPlaylistPattern =
            /^(?:https:\/\/open\.spotify\.com\/(?:user\/[A-Za-z0-9]+\/)?|spotify:)(playlist)[\/:]([A-Za-z0-9]+).*$/;
        const YTPlaylistPattern = /^.*(youtu.be\/|list=)([^#\&\?]*).*/gi;
        if (!client.players.has(guild.id)) {
            const player = client.manager.create({
                guild: guild.id,
                textChannel: interaction.channel!.id,
                voiceChannel: voiceChannel.id,
                selfDeafen: true,
            });
            client.players.set(guild.id, { player });
            player.connect();
        }
        const { player } = client.players.get(guild.id);
        if (!player.voiceChannel) {
            player.voiceChannel = voiceChannel;
        }
        const result = await client.manager.search(songQuery, member.user);
        if (!result.tracks.length) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("I am not able to find this song");
            await interaction.followUp({ embeds: [embed] });
            return;
        }

        if (
            YTPlaylistPattern.test(songQuery) ||
            SpotifyPlaylistPattern.test(songQuery)
        ) {
            player.queue.add(result.tracks);
            const embed = new MessageEmbed()
                .setDescription(
                    `Enqueuing \`${result.tracks.length}\` tracks. [<@${interaction.user.id}>]`
                )
                .setColor("#FFBD4F");
            await interaction.followUp({ embeds: [embed] });
        } else {
            player.queue.add(result.tracks[0]);
            const embed = new MessageEmbed()
                .setDescription(
                    `Enqueuing track [${result.tracks[0].title}](${result.tracks[0].uri}) [<@${interaction.user.id}>].`
                )
                .setColor("#FFBD4F");
            await interaction.followUp({ embeds: [embed] });
        }
        if (!guild.me!.voice.channel) {
            player.connect();
        }

        if (!player.playing && !player.paused) {
            player.play();
        }
    }
}
