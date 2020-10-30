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

import static com.undercurrency.audiomoth.usbhid.USBUtils.readShortFromLittleEndian;
import static com.undercurrency.audiomoth.usbhid.USBUtils.readIntFromLittleEndian;
import static com.undercurrency.audiomoth.usbhid.USBUtils.writeIntToLittleEndian;
import static com.undercurrency.audiomoth.usbhid.USBUtils.writeShortToLittleEndian;



/**
 * RecordingSettings a POJO holding all the AM settings
 */
public class RecordingSettings {

    private static final int MAX_PERIODS = 5;
    private static final int SECONDS_IN_DAY = 86400;
    private static final int UINT16_MAX = 0xFFFF;
    private static final int UINT32_MAX = 0xFFFFFFFF;
    boolean amplitudeThresholdingEnabled;
    private DeviceInfo deviceInfo;
    private TimePeriods[] timePeriods;
    private boolean ledEnabled;
    private boolean lowVoltageCutoffEnabled;
    private boolean batteryLevelCheckEnabled;
    private int sampleRate;
    private byte gain;
    private short recordDuration;
    private short sleepDuration;
    private boolean localTime;
    private boolean dutyEnabled;
    private boolean passFiltersEnabled;
    private FilterType filterType;
    private int lowerFilter;
    private int higherFilter;
    private short amplitudeTreshold;
    private Date firstRecordinDate;
    private Date lastRecordingDate;

    public RecordingSettings() {
    }

    public RecordingSettings(DeviceInfo deviceInfo, TimePeriods[] timePeriods, boolean ledEnabled, boolean lowVoltageCutoffEnabled, boolean batteryLevelCheckEnabled, int sampleRate, byte gain, short recordDuration, short sleepDuration, boolean localTime, boolean dutyEnabled, boolean passFiltersEnabled, FilterType filterType, int lowerFilter, int higherFilter, boolean amplitudeThresholdingEnabled, byte amplitudeTreshold, Date firstRecordinDate, Date lastRecordingDate) {
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
     * @param array
     */
    public RecordingSettings(byte[] array) {
        int i=0;
        int fecha = readIntFromLittleEndian(array, 0);
        i += 4;
        setGain(array[i++]); //5
        int clockDivider = array[i++]; //6
        int acquisitionCycles = array[i++]; //7
        int oversampleRate = array[i++];//8
        int sampleRate = readIntFromLittleEndian(array, i);//9
        i += 4;
        int sampleRateDivider = array[i++];
        setSleepDuration(readShortFromLittleEndian(array, i));
        i += 2;
        setRecordDuration(readShortFromLittleEndian(array, i));
        i += 2;
        setLedEnabled(array[i++] != 0);
        int timePeriodsLength = array[i++];
        TimePeriods[] tp = new TimePeriods[timePeriodsLength + 1];

        for (int j = 0; j < timePeriodsLength; j++) {
            int intStartMins = readShortFromLittleEndian(array, i);
            i += 2;
            int intEndMins = readShortFromLittleEndian(array, i);
            i += 2;
            tp[j] = new TimePeriods(intStartMins, intEndMins);
        }
        setTimePeriods(tp);
        for (int k = 0; k < MAX_PERIODS - timePeriodsLength; k++) {
            i += 4;
        }
        setLocalTime(array[i++] != 0);
        setLowVoltageCutoffEnabled(array[i++] != 0);
        setBatteryLevelCheckEnabled(array[i++] != 0);
        i++;
        setDutyEnabled(array[i++] != 0);
        i += 4;
        i += 4;
        setPassFiltersEnabled(array[i] != 0 && array[i] != 0);
        setLowerFilter(readShortFromLittleEndian(array, i));
        i += 2;
        setHigherFilter(readShortFromLittleEndian(array, i));
        i += 2;
        setAmplitudeTreshold(readShortFromLittleEndian(array, i));
        i += 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecordingSettings that = (RecordingSettings) o;

        if (ledEnabled != that.ledEnabled) return false;
        if (lowVoltageCutoffEnabled != that.lowVoltageCutoffEnabled) return false;
        if (batteryLevelCheckEnabled != that.batteryLevelCheckEnabled) return false;
        if (sampleRate != that.sampleRate) return false;
        if (gain != that.gain) return false;
        if (recordDuration != that.recordDuration) return false;
        if (sleepDuration != that.sleepDuration) return false;
        if (localTime != that.localTime) return false;
        if (dutyEnabled != that.dutyEnabled) return false;
        if (passFiltersEnabled != that.passFiltersEnabled) return false;
        if (lowerFilter != that.lowerFilter) return false;
        if (higherFilter != that.higherFilter) return false;
        if (amplitudeThresholdingEnabled != that.amplitudeThresholdingEnabled) return false;
        if (amplitudeTreshold != that.amplitudeTreshold) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(timePeriods, that.timePeriods)) return false;
        if (filterType != that.filterType) return false;
        if (!firstRecordinDate.equals(that.firstRecordinDate)) return false;
        return lastRecordingDate.equals(that.lastRecordingDate);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(timePeriods);
        result = 31 * result + (ledEnabled ? 1 : 0);
        result = 31 * result + (lowVoltageCutoffEnabled ? 1 : 0);
        result = 31 * result + (batteryLevelCheckEnabled ? 1 : 0);
        result = 31 * result + sampleRate;
        result = 31 * result + (int) gain;
        result = 31 * result + (int) recordDuration;
        result = 31 * result + (int) sleepDuration;
        result = 31 * result + (localTime ? 1 : 0);
        result = 31 * result + (dutyEnabled ? 1 : 0);
        result = 31 * result + (passFiltersEnabled ? 1 : 0);
        result = 31 * result + filterType.hashCode();
        result = 31 * result + lowerFilter;
        result = 31 * result + higherFilter;
        result = 31 * result + (amplitudeThresholdingEnabled ? 1 : 0);
        result = 31 * result + (int) amplitudeTreshold;
        result = 31 * result + firstRecordinDate.hashCode();
        result = 31 * result + lastRecordingDate.hashCode();
        return result;
    }

