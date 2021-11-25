import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import {
    CommandInteraction,
    MessageActionRow,
    MessageButton,
    MessageEmbed,
    MessageAttachment,
    Interaction,
    MessageSelectMenu,
} from "discord.js";
import { inspect } from "util";
import os from "os";
import { readdir } from "fs/promises";

export default class HelpCommand extends BaseCommand {
    constructor() {
        super("su", "Developer stuff");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        const embed = new MessageEmbed().setColor("#FFBD4F");
        if (
            ![
                "534783899331461123",
                "687893451534106669",
                "180485886184521728",
            ].includes(interaction.user.id)
        )
            return;
        const cmd = interaction.options.getString("cmd", true);
        switch (cmd) {
            case "eval": {
                if (!interaction.options.getString("code", false)) {
                    await interaction.reply({
                        embeds: [
                            embed.setDescription(
                                "You need the 'code' argument to use eval"
                            ),
                        ],
                    });
                    return;
                }
                await Evaluate(client, interaction);
                break;
            }
            case "info": {
                await Dev_info(client, interaction);
                break;
            }
            case "reload": {
                await Reload(client, interaction);
                break;
            }
        }
        return;
    }
}

async function Evaluate(
    client: DiscordClient,
    interaction: CommandInteraction
) {
    const date = Date.now();
    const text = (text: string) => {
        interaction.channel!.send({ content: text });
    };
    const channel = interaction.channel,
        lmsg = interaction.channel?.lastMessage;
    const code = interaction.options.data[1].value!.toString();
    const XBtn = new MessageActionRow().addComponents(
        new MessageButton()
            .setCustomId("delete-" + date)
            .setStyle("PRIMARY")
            .setEmoji("❌")
    );
    try {
        const start = process.hrtime();
        var evaled = eval(code.replace(/interaction.reply/g, "channel.send"));
        if (evaled instanceof Promise) {
            evaled = await evaled;
        }
        const stop = process.hrtime(start);
        const evmbed = new MessageEmbed()
            .setColor("#00FF00")
            .setFooter(
                `Time Taken: ${(stop[0] * 1e9 + stop[1]) / 1e6}ms`,
                client!.user!.displayAvatarURL()
            )
            .setTitle("Eval")
            .addField(
                `**Output:**`,
                `\`\`\`js\n${clean(inspect(evaled, { depth: 0 }))}\n\`\`\``
            )
            .addField(`**Type:**`, typeof evaled);

        const response = clean(inspect(evaled, { depth: 0 }));
        if (response.length < 1900) {
            await interaction.reply({
                embeds: [evmbed],
                components: [XBtn],
            });
        } else if (response.length <= 2000) {
            await interaction.reply({
                content: "```js\n" + response + "\n```",
                components: [XBtn],
            });
        } else {
            const output = new MessageAttachment(
                Buffer.from(response),
                "output.txt"
            );
            await interaction.user!.send({ files: [output] });
        }
    } catch (err: any) {
        const errevmbed = new MessageEmbed()
            .setColor("#FF0000")
            .setTitle(`ERROR`)
            .setDescription(`\`\`\`xl\n${clean(err.toString())}\n\`\`\``)
            .setTimestamp()
            .setFooter(
                client!.user!.username,
                client!.user!.displayAvatarURL()
            );
        await interaction.reply({
            embeds: [errevmbed],
            components: [XBtn],
        });
    }

    const filter = (m: Interaction) => interaction.user.id === m.user.id;
    const collector = interaction.channel!.createMessageComponentCollector({
        filter,
    });
    collector.on("collect", async (int) => {
        if (int.customId === "delete-" + date) {
            interaction.deleteReply();
        }
    });
    function clean(text: string) {
        text = text
            .replace(/`/g, `\\\`${String.fromCharCode(8203)}`)
            .replace(/@/g, `@${String.fromCharCode(8203)}`)
            .replace(
                new RegExp(client!.token!, "gi"),
                `NrzaMyOTI4MnU1NT3oDA1rTk4.pPizb1g.hELpb6PAi1Pewp3wAwVseI72Eo`
            );
        return text;
    }
}

async function Dev_info(
    client: DiscordClient,
    interaction: CommandInteraction
) {
    var size = client.guilds.cache.size;

    const embed = new MessageEmbed().setColor("AQUA")
        .setDescription(`**Bot stats**\n
Guilds size: **${size}**
Users size: **${client.users.cache.size}**
CPU: **${os.cpus()[0].model}**
Ram Usage in GB: **${(
        process.memoryUsage().heapUsed /
        1024 /
        1024 /
        1024
    ).toFixed(2)} GB**
Ram Usage in MB: **${(process.memoryUsage().heapUsed / 1024 / 1024).toFixed(
        2
    )} MB**
Total ram: **${(os.totalmem() / 1024 / 1024 / 1024).toFixed(2)} GB**`);

    interaction.reply({ embeds: [embed] });
    return;
}

async function Reload(client: DiscordClient, interaction: CommandInteraction) {
    const date = Date.now();
    const commandMenu = new MessageSelectMenu()
        .setCustomId("select_file_tr-" + date)
        .setPlaceholder("Nothing Selected")
        .addOptions({
            value: "all",
            label: "All",
            description: "Reload all commands",
        })
        .setMinValues(1);
    client.commands
        .map((a) => a)
        .forEach((cmd) => {
            commandMenu.addOptions({
                label: cmd.name,
                value: cmd.name,
                description: cmd.description,
            });
        });

    const commandsRow = new MessageActionRow().addComponents(commandMenu);
    const embed = new MessageEmbed()
        .setColor("DARK_RED")
        .setDescription("Choose the File(s) you want to reload");
    await interaction.reply({
        embeds: [embed],
        components: [commandsRow],
    });
    const filter = (m: Interaction) => interaction.user.id === m.user.id;
    const collector = interaction.channel!.createMessageComponentCollector({
        filter,
        time: 15e3,
    });
    collector.on("collect", async (int) => {
        if (int.isSelectMenu() && int.customId === "select_file_tr-" + date) {
            var files = int.values.map((v) => MCFCD(v));
            if (int.values.includes("all")) {
                files = await readdir("./");
            }
            for (const f of files) {
                delete require.cache[require.resolve(`./${f}`)];
                const { default: Command } = await import(`./${f}`);
                const command = new Command();
                client.commands.set(command.name, command);
            }
            const embed2 = new MessageEmbed()
                .setColor("DARK_VIVID_PINK")
                .setDescription(
                    `Reload \`${files.join(", ").replace(/Command.ts/, "")}\``
                );
            await interaction.editReply({ embeds: [embed2], components: [] });
            return;
        }
    });
    collector.on("end", async (coll, reason) => {
        await interaction.deleteReply();
        return;
    });
    return;
    function MCFCD(input: string) {
        input = input.replace(/-/, "") + "Command.ts";
        return input;
    }
}
