import DiscordClient from "../../Client/Client";
import { CommandInteraction } from "discord.js";

export abstract class BaseCommand {
    constructor(
        private _name: string,
        private _description: string,
        private _usage: string,
        private _example: string
    ) {}

    get name(): string {
        return this._name;
    }

    get description(): string {
        return this._description;
    }

    get usage(): string {
        return this._usage;
    }

    get example(): string {
        return this._usage;
    }

    abstract run(client: DiscordClient, interaction: CommandInteraction): void;
}
