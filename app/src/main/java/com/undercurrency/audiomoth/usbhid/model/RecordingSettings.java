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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * RecordingSettings a POJO holding all the AM settings
 */
public class RecordingSettings {

    private static int MAX_PERIODS = 5;
    private static int SECONDS_IN_DAY = 86400;
    private static int UINT16_MAX = 0xFFFF;
    private static int UINT32_MAX = 0xFFFFFFFF;

    private DeviceInfo deviceInfo;
    private TimePeriods[] timePeriods;
    private boolean ledEnabled;
    private boolean lowVoltageCutoffEnabled;
    private boolean batteryLevelCheckEnabled;
    private byte sampleRate;
    private byte gain;
    private byte recordDuration;
    private byte sleepDuration;
    private boolean localTime;
    private boolean dutyEnabled;
    private boolean passFiltersEnabled;
    private FilterType filterType;
    private int lowerFilter;
    private int higherFilter;
    boolean amplitudeThresholdingEnabled;
    private byte amplitudeTreshold;
    private Date firstRecordinDate;
    private Date lastRecordingDate;

    public RecordingSettings(DeviceInfo deviceInfo, TimePeriods[] timePeriods, boolean ledEnabled, boolean lowVoltageCutoffEnabled, boolean batteryLevelCheckEnabled, byte sampleRate, byte gain, byte recordDuration, byte sleepDuration, boolean localTime, boolean dutyEnabled, boolean passFiltersEnabled, FilterType filterType, int lowerFilter, int higherFilter, boolean amplitudeThresholdingEnabled, byte amplitudeTreshold, Date firstRecordinDate, Date lastRecordingDate) {
        this.deviceInfo = deviceInfo;
        this.timePeriods = timePeriods;
        this.ledEnabled = ledEnabled;
        this.lowVoltageCutoffEnabled = lowVoltageCutoffEnabled;
        this.batteryLevelCheckEnabled = batteryLevelCheckEnabled;
        this.sampleRate = sampleRate;
        this.gain = gain;
        this.recordDuration = recordDuration;
        this.sleepDuration = sleepDuration;
        this.localTime = localTime;
        this.dutyEnabled = dutyEnabled;
        this.passFiltersEnabled = passFiltersEnabled;
        this.filterType = filterType;
        this.lowerFilter = lowerFilter;
        this.higherFilter = higherFilter;
        this.amplitudeThresholdingEnabled = amplitudeThresholdingEnabled;
        this.amplitudeTreshold = amplitudeTreshold;
        this.firstRecordinDate = firstRecordinDate;
        this.lastRecordingDate = lastRecordingDate;
    }

