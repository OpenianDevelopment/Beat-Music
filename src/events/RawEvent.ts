import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";

export default class RawEvent extends BaseEvent {
    constructor() {
        super("raw");
    }
    async run(client: DiscordClient, data: any) {
        client.manager.updateVoiceState(data);
    }
}
