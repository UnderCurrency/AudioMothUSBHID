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

    long totalRecCount=0;
    boolean plural= false;
    boolean upTo=false;
    String fileSizeInUnits ="0 MB";
    float energyUsed=0.0F;

    public LifeSpan(){}

    public LifeSpan(long totalRecCount, boolean plural, boolean upTo, String fileSizeInUnits, float energyUsed) {
        this.totalRecCount = totalRecCount;
        this.plural = plural;
        this.upTo = upTo;
        this.fileSizeInUnits = fileSizeInUnits;
        this.energyUsed = energyUsed;
    }


    private static float getStartCurrent(int rateIndex) {
        float startCurrent = Configurations.getConfig(rateIndex, false).getStartCurrent();
        return startCurrent;
    }

    private static float getRecordCurrent(int rateIndex) {
        float recordCurrent = Configurations.getConfig(rateIndex, false).getRecordCurrent();
        return recordCurrent;
    }

    @Override
    public String toString() {
        return "LifeSpan{" +
                "totalRecCount=" + totalRecCount +
                ", plural=" + plural +
                ", upTo=" + upTo +
                ", fileSizeInUnits='" + fileSizeInUnits + '\'' +
                ", energyUsed=" + energyUsed +
                '}';
    }

    public long getTotalRecCount() {
        return totalRecCount;
    }

    public boolean isPlural() {
        return plural;
    }

    public boolean isUpTo() {
        return upTo;
    }

    public String getFileSizeInUnits() {
        return fileSizeInUnits;
    }

    public float getEnergyUsed() {
        return energyUsed;
    }

    private static byte getSampleRateDivider(int rateIndex) {
        byte sampleRateDivider = Configurations.getConfig(rateIndex, false).getSampleRateDivider();
        return sampleRateDivider;
    }

    public static LifeSpan getLifeSpan(RecordingSettings rs) {
        long MAX_WAV_LENGTH =4294966806L;
        int START_UP_TIME = 2;
        float SLEEP_ENERGY = 0.125F;
        //out variables
        float energyUsed = 0;
        String formatFileSize = "";
        long totalRecCount = 0;
        long completeRecCount = 0;
        boolean upToFile = false;
        boolean upToTotal = false;
        String upToFileSize = "MB";
        long totalSize = 0;
        long scheduleLength = 0;
        long upToSize=0;
        long maxFileSize =0;
        long recordingSize=0;
        boolean sizeWarn = false;

        //Auxiliaries
        long truncatedRecCount =0;
        long truncatedRecTime =0;
        long totalRecLength = 0;
        long recLength = 0;
        long recSize=0;
        long truncatedRecordingSize=0;
        long maxLength =0;
        int energyPrecision=0;

        upToFile = rs.isAmplitudeThresholdingEnabled();
        upToTotal = rs.isAmplitudeThresholdingEnabled();

        /* Calculate amount of energy used both recording a sleeping over the course of a day */
        energyUsed = Math.min(86400 - totalRecCount*START_UP_TIME,totalRecLength)* getRecordCurrent(rs.getSampleRate())/3600;
        energyUsed += totalRecCount*START_UP_TIME*getRecordCurrent(rs.getSampleRate())/3600;
        energyUsed += Math.max(0,86400-totalRecCount*START_UP_TIME-totalRecLength)*SLEEP_ENERGY/3600;
        energyPrecision = energyUsed > 100 ? 10 : energyUsed > 50 ? 5 : energyUsed > 20 ? 2 : 1;
        energyUsed = Math.round(energyUsed/energyPrecision)*energyPrecision;

        if (rs.getTimePeriods().size() > 0) {
            if(rs.isDutyEnabled()){
                long[] countResponse = getDailyCount(rs);
                completeRecCount = countResponse[0];
                truncatedRecCount = countResponse[1];
                truncatedRecTime = countResponse[2];
                totalRecLength = (completeRecCount * recLength) + truncatedRecTime;

                /* Calculate the size of a days worth of recordings */
                recSize = rs.getSampleRate() / getSampleRateDivider(rs.getSampleRate()) * 2 * recLength;
                truncatedRecordingSize = (truncatedRecTime *  rs.getSampleRate() / getSampleRateDivider(rs.getSampleRate()) * 2);
                totalSize = (recSize * completeRecCount) + truncatedRecordingSize;
            } else{
                completeRecCount = rs.getTimePeriods().size();
                truncatedRecCount=0;
                truncatedRecTime = 0;
                totalRecLength=0;
                maxLength = 0;

                for(int i =0; i<completeRecCount; i++){
                    TimePeriods period = rs.getTimePeriods().get(i);
                    long length = period.getEndMins() - period.getStartMins();
                    if(i >0 && !rs.isAmplitudeThresholdingEnabled()){
                        TimePeriods prevPeriod = rs.getTimePeriods().get(i - 1);
                        long prevLenght = prevPeriod.getEndMins() - prevPeriod.getStartMins();
                        if(length!=prevLenght){
                            upToFile = true;
                        }
                    }
                    totalRecLength +=length;
                    maxLength = (length> maxLength)?length:maxLength;
                }
                totalRecLength *= 60;
                totalSize = rs.getSampleRate()/getSampleRateDivider(rs.getSampleRate())*2*totalRecLength;

            }
            totalRecCount = completeRecCount + truncatedRecCount;

            if(completeRecCount > 1){
                if(rs.isDutyEnabled()){
                    upToSize = recSize;
                } else {
                    maxFileSize = rs.getSampleRate()/ getSampleRateDivider(rs.getSampleRate())*2*maxLength*60;
                    upToSize= maxFileSize;
                }
            }
            recordingSize = (completeRecCount>1)?upToSize:totalSize;

            if(recordingSize>MAX_WAV_LENGTH){
                sizeWarn = true;
            }

            return new LifeSpan(totalRecCount,totalRecCount>1,completeRecCount>1?upToFile:upToTotal,completeRecCount>1?formatFileSize(upToSize):formatFileSize(totalSize),energyUsed);

        }

        return new LifeSpan();

    }


    private static String formatFileSize(long fileSize){
        int calcFileSize= Math.round(fileSize / 1000);
        if (calcFileSize < 10000) {
            return calcFileSize + " kB";
        }
        calcFileSize = Math.round(calcFileSize / 1000);
        if (calcFileSize < 10000) {
            return calcFileSize + " MB";
        }
        calcFileSize = Math.round(calcFileSize / 1000);
        return fileSize + "GB";
    }

    private static long[] getDailyCount(RecordingSettings rs) {
        long[] data = new long[3];
        long periodSecs = 0;
        long completeRecCount = 0;
        long totalRecLength = 0;
        long timeRemaining = 0;

        /* Total number of recordings of the intended length */
        long totalCompleteRecCount = 0;
        /* Total number of recordings which could not be the intended length, so have been truncated */
        long truncatedRecCount = 0;
        /* Total length of all truncated files in seconds */
        long truncatedRecTime = 0;
        for (int i = 0; i < rs.getTimePeriods().size(); i++) {
            periodSecs = (rs.getTimePeriods().get(i).getEndMins() - rs.getTimePeriods().get(i).getStartMins()) * 60;
            completeRecCount = (int) Math.floor(periodSecs / (rs.getRecordDuration() + rs.getSleepDuration()));
            /* Check if a truncated recording will fit in the rest of the period */
            totalRecLength = completeRecCount * (rs.getRecordDuration() + rs.getSleepDuration());
            timeRemaining = periodSecs - totalRecLength;
            if (timeRemaining > 0) {
                if (timeRemaining >= rs.getRecordDuration()) {
                    completeRecCount += 1;
                } else {
                    truncatedRecTime += timeRemaining;
                    truncatedRecCount += 1;
                }
            }
            totalCompleteRecCount += completeRecCount;
        }

        data[0] = totalCompleteRecCount;
        data[1] = truncatedRecCount;
        data[2] = truncatedRecTime;
        return data;
    }

}