    /**
     * Convert the Recording Settings object to the byte array representation for AM
     * AM uses this byte array to define the operation modes in runtime.
     * In the firmware, when the device receives the config data via USB in a form of byte array, it
     * copies to a C structure called configSettings_t, and returns it back to the USB connection
     *
     * @return a byte array
     * @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Firmware-Basic/blob/1.4.4/main.c#L954></a>
     * <p>
     * Note: the javascript cousin of this code checks the presence in the ui of < 3 sample rate
     * settings to determine the semantic version, we will ignore this, after comparing
     * the firmware 1.4.4 to 1.3.0, the struct in the firmware differs from several fields,
     * blame the bizarre logic around this check. If it is lower to 1.4.4 it only have 3 posible configurations
     * @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Configuration-App/blob/master/uiIndex.js#L217"></a>
     * an also @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Configuration-App/blob/master/constants.js#L83"></a>
     */
    public byte[] serializeToBytes() {
        Configurations config;
        byte[] serialization = new byte[58];
        int unixTime = (int) (System.currentTimeMillis() / 1000);
        int index = 0;
        writeLittleEndianBytes(serialization, index, 4, unixTime);
        index += 4;
        serialization[index++] = this.gain;
        config = Configurations.getConfig(sampleRate, deviceInfo.isOlderSemanticVersion());
        serialization[index++] = config.getClockDivider();
        serialization[index++] = config.getAcquisitionCycles();
        serialization[index++] = config.getOversampleRate();
        writeLittleEndianBytes(serialization, index, 4, config.getSampleRate());
        index += 4;
        serialization[index++] = config.getSampleRateDivider();
        writeLittleEndianBytes(serialization, index, 2, getSleepDuration());
        index += 2;
        writeLittleEndianBytes(serialization, index, 2, getRecordDuration());
        index += 2;
        serialization[index++] = (byte) (isLedEnabled() ? 1 : 0);
        Arrays.sort(timePeriods);
        serialization[index++] = (byte) timePeriods.length;
        for (int i = 0; i < timePeriods.length; i++) {
            writeLittleEndianBytes(serialization, index, 2, timePeriods[i].getStartMins());
            index += 2;
            writeLittleEndianBytes(serialization, index, 2, timePeriods[i].getEndMins());
            index += 2;
        }
        for (int i = 0; i < MAX_PERIODS - timePeriods.length; i++) {
            writeLittleEndianBytes(serialization, index, 2, 0);
            index += 2;
            writeLittleEndianBytes(serialization, index, 2, 0);
            index += 2;
        }
        serialization[index++] = (byte) (isLocalTime() ? calculateTimezoneOffsetHours() : 0);
        serialization[index++] = (byte) (isLowVoltageCutoffEnabled() ? 1 : 0);
        serialization[index++] = (byte) (isBatteryLevelCheckEnabled() ? 1 : 0);
        /* For non-integer timezones */
        serialization[index++] = (byte) (isLocalTime() ? calculateTimezoneOffsetMins() : 0);

        /* Duty cycle disabled (default value = 0) */
        serialization[index++] = (byte) (isDutyEnabled() ? 1 : 0);

        /* Start/stop dates */

        int earliestRecordingTime = 0;
        /* If the timezone difference has caused the day to differ from the day as a UTC time, undo the offset */
        if (getFirstRecordinDate() != null && isLocalTime()) {
            earliestRecordingTime = fixTimeZone(getFirstRecordinDate());
        }

        int lastRecordingTime = 0;
        Date lastRecordingDateTimestamp = new Date();
        if (getLastRecordingDate() != null && isLocalTime()) {
            /* Make latestRecordingTime timestamp inclusive by setting it to the end of the chosen day */
           lastRecordingTime = fixTimeZone(getLastRecordingDate()) + SECONDS_IN_DAY;
        }

        /* Check ranges of values before sending */
        earliestRecordingTime = Math.min(UINT32_MAX, earliestRecordingTime);
        lastRecordingTime = Math.min(UINT32_MAX, lastRecordingTime);

        writeLittleEndianBytes(serialization, index, 4, earliestRecordingTime);
        index += 4;
        writeLittleEndianBytes(serialization, index, 4, lastRecordingTime);
        index += 4;

        /* Filter settings */
        if (isPassFiltersEnabled()) {
            switch (getFilterType()) {
                case LOW:
                    setLowerFilter(UINT16_MAX);
                    setHigherFilter((int) (getHigherFilter() / 100));
                    break;
                case HIGH:
                    setLowerFilter(getLowerFilter() / 100);
                    setHigherFilter(UINT16_MAX);
                    break;
                case BAND:
                    setLowerFilter(getLowerFilter() / 100);
                    setHigherFilter(getHigherFilter() / 100);
                    break;

            }
        } else {
            setLowerFilter(0);
            setHigherFilter(0);
        }
        writeLittleEndianBytes(serialization, index, 2, getLowerFilter());
        index += 2;
        writeLittleEndianBytes(serialization, index, 2, getHigherFilter());
        index += 2;
        /* CMV settings */
        writeLittleEndianBytes(serialization, index, 2, getAmplitudeTreshold());
        index += 2;

        return serialization;
    }

