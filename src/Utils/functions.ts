import {
    CommandInteraction,
    GuildMember,
    Interaction,
    InteractionCollector,
    MessageActionRow,
    MessageButton,
    MessageEmbed,
} from "discord.js";
import DiscordClient from "../Client/Client";
import { Schema, model, Types, Document } from "mongoose";

export async function checkMusicPermission(
    client: DiscordClient,
    interaction: CommandInteraction
) {
    const member = interaction.member as GuildMember;
    const guild = interaction.guild!;
    if (!client.players.has(guild.id)) {
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("❗ No Music is playing");
        await interaction.reply({ embeds: [embed], ephemeral: true });
        return false;
    } else if (member.voice.channel !== guild.me!.voice.channel) {
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription("❗ You need to be in same channel as me");
        await interaction.reply({ embeds: [embed], ephemeral: true });
        return false;
    }
    return true;
}

export async function PageInteraction(
    embeds: MessageEmbed[],
    interaction: CommandInteraction
) {
    let currentPage = 0;
    const bothButton = new MessageActionRow().addComponents(
        new MessageButton()
            .setCustomId("previous")
            .setEmoji("⬅️")
            .setStyle("PRIMARY")
            .setLabel("Previous"),
        new MessageButton()
            .setCustomId("forward")
            .setEmoji("➡️")
            .setStyle("PRIMARY")
            .setLabel("Next")
    );
    const forwardButton = new MessageActionRow().addComponents(
        new MessageButton()
            .setCustomId("forward")
            .setStyle("PRIMARY")
            .setLabel("Next")
            .setEmoji("➡️")
    );
    const prevButton = new MessageActionRow().addComponents(
        new MessageButton()
            .setCustomId("previous")
            .setStyle("PRIMARY")
            .setEmoji("⬅️")
            .setLabel("Previous")
    );
    await interaction.reply({
        embeds: [embeds[0]],
        components: [forwardButton],
    });
    const filter = (m: Interaction) => interaction.user.id === m.user.id;
    const collector = interaction.channel!.createMessageComponentCollector({
        filter,
        time: 60000,
    });
    collector.on("collect", async (i) => {
        if (i.customId === "forward") {
            if (currentPage < embeds.length - 1) {
                currentPage++;
                if (currentPage === embeds.length - 1) {
                    await i.update({
                        embeds: [embeds[currentPage]],
                        components: [prevButton],
                    });
                } else {
                    await i.update({
                        embeds: [embeds[currentPage]],
                        components: [bothButton],
                    });
                }
            }
        } else if (i.customId === "previous") {
            if (currentPage !== 0) {
                --currentPage;
                if (currentPage === 0) {
                    await i.update({
                        embeds: [embeds[currentPage]],
                        components: [forwardButton],
                    });
                } else {
                    await i.update({
                        embeds: [embeds[currentPage]],
                        components: [bothButton],
                    });
                }
            }
        }
    });
}
