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

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.readDateFromByteArray;

/**
 * DeviceInfo a pojo to hold the basic device identification for an AM device
 */
public class DeviceInfo implements Serializable {

    private static final long serialVersionUID = 8799656478674716641L;

    private static final String SEMANTIC_VERSION="1.4.4";
    private String deviceId;
    private String firmwareVersion;
    private String  battery;
    private String date;


    /**
     * Create DeviceInfo from byte array
     * @param fromArray
     */
    public DeviceInfo(byte[] fromArray){
        date = readDate(fromArray,1);
        deviceId = readDeviceId(fromArray,1+4);
        battery = readBatteryStatus(fromArray, 1+4+8);
        firmwareVersion= readFirmware(fromArray,1 + 4 + 8 + 1);
    }



    public DeviceInfo(String deviceId, String firmwareVersion, String battery, Date date) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.battery = battery;
        SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy hh:mm:ss Z");
        this.date = sdf.format(date);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getRealDate(){
        SimpleDateFormat sdf= new SimpleDateFormat("hh:mm:ss dd/MM/yyyy Z");
        Date realDate = null;
        try {
            realDate = sdf.parse(getDate());
        } catch (ParseException e) {
          return null;
        }
        return realDate;
    }

    private String readDate(byte[]  buffer, int offset){
       SimpleDateFormat sdf= new SimpleDateFormat("hh:mm:ss dd/MM/yyyy Z");
        Date deviceDate = readDateFromByteArray(buffer,offset);
        if(deviceDate == null) return null;
        return sdf.format(deviceDate);
    }

    private String readDeviceId(byte[] buffer, int offset){
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[8 * 2];
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

    private String readBatteryStatus(byte[] buffer, int offset){
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

    private String readFirmware(byte[] fromArray, int offset) {
      return  Integer.toString(fromArray[offset ] & 0xFF)+"."+Integer.toString(fromArray[offset + 1]& 0xFF)+"."+Integer.toString(fromArray[offset + 2]& 0xFF);
    }
}
