package me.rohank05.beat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;

public class Bot {

    public static void main(String[] args) throws LoginException {
        try {
            JDA jda = JDABuilder
                    .createLight(Config.get("TOKEN"), GatewayIntent.GUILD_VOICE_STATES)
                    .addEventListeners(new Listeners())
                    .setActivity(Activity.listening("Chill Songs"))
                    .build();
            jda.awaitReady();
            System.out.println("Finished Building JDA!!");
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
