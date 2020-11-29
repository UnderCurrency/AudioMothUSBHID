/*
 *
 *  (c)  Copyright 2020 Undercurrency
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.undercurrency.audiomoth.usbhid.model;

import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.tz.UTCProvider;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.readDateFromByteArray;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.readIntFromLittleEndian;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.readMillisFromByteArray;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.readShortFromLittleEndian;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.writeIntToLittleEndian;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.writeLongToLittleEndian;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.writeShortToLittleEndian;


/**
 * RecordingSettings a POJO holding all the AudioMoth settings
 */
public class RecordingSettings implements Serializable {
    private static final String TAG="RecordingSettings";
    private static final long serialVersionUID = 8799656478674716638L;
    private static final int MAX_PERIODS = 5;
    private static final int SECONDS_IN_DAY = 86400;
    private static final int UINT16_MAX = 0xFFFF;
    private static final int UINT32_MAX = 0xFFFFFFFF;
    boolean amplitudeThresholdingEnabled;
    private transient DeviceInfo deviceInfo;
    private ArrayList<TimePeriods> timePeriods = new ArrayList(5);
    private boolean ledEnabled;
    private boolean lowVoltageCutoffEnabled;
    private boolean batteryLevelCheckEnabled;
    private int sampleRate;
    private byte gain;
    private int recordDuration;
    private int sleepDuration;
    private boolean localTime;
    private boolean dutyEnabled;
    private boolean passFiltersEnabled;
    private FilterType filterType;
    private int lowerFilter;
    private int higherFilter;
    private int amplitudeThreshold;
    private transient boolean firstRecordingEnable;
    private transient boolean lastRecordingEnable;
    private Date firstRecordingDate;
    private Date lastRecordingDate;

    public RecordingSettings() {
    }

    public RecordingSettings(DeviceInfo deviceInfo, ArrayList<TimePeriods> timePeriods, boolean ledEnabled, boolean lowVoltageCutoffEnabled, boolean batteryLevelCheckEnabled, int sampleRate, byte gain, short recordDuration, short sleepDuration, boolean localTime, boolean dutyEnabled, boolean passFiltersEnabled, FilterType filterType, int lowerFilter, int higherFilter, boolean amplitudeThresholdingEnabled, byte amplitudeThreshold, Date firstRecordingDate, Date lastRecordingDate) {
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
        this.amplitudeThreshold = amplitudeThreshold;
        this.firstRecordingDate = firstRecordingDate;
        this.lastRecordingDate = lastRecordingDate;
    }

