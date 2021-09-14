import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import {
    CommandInteraction,
    MessageActionRow,
    MessageButton,
    MessageEmbed,
} from "discord.js";
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
        const components = new MessageActionRow().addComponents([
            new MessageButton()
                .setStyle("LINK")
                .setURL(
                    "https://discord.com/oauth2/authorize?client_id=881050313870684180&scope=bot%20applications.commands"
                )
                .setLabel("Invite Bot"),
            new MessageButton()
                .setStyle("LINK")
                .setURL("https://discord.com/invite/fBKZngbHHb")
                .setLabel("Support Server"),
        ]);
        await interaction.followUp({
            embeds: [embed],
            components: [components],
        });
    }
}
