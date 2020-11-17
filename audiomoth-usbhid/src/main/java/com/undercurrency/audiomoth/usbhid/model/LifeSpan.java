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

public class LifeSpan {
    private double totalCompleteRecordingCount;
    private double truncatedRecordingCount;
    private double truncatedRecordingTime;
    private double totalRecLength;
    private float dailyEnergyConsumption;

    public LifeSpan(double totalCompleteRecordingCount, double truncatedRecordingCount, double truncatedRecordingTime, double totalRecLength, float dailyEnergyConsumption) {
        this.totalCompleteRecordingCount = totalCompleteRecordingCount;
        this.truncatedRecordingCount = truncatedRecordingCount;
        this.truncatedRecordingTime = truncatedRecordingTime;
        this.totalRecLength = totalRecLength;
        this.dailyEnergyConsumption = dailyEnergyConsumption;
    }


    public double getTotalCompleteRecordingCount() {
        return totalCompleteRecordingCount;
    }

    public void setTotalCompleteRecordingCount(double totalCompleteRecordingCount) {
        this.totalCompleteRecordingCount = totalCompleteRecordingCount;
    }

    public double getTruncatedRecordingCount() {
        return truncatedRecordingCount;
    }

    public void setTruncatedRecordingCount(double truncatedRecordingCount) {
        this.truncatedRecordingCount = truncatedRecordingCount;
    }

    public double getTruncatedRecordingTime() {
        return truncatedRecordingTime;
    }

    public void setTruncatedRecordingTime(double truncatedRecordingTime) {
        this.truncatedRecordingTime = truncatedRecordingTime;
    }

    public double getTotalRecLength() {
        return totalRecLength;
    }

    public void setTotalRecLength(double totalRecLength) {
        this.totalRecLength = totalRecLength;
    }

    public float getDailyEnergyConsumption() {
        return dailyEnergyConsumption;
    }

    public void setDailyEnergyConsumption(float dailyEnergyConsumption) {
        this.dailyEnergyConsumption = dailyEnergyConsumption;
    }
}
