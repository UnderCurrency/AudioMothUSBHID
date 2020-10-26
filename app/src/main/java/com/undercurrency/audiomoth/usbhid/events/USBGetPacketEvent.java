package com.undercurrency.audiomoth.usbhid.events;

public class USBGetPacketEvent {
    private final byte[] data;

    public USBGetPacketEvent(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
