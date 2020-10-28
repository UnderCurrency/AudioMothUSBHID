package com.undercurrency.audiomoth.usbhid.model;

import java.util.Date;

public class DateSettings {

    private Date date;

    public DateSettings(Date date){
        this.date = date;
    }

    public Date getDate(){
        return this.date;
    }

    public byte[] serializeToBytes(){
        int timestamp = (int)getDate().getTime()/1000;
        byte[] bytes = new byte[4];
        bytes[3] = (byte) ((timestamp>>24) & 0xFF);
        bytes[2] = (byte) ((timestamp>>16) & 0xFF);
        bytes[1] = (byte) ((timestamp>>8) & 0xFF);
        bytes[0] = (byte) (timestamp & 0xFF);
        return bytes;
    }
}
