import DiscordClient from "../Client/Client";
import * as path from "path";
import { promises as fs } from "fs";
import { Manager } from "erela.js";
import { MessageEmbed, Snowflake, TextChannel, User, WebhookClient } from "discord.js";
import Filter from "erela.js-filters";
import Spotify from "erela.js-spotify";
import Deezer from "erela.js-deezer";
import { updateSongCount } from "./database/functions";

const player_log_webhook = new WebhookClient({url: process.env.PLAYER_LOG_WEBHOOK!})
export async function registerCommands(
    client: DiscordClient,
    dir: string = ""
) {
    const filePath = path.join(__dirname, dir);
    const files = await fs.readdir(filePath);
    for (const file of files) {
        const stat = await fs.lstat(path.join(filePath, file));
        if (stat.isDirectory())
            await registerCommands(client, path.join(dir, file));
        if (file.endsWith(".js") || file.endsWith(".ts")) {
            const { default: Command } = await import(path.join(dir, file));
            const command = new Command();
            client.commands.set(command.name, command);
        }
    }
}

export async function registerEvents(client: DiscordClient, dir: string = "") {
    const filePath = path.join(__dirname, dir);
    const files = await fs.readdir(filePath);
    for (const file of files) {
        const stat = await fs.lstat(path.join(filePath, file));
        if (stat.isDirectory())
            await registerEvents(client, path.join(dir, file));
        if (file.endsWith(".js") || file.endsWith(".ts")) {
            const { default: Event } = await import(path.join(dir, file));
            const event = new Event();
            client.events.set(event.getName(), event);
            client.on(event.getName(), event.run.bind(event, client));
        }
    }
}

export function initErela(client: DiscordClient) {
    return new Manager({
        nodes: [
            {
                host: "localhost",
                port: 8000,
                password: "password",
            },
        ],
        plugins: [
            new Filter(),
            new Spotify({
                clientID: process.env.SPOTIFY_ID!,
                clientSecret: process.env.SPOTIFY_SECRET!,
            }),
            new Deezer({}),
        ],
        send(id, payload) {
            const guild = client.guilds.cache.get(id as Snowflake);
            if (guild) guild.shard.send(payload);
        },
    })
        .on("nodeConnect", (node) => [
            console.log(`Node ${node.options.identifier} connected`),
        ])
        .on("nodeError", (node, error) => {
            console.log(
                `Node ${node.options.identifier} had an error: ${error.message}`
            );
        })
        .on("trackStart", (player, track) => {
            // @ts-ignore
            const user = track.requester as User;
            if (!player.textChannel) return;
            const channel = client.channels?.cache.get(
                player.textChannel as Snowflake
            ) as TextChannel;
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setTitle("Now Playing")
                .setDescription(`[${track.title}](${track.uri}) [${user}]`)
                .setThumbnail(
                    track.thumbnail
                        ? track.thumbnail
                        : "https://i.imgur.com/FYBKE19.jpeg"
                );
            if (!channel?.permissionsFor(client.user!)?.has("SEND_MESSAGES"))
                return;
            channel?.send({ embeds: [embed] }).catch((err) => {
                channel
                    .send({
                        content: `**Now Playing**: \`${track.title}\`\nPlease give me Embed Link Permission`,
                    })
                    .catch(console.log);
            });
            const playerLogEmbed = new MessageEmbed().setDescription(`**${user.tag}** started playing [${track.title}](${track.uri})`).setColor("#FFBD4F");
            player_log_webhook.send({embeds:[playerLogEmbed]}).catch(console.error)
            client.songCount++;
            updateSongCount(client.songCount).catch(console.error);
        })
        .on("queueEnd", (player) => {
            if (!player.textChannel) return;
            const channel = client.channels?.cache.get(
                player.textChannel as Snowflake
            ) as TextChannel;
            if (!channel?.permissionsFor(client.user!)?.has("SEND_MESSAGES"))
                return;
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Queue has Ended. Leaving the channel");
            channel?.send({ embeds: [embed] }).catch((err) => {
                console.log(err);
            });
            player.destroy();
            client.players.delete(player.guild);
        })
        .on("trackStuck", (player, track) => {
            player.stop();
        })
        .on("playerMove", (player, oldChannel, newChannel) => {
            if (!newChannel) {
                player.destroy();
                client.players.delete(player.guild);
            } else {
                player.voiceChannel = newChannel;
            }
        });

}
