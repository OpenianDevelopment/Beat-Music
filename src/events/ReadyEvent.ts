import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";

export default class ReadyEvent extends BaseEvent {
    constructor() {
        super("ready");
    }
    async run(client: DiscordClient) {
        console.log(client.user!.tag, "has logged in");
        client.manager.init(client.user!.id);
    }
}
