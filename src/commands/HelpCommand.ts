import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction, MessageEmbed } from "discord.js";
import { checkMusicPermission } from "../Utils/functions";

export default class HelpCommand extends BaseCommand {
    constructor() {
        super("help", "List all commands");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {
        let string = "";
        client.commands.forEach((command) => {
            string += `\`${command.name}\` - ${command.description}\n`;
        });
        const embed = new MessageEmbed()
            .setColor("#FFBD4F")
            .setDescription(string);
        await interaction.reply({ embeds: [embed] });
    }
}
