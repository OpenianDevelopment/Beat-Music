package me.rohank05.beat;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.rohank05.beat.command.commands.QueueCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {
    private final EventWaiter eventWaiter = new EventWaiter();
    public static void main(String[] args) throws LoginException {
        new Bot().start();
    }
    public void start() throws LoginException{
        JDABuilder
                .createDefault(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new Listeners(), new QueueCommand(this), eventWaiter)
                .setActivity(Activity.listening("Chill Songs"))
                .disableCache(EnumSet.of(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS
                )).enableCache(EnumSet.of(CacheFlag.VOICE_STATE))
                .build();
    }
    public EventWaiter getEventWaiter()
    {
        return eventWaiter;
    }
}
