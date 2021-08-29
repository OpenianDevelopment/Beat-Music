import { Client, ClientOptions, Collection } from "discord.js";
import { BaseCommand } from "../Utils/structures/BaseCommand";
import { BaseEvent } from "../Utils/structures/BaseEvent";
import { Manager } from "erela.js";
import { initErela } from "../Utils/registry";
export default class DiscordClient extends Client {
    private _commands = new Collection<string, BaseCommand>();
    private _events = new Collection<string, BaseEvent>();
    public players = new Map();
    public manager: Manager = initErela(this);
    public songCount!: number;

    constructor(options: ClientOptions) {
        super(options);
    }

    get commands(): Collection<string, BaseCommand> {
        return this._commands;
    }
    get events(): Collection<string, BaseEvent> {
        return this._events;
    }
}
