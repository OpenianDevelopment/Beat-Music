package me.rohank05.beat.paginator;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;

public class Paginator {
    public final ArrayList<MessageEmbed> embeds;
    public Integer page;
    public final Long userId;


    public Paginator(ArrayList<MessageEmbed> embeds, Integer page, Long userId){
        this.embeds = embeds;
        this.page = page;
        this.userId = userId;
    }

    public void increasePage(){
        page++;
    }
    public void decreasePage(){
        page++;
    }
}
