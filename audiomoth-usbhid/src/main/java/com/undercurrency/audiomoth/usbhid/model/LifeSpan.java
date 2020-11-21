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
    int energyUsed=0;
    String formatFileSize="";
    int totalRecCount=0;
    int completeRecCount=0;
    int upToFile=0;
    String upToFileSize="MB";
    int totalSize=0;
    int scheduleLength=0;


    public LifeSpan(int energyUsed, String formatFileSize, int totalRecCount, int completeRecCount, int upToFile, String upToFileSize, int totalSize, int scheduleLength) {
        this.energyUsed = energyUsed;
        this.formatFileSize = formatFileSize;
        this.totalRecCount = totalRecCount;
        this.completeRecCount = completeRecCount;
        this.upToFile = upToFile;
        this.upToFileSize = upToFileSize;
        this.totalSize = totalSize;
        this.scheduleLength = scheduleLength;
    }

    public int getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(int energyUsed) {
        this.energyUsed = energyUsed;
    }

    public String getFormatFileSize() {
        return formatFileSize;
    }

    public void setFormatFileSize(String formatFileSize) {
        this.formatFileSize = formatFileSize;
    }

    public int getTotalRecCount() {
        return totalRecCount;
    }

    public void setTotalRecCount(int totalRecCount) {
        this.totalRecCount = totalRecCount;
    }

    public int getCompleteRecCount() {
        return completeRecCount;
    }

    public void setCompleteRecCount(int completeRecCount) {
        this.completeRecCount = completeRecCount;
    }

    public int getUpToFile() {
        return upToFile;
    }

    public void setUpToFile(int upToFile) {
        this.upToFile = upToFile;
    }

    public String getUpToFileSize() {
        return upToFileSize;
    }

    public void setUpToFileSize(String upToFileSize) {
        this.upToFileSize = upToFileSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getScheduleLength() {
        return scheduleLength;
    }

    public void setScheduleLength(int scheduleLength) {
        this.scheduleLength = scheduleLength;
    }

    @Override
    public String toString() {
        return "LifeSpan{" +
                "energyUsed=" + energyUsed +
                ", formatFileSize='" + formatFileSize + '\'' +
                ", totalRecCount=" + totalRecCount +
                ", completeRecCount=" + completeRecCount +
                ", upToFile=" + upToFile +
                ", upToFileSize='" + upToFileSize + '\'' +
                ", totalSize=" + totalSize +
                ", scheduleLength=" + scheduleLength +
                '}';
    }
}
