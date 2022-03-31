import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";

export default class VoteCommand extends BaseCommand {
    constructor() {
        super("vote", "Vote for the bot!");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        interaction.followUp({
            embeds: [
                new MessageEmbed()
                    .setColor("#FFBD4F")
                    .setDescription(
                        `You can vote at https://top.gg/bot/${client.user?.id}/vote every 12h!`
                    ),
            ],
        });
        return;
    }
}
