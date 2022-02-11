package me.rohank05.beat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {

    public static void main(String[] args) throws LoginException {
        try {
            JDA jda = JDABuilder
                    .createDefault(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES)
                    .addEventListeners(new Listeners())
                    .setActivity(Activity.listening("Chill Songs"))
                    .disableCache(EnumSet.of(
                            CacheFlag.ACTIVITY,
                            CacheFlag.EMOTE,
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.ONLINE_STATUS
                    )).enableCache(EnumSet.of(CacheFlag.VOICE_STATE))
                    .build();
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
