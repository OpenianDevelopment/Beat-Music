import { song } from "./SongSchema";
import DiscordClient from "../../Client/Client";
import { Document, Types } from "mongoose";

interface song extends Document {
    _id: Types.ObjectId;
    bot_id: string;
    count: number;
}

export async function setSongCount(client: DiscordClient) {
    const res = (await song.findOne({ bot_id: "881050313870684180" })) as song;
    client.songCount = res.count;
}

export async function updateSongCount(count: number) {
    song.updateOne({ bot_id: "881050313870684180" }, { $set: { count: count } })
        .then(console.log)
        .catch(console.error);
}
