import { Schema, model } from "mongoose";

const SongSchema = new Schema({
    bot_id: String,
    count: Number,
});

export const song = model("song", SongSchema);
