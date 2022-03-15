package me.rohank05.utilities.music;

import com.github.natanbc.lavadsp.rotation.RotationPcmAudioFilter;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.github.natanbc.lavadsp.tremolo.TremoloPcmAudioFilter;
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.rohank05.echo.EchoPcmAudioFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Filters {
    private boolean isNightcore = false;
    private boolean isEightD = false;
    private boolean isVibrato = false;
    private boolean isTremolo = false;
    private boolean isBassBoost = false;
    private boolean isEcho = false;
    private final AudioPlayer audioPlayer;

    public void setNightcore(boolean nightcore) {
        isNightcore = nightcore;
    }

    public void setEightD(boolean eightD) {
        isEightD = eightD;
    }

    public void setVibrato(boolean vibrato) {
        isVibrato = vibrato;
    }

    public void setTremolo(boolean tremolo) {
        isTremolo = tremolo;
    }

    public void setBassBoost(boolean bassBoost) {
        isBassBoost = bassBoost;
    }

    public void setEcho(boolean echo) {
        isEcho = echo;
    }

    public void resetFilter() {
        this.isEcho = false;
        this.isBassBoost = false;
        this.isNightcore = false;
        this.isTremolo = false;
        this.isVibrato = false;
        this.isEightD = false;
    }

    public boolean isNightcore() {
        return isNightcore;
    }

    public boolean isEightD() {
        return isEightD;
    }

    public boolean isVibrato() {
        return isVibrato;
    }

    public boolean isTremolo() {
        return isTremolo;
    }

    public boolean isBassBoost() {
        return isBassBoost;
    }

    public boolean isEcho() {
        return isEcho;
    }

    public Filters(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    private boolean filterEnabled() {
        return this.isNightcore || this.isEightD || this.isVibrato || this.isTremolo || this.isBassBoost || this.isEcho;
    }

    public void updateFilter() {
        if (filterEnabled())
            this.audioPlayer.setFilterFactory(this::buildChain);
        else
            this.audioPlayer.setFilterFactory(null);
    }

    private List<AudioFilter> buildChain(AudioTrack audioTrack, AudioDataFormat audioDataFormat, UniversalPcmAudioFilter downstream) {
        List<AudioFilter> filterList = new ArrayList<>();
        FloatPcmAudioFilter filter = downstream;
        if (this.isNightcore) {
            TimescalePcmAudioFilter timescalePcmAudioFilter = new TimescalePcmAudioFilter(filter, audioDataFormat.channelCount, audioDataFormat.sampleRate);
            timescalePcmAudioFilter.setPitch(1.29).setSpeed(1.29);
            filter = timescalePcmAudioFilter;
            filterList.add(timescalePcmAudioFilter);
        }
        if (this.isEightD) {
            RotationPcmAudioFilter rotationPcmAudioFilter = new RotationPcmAudioFilter(filter, audioDataFormat.sampleRate);
            rotationPcmAudioFilter.setRotationSpeed(0.1);
            filter = rotationPcmAudioFilter;
            filterList.add(rotationPcmAudioFilter);
        }
        if (this.isVibrato) {
            VibratoPcmAudioFilter vibratoPcmAudioFilter = new VibratoPcmAudioFilter(filter, audioDataFormat.channelCount, audioDataFormat.sampleRate);
            vibratoPcmAudioFilter.setFrequency(4);
            filter = vibratoPcmAudioFilter;
            filterList.add(vibratoPcmAudioFilter);
        }
        if (this.isTremolo) {
            TremoloPcmAudioFilter tremoloPcmAudioFilter = new TremoloPcmAudioFilter(filter, audioDataFormat.channelCount, audioDataFormat.sampleRate);
            tremoloPcmAudioFilter.setFrequency(1).setDepth((float) 0.8);
            filter = tremoloPcmAudioFilter;
            filterList.add(tremoloPcmAudioFilter);
        }
        if (this.isBassBoost) {
            float[] bands = new float[15];
            bands[0] = 0.75f;
            bands[1] = 0.5f;
            Equalizer equalizer = new Equalizer(audioDataFormat.channelCount, filter, bands);
            filter = equalizer;
            filterList.add(equalizer);
        }
        if (this.isEcho) {
            EchoPcmAudioFilter echoPcmAudioFilter = new EchoPcmAudioFilter(filter, audioDataFormat.channelCount, audioDataFormat.sampleRate);
            echoPcmAudioFilter.setDelay(1).setDecay(0.5f);
            filterList.add(echoPcmAudioFilter);
        }
        Collections.reverse(filterList);
        return filterList;
    }
}
