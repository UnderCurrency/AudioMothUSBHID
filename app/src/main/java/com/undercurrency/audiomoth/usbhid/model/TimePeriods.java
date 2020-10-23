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
 * TimePeriods a class to represent time recording intervals for AM device
 */
public class TimePeriods implements Comparable<TimePeriods> {
  int startMins;
  int endMins;

    public TimePeriods(int startMins, int endMins) {
        this.startMins = startMins;
        this.endMins = endMins;
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
        return this.getStartMins()-o.getStartMins();
    }
}
