package me.rohank05.beat;


import me.rohank05.beat.lavaplayer.PlayerManager;
import me.rohank05.beat.paginator.PageManager;
import me.rohank05.beat.paginator.Paginator;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;


public class Listeners extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Listeners.class);
    private final CommandManager manager = new CommandManager();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        JDA jda = event.getJDA();
        LOGGER.info("{} Ready!!", jda.getSelfUser().getAsTag());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        manager.run(event);
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        /**
         * Check if Bot has left the channel. If it had it will destroy the player
         */
        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel())
            if (PlayerManager.getINSTANCE() != null)
                if (PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null)
                    PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.destroy();
        /**
         * Check if is the only member left in the channel. If yes then it will pause the song until another member join
         */
        if (event.getChannelLeft().getMembers().size() == 1)
            if (event.getChannelLeft().getMembers().get(0).equals(event.getGuild().getSelfMember()))
                if (PlayerManager.getINSTANCE() != null)
                    PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setPaused(true);
    }


    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {

        /**
         * Resumes the player when another users join
         */
        if (event.getChannelJoined().getMembers().contains(event.getGuild().getSelfMember()))
            if (event.getChannelJoined().getMembers().size() > 1)
                if (PlayerManager.getINSTANCE() != null)
                    if (PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.isPaused())
                        PlayerManager.getINSTANCE().getMusicManager(event.getGuild()).audioPlayer.setPaused(false);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        long id = event.getMessageIdLong();
        Map<Long, Paginator> pagination = PageManager.getINSTANCE().getPaginator();
        if(!pagination.containsKey(id)) return;
        Paginator pages = pagination.get(id);
        if(!pages.userId.equals(event.getUser().getIdLong())) return;
        if(event.getButton().getLabel().equals("forward")){
            if(pages.page == pages.embeds.size()-1){
                event.deferReply(true).queue();
                event.getInteraction().getHook().sendMessage("Already on the last page").queue();
                return;
            }
            MessageEmbed embed = pages.embeds.get(pages.page+1);
            event.editMessageEmbeds(embed).queue();
            pages.increasePage();
        }
        else if(event.getButton().getLabel().equals("backward")){
            if(pages.page == 0){
                event.deferReply(true).queue();
                event.getInteraction().getHook().setEphemeral(true).sendMessage("Already on the first page").queue();
                return;
            }
            MessageEmbed embed = pages.embeds.get(pages.page+1);
            event.editMessageEmbeds(embed).queue();
            pages.decreasePage();
        }
    }
}
