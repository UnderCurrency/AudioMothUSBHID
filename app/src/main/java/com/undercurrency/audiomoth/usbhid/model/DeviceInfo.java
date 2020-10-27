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

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * DeviceInfo a pojo to hold the basic device identification for an AM device
 */
public class DeviceInfo {
    private static final String SEMANTIC_VERSION="1.4.4";
    private String deviceId;
    private String firmwareVersion;
    private String  battery;
    private Date date;



    public DeviceInfo(byte[] fromArray){
        date = convertBytesToDate(fromArray,1);
        deviceId = convertBytesToString(fromArray,1+4);
        battery = convertBytesToBatteryState(fromArray, 1+4+8);
        firmwareVersion= convertBytesToFirmwareVersion(fromArray,1 + 4 + 8 + 1);
    }



    public DeviceInfo(String deviceId, String firmwareVersion, String battery, Date date) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.battery = battery;
        this.date = date;
    }

    /**
     * Determine if the firmwareVersion is older than the official latest version
     * @return true if the current version is minor than SEMANTIC_VERSION, false otherwise
     */
    public boolean isOlderSemanticVersion () {

        String[] aVersionArr, bVersionArr;
        Integer aVersionNum, bVersionNum;
        aVersionArr = firmwareVersion.split(".");
        bVersionArr = SEMANTIC_VERSION.split(".");

        for (int i = 0; i < aVersionArr.length; i++) {
            aVersionNum = Integer.parseInt(aVersionArr[i]);
            bVersionNum = Integer.parseInt(bVersionArr[i]);
            if (aVersionNum > bVersionNum) {
                return false;
            } else if (aVersionNum < bVersionNum) {
                return true;
            }
        }
        return false;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private Date convertBytesToDate(byte[]  buffer, int offset){
        long unixTimestamp = (buffer[offset] & 0xFF) + ((buffer[offset + 1] & 0xFF) << 8) + ((buffer[offset + 2] & 0xFF) << 16) + ((buffer[offset + 3] & 0xFF) << 24);
        return new Date(unixTimestamp * 1000);
    }

    private String convertBytesToString(byte[] buffer, int offset){
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[buffer.length * 2];
        byte[] array = Arrays.copyOfRange(buffer,offset,offset+8);
        byte[] reverse = new byte[array.length];
        for(int i=array.length-1;i>=0;i--){
            reverse[array.length - i - 1]= array[i];
        }
        for (int j = 0; j < reverse.length; j++) {
            int v = reverse[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String convertBytesToBatteryState(byte[] buffer, int offset){
        DecimalFormat df = new DecimalFormat("#.#");
        byte batterySate = buffer[offset];
        switch (batterySate) {
            case 0: return "< 3.6V";
            case 15 : return "<4.9V";
            default: {
                return df.format(3.5+batterySate/10)+"V";
            }
        }
    }

    private String convertBytesToFirmwareVersion(byte[] fromArray, int i) {
      return  Integer.toString(fromArray[i + 1])+"."+Integer.toString(fromArray[i + 2])+"."+Integer.toString(fromArray[i + 3]);
    }
}
