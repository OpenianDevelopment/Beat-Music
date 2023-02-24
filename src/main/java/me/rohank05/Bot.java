package me.rohank05;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Bot{
    public static EventWaiter eventWaiter = new EventWaiter();
    public static void main(String[] args) throws InterruptedException {
        JDABuilder.createDefault(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS
                ).enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new EventListeners(), eventWaiter)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setActivity(Activity.listening("to The Score"))
                .build()
                .awaitReady();
    }
}
