package me.rohank05;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {
    public static EventWaiter eventWaiter = new EventWaiter();
    public static void main(String[] args) throws InterruptedException, LoginException {
        JDABuilder.createDefault(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES)
                .disableCache(EnumSet.of(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS
                )).enableCache(EnumSet.of(CacheFlag.VOICE_STATE))
                .addEventListeners(new EventListeners(), eventWaiter)
                .setAudioSendFactory(new NativeAudioSendFactory())
                .setActivity(Activity.listening("to The Score"))
                .build()
                .awaitReady();

    }
}
