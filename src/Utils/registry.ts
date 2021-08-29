import DiscordClient from "../Client/Client";
import * as path from "path";
import { promises as fs } from "fs";
import { Manager } from "erela.js";
import { MessageEmbed, Snowflake, TextChannel } from "discord.js";
import filter from "erela.js-filters";
// @ts-ignore
import Spotify from "erela.js-spotify";
import { updateSongCount } from "./database/functions";

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
    const manager = new Manager({
        nodes: [
            {
                host: "localhost",
                port: 8000,
                password: "password",
            },
        ],
        plugins: [
            new filter(),
            new Spotify({
                clientID: "8bf6329ab57745c59de9daecb3a372d9",
                clientSecret: "799ebe01d9ff46689c4108da9f177b5d",
            }),
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
            if (!player.textChannel) return;
            const channel = client.channels?.cache.get(
                player.textChannel as Snowflake
            ) as TextChannel;
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setTitle("Now Playing")
                .setDescription(
                    `[${track.title}](${track.uri}) [${track.requester}]`
                )
                .setThumbnail(
                    track.thumbnail
                        ? track.thumbnail
                        : "https://i.imgur.com/FYBKE19.jpeg"
                );
            channel?.send({ embeds: [embed] });
            client.songCount++;
            updateSongCount(client.songCount);
        })
        .on("queueEnd", (player) => {
            if (!player.textChannel) return;
            const channel = client.channels?.cache.get(
                player.textChannel as Snowflake
            ) as TextChannel;
            const embed = new MessageEmbed()
                .setColor("#FFBD4F")
                .setDescription("Queue has Ended. Leaving the channel");
            channel?.send({ embeds: [embed] });
            player.destroy();
            client.players.delete(player.guild);
        })
        .on("trackStuck", (player, track) => {
            player.stop();
        })
        .on("playerMove", (player, oldChannel, newChannel) => {
            if (!newChannel) {
                player.destroy();
            } else {
                player.voiceChannel = newChannel;
            }
        });
    return manager;
}
