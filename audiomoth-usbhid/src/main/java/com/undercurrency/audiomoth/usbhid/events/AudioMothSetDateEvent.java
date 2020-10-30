package com.undercurrency.audiomoth.usbhid.events;

import android.provider.MediaStore;

import java.util.Date;

public class AudioMothSetDateEvent {
    private static Date date;

    public AudioMothSetDateEvent(Date date){
        this.date = date;
    }

    public Date getDate(){
        return this.date;
    }
}
