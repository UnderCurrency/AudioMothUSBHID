package com.undercurrency.audiomoth.usbhid.model;

public enum AudioMothOperations {

    USB_MSG_TYPE_GET_TIME (0x01),
    USB_MSG_TYPE_SET_TIME(0x02),
    USB_MSG_TYPE_GET_UID ( 0x03),
    USB_MSG_TYPE_GET_BATTERY ( 0x04),
    USB_MSG_TYPE_GET_APP_PACKET (0x05),
    USB_MSG_TYPE_SET_APP_PACKET (0x06),
    USB_MSG_TYPE_GET_FIRMWARE_VERSION (0x07),
    USB_MSG_TYPE_GET_FIRMWARE_DESCRIPTION(0x08),
    USB_MSG_TYPE_QUERY_BOOTLOADER(0x09),
    USB_MSG_TYPE_SWITCH_TO_BOOTLOADER(0x0A)

    ;
    AudioMothOperations(int opcode){
        this.opcode = (byte) opcode;
    }
    private final byte opcode;

    public byte getOpcode(){
        return opcode;
    }
}
