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
 * DeviceInfo a pojo to hold the basic device identification for an AM device
 */
public class DeviceInfo {
    private static final String SEMANTIC_VERSION="1.4.4";
    private int deviceId;
    private String firmwareVersion;
    private float  battery;

    public DeviceInfo(int deviceId, String firmwareVersion, float battery) {
        this.deviceId = deviceId;
        this.firmwareVersion = firmwareVersion;
        this.battery = battery;
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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (deviceId != that.deviceId) return false;
        if (Float.compare(that.battery, battery) != 0) return false;
        return firmwareVersion.equals(that.firmwareVersion);
    }

    @Override
    public int hashCode() {
        int result = deviceId;
        result = 31 * result + firmwareVersion.hashCode();
        result = 31 * result + (battery != +0.0f ? Float.floatToIntBits(battery) : 0);
        return result;
    }
}
