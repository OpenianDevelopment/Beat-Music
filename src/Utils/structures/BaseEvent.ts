import DiscordClient from "../../Client/Client";

export abstract class BaseEvent {
    constructor(private name: string) {}
    getName(): string {
        return this.name;
    }
    abstract run(client: DiscordClient, ...args: any): void;
}
