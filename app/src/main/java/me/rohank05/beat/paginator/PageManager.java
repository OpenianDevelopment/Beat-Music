package me.rohank05.beat.paginator;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageManager {
    private static PageManager INSTANCE;
    public final Map<Long, Paginator> paginator;

    public PageManager() {
        this.paginator = new HashMap<>();
    }

    public Map<Long, Paginator> getPaginator() {
        return this.paginator;
    }

    public void setPaginator(Long id, ArrayList<MessageEmbed> embeds, Integer page, Long userId){
        this.paginator.computeIfAbsent(id, (messageId)->{
            final Paginator paginator = new Paginator(embeds, page, userId);
            return paginator;
        });
    }

    public static PageManager getINSTANCE() {
        if(INSTANCE == null){
            INSTANCE = new PageManager();
        }
        return INSTANCE;
    }
}
