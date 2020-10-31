package com.undercurrency.audiomoth.usbhid.model;

import java.util.Date;
import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.writeDateToByteArray;
/**
 * The DateSettings class represents a value holder
 */
public class DateSettings {

    private Date date;

    public DateSettings(byte[] buffer){


    }
    public DateSettings(Date date){
        this.date = date;
    }

    public Date getDate(){
        return this.date;
    }

    public byte[] serializeToBytes(){
        return writeDateToByteArray(getDate());

    }
}
