package com.undercurrency.audiomoth.usbhid.events;

import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;

public class AudioMothPacketReceiveEvent {

    private DeviceInfo deviceInfo;
    public DeviceInfo getDeviceInfo(){
        return this.deviceInfo;
    }
    public AudioMothPacketReceiveEvent(DeviceInfo deviceInfo){
        this.deviceInfo = deviceInfo;
    }
}
