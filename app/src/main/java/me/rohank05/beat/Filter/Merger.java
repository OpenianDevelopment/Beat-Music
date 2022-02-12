/**
 * Copyright (c) 2022 Rohan Kumar and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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

    public Merger() {
    }

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

    public List<AudioFilter> enableFilter(FloatPcmAudioFilter output, AudioDataFormat format) {
        audioFilters.clear();
        this.lastOutput = output;
        if (this.isNightcoreEnabled()) {
            TimescalePcmAudioFilter nightCoreFilter = new TimescalePcmAudioFilter(this.lastOutput, format.channelCount, format.sampleRate);
            nightCoreFilter.setSpeed(1.29).setPitch(1.29);
            this.lastOutput = nightCoreFilter;
            audioFilters.add(0, nightCoreFilter);
        }
        if (this.isEightDEnabled()) {
            RotationPcmAudioFilter eightDFilter = new RotationPcmAudioFilter(this.lastOutput, format.sampleRate);
            eightDFilter.setRotationSpeed(0.2);
            this.lastOutput = eightDFilter;
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