    private int fixTimeZone(Date aDate) {
        DateTime today = new DateTime();
        int dayDiff = today.getDayOfMonth()-  (new DateTime(DateTimeZone.UTC)).getDayOfMonth();
        int timezoneOffset = -60 * dayDiff;

        int day = new DateTime(aDate).getDayOfMonth() - dayDiff;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(aDate);
        int seconds = calendar.get(Calendar.SECOND)-timezoneOffset;
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.SECOND,seconds);
        return (int) (calendar.getTime().getTime())/10000;
    }

    private int calculateTimezoneOffsetMins() {
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / 1000 / 60;
        return tzOffset;
    }

    private int calculateTimezoneOffsetHours() {
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / 1000 / 60 / 60;
        return tzOffset;
    }

    private void writeLittleEndianBytes(byte[] buffer, int start, int byteCount, int value) {
        for (int i = 0; i < byteCount; i++) {
            buffer[start + i] = (byte) ((value >> (i * 8)) & 255);
        }
    }


    public TimePeriods[] getTimePeriods() {
        return timePeriods;
    }

    public void setTimePeriods(TimePeriods[] timePeriods) {
        this.timePeriods = timePeriods;
    }

    public boolean isLedEnabled() {
        return ledEnabled;
    }

    public void setLedEnabled(boolean ledEnabled) {
        this.ledEnabled = ledEnabled;
    }

    public boolean isLowVoltageCutoffEnabled() {
        return lowVoltageCutoffEnabled;
    }

    public void setLowVoltageCutoffEnabled(boolean lowVoltageCutoffEnabled) {
        this.lowVoltageCutoffEnabled = lowVoltageCutoffEnabled;
    }

    public boolean isBatteryLevelCheckEnabled() {
        return batteryLevelCheckEnabled;
    }

    public void setBatteryLevelCheckEnabled(boolean batteryLevelCheckEnabled) {
        this.batteryLevelCheckEnabled = batteryLevelCheckEnabled;
    }

    public byte getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(byte sampleRate) {
        this.sampleRate = sampleRate;
    }

    public byte getGain() {
        return gain;
    }

    public void setGain(byte gain) {
        this.gain = gain;
    }

    public byte getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(byte recordDuration) {
        this.recordDuration = recordDuration;
    }

    public byte getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(byte sleepDuration) {
        this.sleepDuration = sleepDuration;
    }

    public boolean isLocalTime() {
        return localTime;
    }

    public void setLocalTime(boolean localTime) {
        this.localTime = localTime;
    }

    public boolean isDutyEnabled() {
        return dutyEnabled;
    }

    public void setDutyEnabled(boolean dutyEnabled) {
        this.dutyEnabled = dutyEnabled;
    }

    public boolean isPassFiltersEnabled() {
        return passFiltersEnabled;
    }

    public void setPassFiltersEnabled(boolean passFiltersEnabled) {
        this.passFiltersEnabled = passFiltersEnabled;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public int getLowerFilter() {
        return lowerFilter;
    }

    public void setLowerFilter(int lowerFilter) {
        this.lowerFilter = lowerFilter;
    }

    public int getHigherFilter() {
        return higherFilter;
    }

    public void setHigherFilter(int higherFilter) {
        this.higherFilter = higherFilter;
    }

    public boolean isAmplitudeThresholdingEnabled() {
        return amplitudeThresholdingEnabled;
    }

    public void setAmplitudeThresholdingEnabled(boolean amplitudeThresholdingEnabled) {
        this.amplitudeThresholdingEnabled = amplitudeThresholdingEnabled;
    }

    public byte getAmplitudeTreshold() {
        return amplitudeTreshold;
    }

    public void setAmplitudeTreshold(byte amplitudeTreshold) {
        this.amplitudeTreshold = amplitudeTreshold;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Date getFirstRecordinDate() {
        return firstRecordinDate;
    }

    public void setFirstRecordinDate(Date firstRecordinDate) {
        this.firstRecordinDate = firstRecordinDate;
    }

    public Date getLastRecordingDate() {
        return lastRecordingDate;
    }

    public void setLastRecordingDate(Date lastRecordingDate) {
        this.lastRecordingDate = lastRecordingDate;
    }

}