    /**
     * Deserializes a new RecordingSettings from a byte array
     *
     * @param serialization
     */
    public RecordingSettings(byte[] serialization) {
        int i = 0;
        int fecha = readIntFromLittleEndian(serialization, 0);
        i += 4;
        setGain(serialization[i++]); //5
        int clockDivider = serialization[i++]; //6
        int acquisitionCycles = serialization[i++]; //7
        int oversampleRate = serialization[i++];//8
        int sampleRate = readIntFromLittleEndian(serialization, i);//9
        i += 4;
        int sampleRateDivider = serialization[i++];
        int visibleSampleRate = sampleRate/sampleRateDivider;
        setSampleRate(visibleSampleRate);
        setSleepDuration(readShortFromLittleEndian(serialization, i));
        i += 2;
        setRecordDuration(readShortFromLittleEndian(serialization, i));
        i += 2;
        setLedEnabled(serialization[i++] != 0);
        int timePeriodsLength = serialization[i++];
        setLocalTime(serialization[39]!=0);
        ArrayList<TimePeriods> tp = new ArrayList<TimePeriods>(timePeriodsLength + 1);

        for (int j = 0; j < timePeriodsLength; j++) {
            int tzOffset = isLocalTime()?calculateTimezoneOffsetMins():0;
            int intStartMins = readShortFromLittleEndian(serialization, i)+tzOffset;
            i += 2;
            int intEndMins = readShortFromLittleEndian(serialization, i)+tzOffset;
            i += 2;
            tp.add(j, new TimePeriods(intStartMins, intEndMins, isLocalTime()));
        }
        setTimePeriods(tp);
        for (int k = 0; k < MAX_PERIODS - timePeriodsLength; k++) {
            i += 4;
        }
        i++;
        setLowVoltageCutoffEnabled(serialization[i++] != 0);
        setBatteryLevelCheckEnabled(serialization[i++] == 0);
        i++;
        setDutyEnabled(serialization[i++] == 0);
        Date startRecordingDate=null;
        if(isLocalTime()){
            Long lStartRecordingDate = readMillisFromByteArray(serialization,i);
            if(lStartRecordingDate!=null) {
                startRecordingDate = new DateTime(lStartRecordingDate, DateTimeZone.UTC).toLocalDate().toDate();
            }
        } else {
         startRecordingDate = readDateFromByteArray(serialization, i);
        }
        setFirstRecordingDate(startRecordingDate);
        i += 4;
        Date endRecordingDate=null;
        if(isLocalTime()){
            Long lEndRecordingDate = readMillisFromByteArray(serialization,i);
            if(lEndRecordingDate!=null) {
                endRecordingDate = new DateTime(lEndRecordingDate, DateTimeZone.UTC).toLocalDate().toDate();
            }
        } else {
            endRecordingDate = readDateFromByteArray(serialization, i);
        }
        setLastRecordingDate(endRecordingDate);
        i += 4;
        int lowFil = readShortFromLittleEndian(serialization, i);
        i += 2;
        int hiFil = readShortFromLittleEndian(serialization, i);
        i += 2;
        if(lowFil==0 && higherFilter==0){
            setPassFiltersEnabled(false);
        } else if(lowFil==UINT16_MAX && higherFilter== UINT16_MAX){
            Log.d(TAG,"lowFil==UINT16_MAX && highFil=UINT16_MAX" );
            setPassFiltersEnabled(true);
            setFilterType(FilterType.BAND);
            setLowerFilter(0);
            setHigherFilter(24000);
        } else if (lowFil == UINT16_MAX) {
            Log.d(TAG,"lowFil==UINT16_MAX");
            setPassFiltersEnabled(true);
            setFilterType(FilterType.LOW);
            setHigherFilter(hiFil*100);
            setLowerFilter(0);
        } else if (hiFil == UINT16_MAX ) {
            Log.d(TAG, "hiFil==UINT16_MAX");
            setPassFiltersEnabled(true);
            setFilterType(FilterType.HIGH);
            setHigherFilter(24000);
            setLowerFilter(lowFil*100);
        } else {
            setPassFiltersEnabled(true);
            setFilterType(FilterType.BAND);
            setLowerFilter(lowFil*100);
            setHigherFilter(hiFil*100);
        }


        setPassFiltersEnabled(!(getLowerFilter() == 0 && getHigherFilter() == 0));
        setAmplitudeThreshold(readShortFromLittleEndian(serialization, i));
        setAmplitudeThresholdingEnabled(getAmplitudeThreshold() > 0);
    }

