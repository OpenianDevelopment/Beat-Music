package me.rohank05.beat.Filter;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;

import java.util.ArrayList;
import java.util.List;


public class Merger {
    private static Merger INSTANCE;
    private boolean NightcoreEnabled = false;
    private boolean EightDEnabled = false;
    private FloatPcmAudioFilter lastOutput;
    private List<AudioFilter> audioFilters = new ArrayList<AudioFilter>();
    public Merger(){}

    public boolean isNightcoreEnabled() {
        return NightcoreEnabled;
    }

    public boolean isEightDEnabled() {
        return EightDEnabled;
    }

    public void setEightDEnabled(boolean eightDEnabled) {
        EightDEnabled = eightDEnabled;
    }

    public void setNightcoreEnabled(boolean nightcoreEnabled) {
        NightcoreEnabled = nightcoreEnabled;
    }

    public List<AudioFilter> enableFilter(FloatPcmAudioFilter output, AudioDataFormat format){
        audioFilters.clear();
        this.lastOutput = output;
        if(this.isNightcoreEnabled()){
            TimescalePcmAudioFilter nightCoreFilter = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
            nightCoreFilter.setSpeed(1.29).setPitch(1.29);
            audioFilters.add(0, nightCoreFilter);
        }
        if(this.isEightDEnabled()){
            RotationPcmAudioFilter eightDFilter = new RotationPcmAudioFilter(output, format.sampleRate);
            eightDFilter.setRotationSpeed(0.2);
            audioFilters.add(0, eightDFilter);
        }
        return audioFilters;
    }

    public static Merger getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new Merger();
        }
        return INSTANCE;
    }
}
