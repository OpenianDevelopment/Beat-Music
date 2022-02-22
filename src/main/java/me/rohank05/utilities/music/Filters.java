package me.rohank05.utilities.music;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filters {
    private boolean isNightcore = false;
    private boolean isEightD = false;
    private final AudioPlayer audioPlayer;

    public void setNightcore(boolean nightcore) {
        isNightcore = nightcore;
    }

    public void setEightD(boolean eightD) {
        isEightD = eightD;
    }

    public boolean isNightcore() {
        return isNightcore;
    }

    public boolean isEightD() {
        return isEightD;
    }

    public Filters(AudioPlayer audioPlayer){
        this.audioPlayer = audioPlayer;
    }

    private boolean filterEnabled(){
        if(this.isNightcore || this.isEightD)
            return true;
        return false;
    }
    public void updateFilter(){
        if(filterEnabled())
            this.audioPlayer.setFilterFactory(this::buildChain);
        else
            this.audioPlayer.setFilterFactory(null);
    }

    private List<AudioFilter> buildChain(AudioTrack audioTrack, AudioDataFormat audioDataFormat, UniversalPcmAudioFilter downstream){
        System.out.println("working");
        List<AudioFilter> filterList = new ArrayList<>();
        FloatPcmAudioFilter filter = downstream;
        if(this.isNightcore){
            TimescalePcmAudioFilter timescalePcmAudioFilter = new TimescalePcmAudioFilter(filter, audioDataFormat.channelCount, audioDataFormat.sampleRate);
            timescalePcmAudioFilter.setPitch(1.29).setSpeed(1.29);
            filter = timescalePcmAudioFilter;
            filterList.add(timescalePcmAudioFilter);
        }
        if(this.isEightD){
            RotationPcmAudioFilter rotationPcmAudioFilter = new RotationPcmAudioFilter(filter, audioDataFormat.sampleRate);
            rotationPcmAudioFilter.setRotationSpeed(0.1);
            filter = rotationPcmAudioFilter;
            filterList.add(rotationPcmAudioFilter);
        }
        Collections.reverse(filterList);
        return filterList;
    }
}