    /**
     * Convert a RecordingSettings object to a byte array representation for AudioMoth
     * AM uses this byte array to define the operation modes in runtime.
     * In the firmware, when the device receives the config data via USB in a form of byte array, it
     * copies to a C structure called configSettings_t, and returns it back to the USB connection
     *
     * @return a byte array
     * @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Firmware-Basic/blob/1.4.4/main.c#L954></a>
     * <p>
     * Note: the javascript cousin of this code checks the presence in the ui of < 3 sample rate
     * settings to determine the semantic version, we will ignore this, after comparing
     * the firmware 1.4.4 to 1.3.0, the struct in the firmware differs from several fields.
     * If it is lower to 1.4.4 it only have 3 posible configurations
     * @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Configuration-App/blob/master/uiIndex.js#L217"></a>
     * an also @see <a href="https://github.com/OpenAcousticDevices/AudioMoth-Configuration-App/blob/master/constants.js#L83"></a>
     */
    public byte[] serializeToBytes() {
        Configurations config;
        byte[] serialization = new byte[58];
        int unixTime = (int) (System.currentTimeMillis() / 1000);
        int index = 0;
        writeIntToLittleEndian(serialization, index, unixTime);
        index += 4;
        serialization[index++] = getGain();
        config = Configurations.getConfig(getSampleRate() / 1000, getDeviceInfo().isOlderSemanticVersion());
        serialization[index++] = config.getClockDivider();
        serialization[index++] = config.getAcquisitionCycles();
        serialization[index++] = config.getOversampleRate();
        writeIntToLittleEndian(serialization, index, config.getSampleRate());
        index += 4;
        serialization[index++] = config.getSampleRateDivider();
        writeShortToLittleEndian(serialization, index, (short) getSleepDuration());
        index += 2;
        writeShortToLittleEndian(serialization, index, (short) getRecordDuration());
        index += 2;
        serialization[index++] = (byte) (isLedEnabled() ? 1 : 0);
        Collections.sort(timePeriods);
        serialization[index++] = (byte) timePeriods.size();
        for (int i = 0; i < timePeriods.size(); i++) {
            short minOffset = (short) (isLocalTime()?calculateTimezoneOffsetMins():0);
            writeShortToLittleEndian(serialization, index, (short) ((short) timePeriods.get(i).getStartMins()-minOffset));
            index += 2;
            writeShortToLittleEndian(serialization, index, (short) ((short) timePeriods.get(i).getEndMins()-minOffset));
            index += 2;
        }
        for (int i = 0; i < MAX_PERIODS - timePeriods.size(); i++) {
            writeShortToLittleEndian(serialization, index, (short) 0);
            index += 2;
            writeShortToLittleEndian(serialization, index, (short) 0);
            index += 2;
        }
        serialization[index++] = (byte) (isLocalTime() ? calculateTimezoneOffsetHours() : 0);
        serialization[index++] = (byte) (isLowVoltageCutoffEnabled() ? 1 : 0);
        serialization[index++] = (byte) (isBatteryLevelCheckEnabled() ? 0 : 1);
        /* For non-integer timezones */
        serialization[index++] = (byte) (isLocalTime() ? (calculateTimezoneOffsetMins() % 60) : 0);

        /* Duty cycle disabled (default value = 0) */
        serialization[index++] = (byte) (isDutyEnabled() ? 0 : 1);

        /* Start/stop dates */
       /* DateTimeZone tzLocal = DateTimeZone.getDefault();
        DateTime dtUTC = new DateTime(DateTimeZone.UTC);
        DateTime dtLocal = new DateTime();

        long instant = dtLocal.getMillis();
        long timezoneOffset = tzLocal.getOffset(instant)/60000;

        int dayDiff = dtLocal.getDayOfMonth() - dtUTC.getDayOfMonth();*/


        if(getFirstRecordingDate()!=null) {
            long earliestRecordingTime = 0;
            DateTime dtUTC = new LocalDateTime(getFirstRecordingDate().getTime()).toDateTime(DateTimeZone.UTC);
            earliestRecordingTime =dtUTC.getMillis() / 1000L;
            writeLongToLittleEndian(serialization, index, earliestRecordingTime);
        }
        index += 4;
        if(getLastRecordingDate()!=null) {
            long lastRecordingTime = 0;

            DateTime dtUTC = new LocalDateTime(getLastRecordingDate().getTime()).toDateTime(DateTimeZone.UTC);
            dtUTC = dtUTC.withTime(23,59,59,999);
            /* Make latestRecordingTime timestamp inclusive by setting it to the end of the chosen day */
            lastRecordingTime = dtUTC.getMillis() / 1000L;
            writeLongToLittleEndian(serialization, index, lastRecordingTime);
        }
        index += 4;
        /* Filter settings */
        int lowerFilter =0;
        int higherFilter = 0;
        if (isPassFiltersEnabled()) {
            switch (getFilterType()) {
                case LOW:
                    lowerFilter=UINT16_MAX;
                    higherFilter =(getHigherFilter() / 100);
                    break;
                case HIGH:
                    lowerFilter=(getLowerFilter() / 100);
                    higherFilter=(UINT16_MAX);
                    break;
                case BAND:
                    lowerFilter=(getLowerFilter() / 100);
                    higherFilter=(getHigherFilter() / 100);
                    break;

            }
        }
        writeShortToLittleEndian(serialization, index, (short) lowerFilter);
        index += 2;
        writeShortToLittleEndian(serialization, index, (short) higherFilter);
        index += 2;
        /* CMV settings */
        writeShortToLittleEndian(serialization, index, isAmplitudeThresholdingEnabled() ? (short) getAmplitudeThreshold() : (short) 0);
        index += 2;
        return serialization;
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
        if(passFiltersEnabled) {
            if (lowerFilter != that.lowerFilter) return false;
            if (higherFilter != that.higherFilter) return false;
            if (filterType != that.filterType) return false;
        }
        if (amplitudeThresholdingEnabled != that.amplitudeThresholdingEnabled) return false;
        if(amplitudeThresholdingEnabled) {
            if (amplitudeThreshold != that.amplitudeThreshold) return false;
        }
        if (!timePeriods.containsAll(that.timePeriods)) return false;
        if(firstRecordingDate!=null && !firstRecordingDate.equals(that.firstRecordingDate)) return false;
        if(lastRecordingDate!=null && !lastRecordingDate.equals(that.lastRecordingDate)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 31 * (ledEnabled ? 1 : 0);
        if (timePeriods != null)
            result = 31 * result + timePeriods.hashCode();
        result = 31 * result + (lowVoltageCutoffEnabled ? 1 : 0);
        result = 31 * result + (batteryLevelCheckEnabled ? 1 : 0);
        result = 31 * result + sampleRate;
        result = 31 * result + (int) gain;
        result = 31 * result + recordDuration;
        result = 31 * result + sleepDuration;
        result = 31 * result + (localTime ? 1 : 0);
        result = 31 * result + (dutyEnabled ? 1 : 0);
        result = 31 * result + (passFiltersEnabled ? 1 : 0);
        if (filterType != null)
            result = 31 * result + filterType.hashCode();
        result = 31 * result + lowerFilter;
        result = 31 * result + higherFilter;
        result = 31 * result + (amplitudeThresholdingEnabled ? 1 : 0);
        result = 31 * result + amplitudeThreshold;
        if (firstRecordingDate != null)
            result = 31 * result + firstRecordingDate.hashCode();
        if (lastRecordingDate != null)
            result = 31 * result + lastRecordingDate.hashCode();
        return result;
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
        int tzOffset = 0;

            TimeZone tzdata = TimeZone.getDefault();
            int tzInt = tzdata.getOffset(Calendar.ZONE_OFFSET);
            tzOffset = tzInt / 60000;

        return tzOffset;
    }

    private int calculateTimezoneOffsetHours() {
        int tzOffset =  calculateTimezoneOffsetMins()/60;
        return tzOffset;
    }


    public ArrayList<TimePeriods> getTimePeriods() {
        return timePeriods;
    }

    public void setTimePeriods(ArrayList<TimePeriods> timePeriods) {
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

    public int getRecordDuration() {
        return recordDuration;
    }

    public void setRecordDuration(int recordDuration) {
        this.recordDuration = recordDuration;
    }

    public int getSleepDuration() {
        return sleepDuration;
    }

    public void setSleepDuration(int sleepDuration) {
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

    public int getAmplitudeThreshold() {
        return amplitudeThreshold;
    }

    public void setAmplitudeThreshold(int amplitudeThreshold) {
        this.amplitudeThreshold = amplitudeThreshold;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public Date getFirstRecordingDate() {
        return firstRecordingDate;
    }

    public void setFirstRecordingDate(Date firstRecordingDate) {
        this.firstRecordingDate = firstRecordingDate;
    }

    public Date getLastRecordingDate() {
        return lastRecordingDate;
    }

    public void setLastRecordingDate(Date lastRecordingDate) {
        this.lastRecordingDate = lastRecordingDate;
    }

    public boolean isFirstRecordingEnable() {
        return firstRecordingEnable;
    }

    public void setFirstRecordingEnable(boolean firstRecordingEnable) {
        this.firstRecordingEnable = firstRecordingEnable;
    }

    public boolean isLastRecordingEnable() {
        return lastRecordingEnable;
    }

    public void setLastRecordingEnable(boolean lastRecordingEnable) {
        this.lastRecordingEnable = lastRecordingEnable;
    }
}


