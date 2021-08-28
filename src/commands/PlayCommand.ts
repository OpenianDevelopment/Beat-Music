import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, GuildMember, MessageEmbed } from "discord.js";

export default class PlayCommand extends BaseCommand {
    constructor() {
        super("play", "Play a Song", "play <song name>", "play wonderwall");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        const member = interaction.member as GuildMember;
        const guild = interaction.guild!;
        const songQuery = interaction.options.data[0].value as string;
        const voiceChannel = member.voice.channel;
        if (!voiceChannel) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription("You need to join a voice channel first");
            await interaction.reply({ embeds: [embed] });
            return;
        }
        if (
            client.players.has(guild.id) &&
            voiceChannel !== guild.me!.voice.channel
        ) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription("You need to join the same channel as me");
            await interaction.reply({ embeds: [embed] });
            return;
        }
        const ClientPermissions = voiceChannel.permissionsFor(client.user);
        if (
            !ClientPermissions.has("CONNECT") ||
            !ClientPermissions.has("SPEAK")
        ) {
            const embed = new MessageEmbed()
                .setColor("#FFDB4F")
                .setDescription(
                    "I don't have CONNECT and SPEAK permission in that voice channel"
                );
            await interaction.reply({ embeds: [embed] });
            return;
        }
        const SpotifyTrackPattern =
            /^(?:https:\/\/open\.spotify\.com\/(?:user\/[A-Za-z0-9]+\/)?|spotify:)(track)[\/:]([A-Za-z0-9]+).*$/;
        const SpotifyPlaylistPattern =
            /^(?:https:\/\/open\.spotify\.com\/(?:user\/[A-Za-z0-9]+\/)?|spotify:)(playlist)[\/:]([A-Za-z0-9]+).*$/;
        if (client.players.has(guild.id)) {
            const player = client.manager.create({
                guild: guild.id,
                textChannel: interaction.channel!.id,
                voiceChannel: voiceChannel.id,
            });
            client.players.set(guild.id, { player });
            player.connect();
        }
        const { player } = client.players.get(guild.id);
        // if (SpotifyTrackPattern.test(songQuery)) {
        //     await PlaySpotifyTrack(client, songQuery, interaction);
        //     return;
        // } else if (SpotifyPlaylistPattern.test(songQuery)) {
        //     await GetSpotifyPlaylistTracks(client, songQuery, interaction);
        //     return;
        // }

        const result = await client.manager.search(songQuery, member.user);
        if (!result.tracks.length) {
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("I am not able to find this song");
            await interaction.reply({ embeds: [embed] });
            return;
        }
        player.queue.add(result.tracks[0]);
        if (!guild.me!.voice.channel) {
            player.connect();
        }
        if (!player.playing && !player.paused && !player.queue.size) {
            player.play();
        } else {
            const embed = new MessageEmbed()
                .setDescription(
                    `Enqueuing track \`${result.tracks[0].title}\`.`
                )
                .setColor("#FFBD4F");
            await interaction.reply({ embeds: [embed] });
        }
    }
}
