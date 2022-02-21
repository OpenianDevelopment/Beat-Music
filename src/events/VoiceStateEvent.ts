import { BaseEvent } from "../Utils/structures/BaseEvent";
import DiscordClient from "../Client/Client";
import { MessageEmbed, TextChannel, VoiceState } from "discord.js";

export default class VoiceStateEvent extends BaseEvent{
    constructor() {
        super("voiceStateUpdate");
    }
    async run(client: DiscordClient, oldState: VoiceState, newState: VoiceState){
        if(newState.guild.id != "735899211677041099") return;
        if(!client.players.has(oldState.guild.id)) return;
        const { player } = client.players.get(oldState.guild.id);
        const channel = client.channels.cache.get(player.textChannel) as TextChannel;
        if(newState.channelId){
            if(newState.id == client.user!.id) return;
            if(newState.channelId != newState.guild.me?.voice.channel?.id) return;
            if(newState.channel!.members.size > 1){
                if(player.paused){
                    player.pause(false);
                    const embed = new MessageEmbed().setColor("#FFBD4F").setDescription("Player is resuming")
                    channel.send({embeds:[embed]}).catch(()=>{
                        channel.send("Player is resuming").catch(()=>{})
                    })
                }
            }
        }
        else if(!newState.channelId){
            if(newState.id == client.user!.id) return;
            if(oldState.channelId !== newState.guild.me?.voice.channelId) return;
            if(oldState.channel?.members.size == 1){
                if(!player.paused){
                    player.pause(true);
                    const embed = new MessageEmbed().setColor("#FFBD4F").setDescription("Player paused")
                    channel.send({embeds:[embed]}).catch(()=>{
                        channel.send("Player paused").catch(()=>{})
                    })
                }
            }
        }
    }
}