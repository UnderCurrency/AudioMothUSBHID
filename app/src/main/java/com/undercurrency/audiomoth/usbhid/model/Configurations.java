/*
 *  (c)  Copyright 2020 Undercurrency
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.undercurrency.audiomoth.usbhid.model;

/**
 *
 */
public enum Configurations {
    SAMPLE_RATE_8(8,4,16,1,384000,48,11.0f,10.0f),
    SAMPLE_RATE_16(16,4,16,1,384000,24,11.2f, 10.9f),
    SAMPLE_RATE_32(32,4,16,1,384000, 12,11.5f,12.3f),
    SAMPLE_RATE_48(48,4,16,1,384000,8,11.8f,14.0f),
    SAMPLE_RATE_96(96,4,16,1,384000,4,12.7f,17.4f),
    SAMPLE_RATE_192(192,4,16,1,384000,2,14.5f, 25.6f),
    SAMPLE_RATE_250(250,4,16,1,250000,1,15.8f,29.5f),
    SAMPLE_RATE_384(384,4,16,1,384000,1,18.2f,41.6f),
    OLD_SAMPLE_RATE_8(8,4,16,1,128000,16,11.0f,10.0f),
    OLD_SAMPLE_RATE_16(16,4,16,1,128000,8,11.2f,10.9f),
    OLD_SAMPLE_RATE_32(32,4,16,1,128000,4,11.5f,12.3f)
    ;
    private final byte trueSampleRate;
    private final byte clockDivider;
    private final byte acquisitionCycles;
    private final byte oversampleRate;
    private final int sampleRate;
    private final byte sampleRateDivider;
    private final float startCurrent;
    private final float recordCurrent;

    Configurations(int trueSampleRate, int clockDivider, int acquisitionCycles, int oversampleRate, int sampleRate, int sampleRateDivider, float startCurrent, float recordCurrent) {
        this.trueSampleRate = (byte)trueSampleRate;
        this.clockDivider = (byte)clockDivider;
        this.acquisitionCycles = (byte)acquisitionCycles;
        this.oversampleRate = (byte)oversampleRate;
        this.sampleRate = sampleRate;
        this.sampleRateDivider = (byte) sampleRateDivider;
        this.startCurrent = startCurrent;
        this.recordCurrent = recordCurrent;
    }

    public static Configurations getConfig(int rateIndex, boolean oldConfig) {
        if (oldConfig) {
            switch (rateIndex) {
                case 8:
                    return Configurations.OLD_SAMPLE_RATE_8;
                case 16:
                    return Configurations.OLD_SAMPLE_RATE_16;
                case 32:
                    return Configurations.OLD_SAMPLE_RATE_32;
            }
        } else {
            switch (rateIndex) {
                case 8:
                    return Configurations.SAMPLE_RATE_8;
                case 16:
                    return Configurations.SAMPLE_RATE_16;
                case 32:
                    return Configurations.SAMPLE_RATE_32;
                case 48:
                    return Configurations.SAMPLE_RATE_48;
                case 96:
                    return Configurations.SAMPLE_RATE_96;
                case 192:
                    return Configurations.SAMPLE_RATE_192;
                case 250:
                    return Configurations.SAMPLE_RATE_250;
                case 384:
                    return Configurations.SAMPLE_RATE_384;
            }
        }
        return null;
    }

    public byte getTrueSampleRate() {
        return trueSampleRate;
    }

    public byte getClockDivider() {
        return clockDivider;
    }

    public byte getAcquisitionCycles() {
        return acquisitionCycles;
    }

    public byte getOversampleRate() {
        return oversampleRate;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public byte getSampleRateDivider() {
        return sampleRateDivider;
    }

    public float getStartCurrent() {
        return startCurrent;
    }

    public float getRecordCurrent() {
        return recordCurrent;
    }
}
