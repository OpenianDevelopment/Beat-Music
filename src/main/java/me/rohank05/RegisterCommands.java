package me.rohank05;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.rohank05.utilities.command.CommandRegister;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class RegisterCommands {
    public static EventWaiter eventWaiter = new EventWaiter();
    public static void main(String[] args) throws InterruptedException, LoginException {
        JDABuilder.createDefault(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.STICKER,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS
                ).enableCache(CacheFlag.VOICE_STATE)
                .addEventListeners(new CommandRegister(),eventWaiter)
                .build()
                .awaitReady();
        /*
            this starts up teh bot for registering and then calls ./utilities/command/CommandRegister to register the
            commands. Edit that file to update slash commands.
        */
    }
}
