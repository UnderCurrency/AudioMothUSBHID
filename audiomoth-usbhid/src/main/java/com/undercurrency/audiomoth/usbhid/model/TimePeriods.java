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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * TimePeriods a class to represent time recording intervals for AM device
 */
public class TimePeriods implements Comparable<TimePeriods>, Serializable {

    private static final long serialVersionUID = 8799656478674716611L;
    private int startMins;
    private int endMins;
    private transient boolean localTime=false;

    public TimePeriods(int startMins, int endMins) {
        this.startMins = startMins;
        this.endMins = endMins;
    }

    public TimePeriods(int startMins, int endMins, boolean localTime) {
        this.startMins = startMins;
        this.endMins = endMins;
        this.localTime = localTime;
    }

    public int getStartMins() {
        return startMins;
    }

    public void setStartMins(int startMins) {
        this.startMins = startMins;
    }

    public int getEndMins() {
        return endMins;
    }

    public void setEndMins(int endMins) {
        this.endMins = endMins;
    }

    public boolean isLocalTime() {
        return localTime;
    }

    public void setLocalTime(boolean localTime) {
        this.localTime = localTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePeriods that = (TimePeriods) o;

        if (startMins != that.startMins) return false;
        return endMins == that.endMins;
    }

    @Override
    public int hashCode() {
        int result = startMins;
        result = 31 * result + endMins;
        return result;
    }

    @Override
    public int compareTo(TimePeriods o) {
        return this.getStartMins() - o.getStartMins();
    }

    /**
     * Format a TimePeriod timezone aware
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int startMins = getStartMins();
        int endMins = getEndMins();
        int offset = 0;
        int minOffset = 0;
        String utc = "(UTC)";
        if(localTime) {
            Calendar now = Calendar.getInstance();
            TimeZone timeZone = now.getTimeZone();
            offset = timeZone.getRawOffset() / 36000;
            minOffset = timeZone.getRawOffset()/ 60000;
            utc =  String.format(" (UTC%d)",offset);
            startMins +=  minOffset;
            endMins += minOffset;
            startMins = startMins <0 ? startMins+1440:startMins;
            endMins = endMins<0 ? endMins+1440:endMins;
        }
        sb.append(fromMinToHrs(startMins));
        sb.append(" - ");
        sb.append(fromMinToHrs(endMins));
        sb.append(utc);
        return sb.toString();
    }

    /**
     * Returns a string representation of a Time Period
     * if localTime is set, it adds the Timezone Offset
     *
     * @param aTimeInMin
     * @return
     */
    private String fromMinToHrs(int aTimeInMin){
        int hours = aTimeInMin / 60; //since both are ints, you get an int
        int minutes = aTimeInMin % 60;
        return String.format("%d:%02d", hours, minutes);
    }
}
