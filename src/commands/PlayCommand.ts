import { BaseCommand } from "../Utils/structures/BaseCommand";
import DiscordClient from "../Client/Client";
import { CommandInteraction } from "discord.js";

export default class PlayCommand extends BaseCommand {
    constructor() {
        super("play", "Play a Song", "play <song name>", "play wonderwall");
    }
    async run(client: DiscordClient, interaction: CommandInteraction) {}
}