    /**
     * Convert the Recording Settings object to the byte array representation for AM plus 0x06
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


        writeIntToLittleEndian(serialization, index,  unixTime);
        index += 4;
        serialization[index++] = getGain();
        config = Configurations.getConfig(getSampleRate() / 1000, getDeviceInfo().isOlderSemanticVersion());
        serialization[index++] = config.getClockDivider();
        serialization[index++] = config.getAcquisitionCycles();
        serialization[index++] = config.getOversampleRate();
        writeIntToLittleEndian(serialization, index, config.getSampleRate());
        index += 4;
        serialization[index++] = config.getSampleRateDivider();
        writeShortToLittleEndian(serialization, index,  getSleepDuration());
        index += 2;
        writeShortToLittleEndian(serialization, index,  getRecordDuration());
        index += 2;
        serialization[index++] = (byte) (isLedEnabled() ? 1 : 0);
        Arrays.sort(timePeriods);
        serialization[index++] = (byte) timePeriods.length;
        for (int i = 0; i < timePeriods.length; i++) {
            writeShortToLittleEndian(serialization, index,  (short)timePeriods[i].getStartMins());
            index += 2;
            writeShortToLittleEndian(serialization, index,  (short)timePeriods[i].getEndMins());
            index += 2;
        }
        for (int i = 0; i < MAX_PERIODS - timePeriods.length; i++) {
            writeShortToLittleEndian(serialization, index,(short) 0);
            index += 2;
            writeShortToLittleEndian(serialization, index, (short) 0);
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

        writeIntToLittleEndian(serialization, index, earliestRecordingTime);
        index += 4;
        writeIntToLittleEndian(serialization, index, lastRecordingTime);
        index += 4;

        /* Filter settings */
        if (isPassFiltersEnabled()) {
            switch (getFilterType()) {
                case LOW:
                    setLowerFilter(UINT16_MAX);
                    setHigherFilter(getHigherFilter() / 100);
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
        writeShortToLittleEndian(serialization, index, (short)getLowerFilter());
        index += 2;
        writeShortToLittleEndian(serialization, index,  (short)getHigherFilter());
        index += 2;
        /* CMV settings */
        writeShortToLittleEndian(serialization, index,  getAmplitudeTreshold());
        index += 2;

        return serialization;
    }

    private int fixTimeZone(Date aDate) {
        DateTime today = new DateTime();
        int dayDiff = today.getDayOfMonth() - (new DateTime(DateTimeZone.UTC)).getDayOfMonth();
        int timezoneOffset = -60 * dayDiff;

        int day = new DateTime(aDate).getDayOfMonth() - dayDiff;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(aDate);
        int seconds = calendar.get(Calendar.SECOND) - timezoneOffset;
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.SECOND, seconds);
        return (int) (calendar.getTime().getTime()) / 10000;
    }

    private int calculateTimezoneOffsetMins() {
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / 1000 / 60;
        return tzOffset;
    }

    private int calculateTimezoneOffsetHours() {
        int tzOffset = TimeZone.getDefault().getOffset(new Date().getTime()) / 1000 / 60 / 60;
        return tzOffset;
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

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public byte getGain() {
        return gain;
    }

    public void setGain(byte gain) {
        this.gain = gain;
    }

    public short getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(short recordDuration) {
        this.recordDuration = recordDuration;
    }

    public short getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(short sleepDuration) {
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

    public short getAmplitudeTreshold() {
        return amplitudeTreshold;
    }

    public void setAmplitudeTreshold(short amplitudeTreshold) {
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

