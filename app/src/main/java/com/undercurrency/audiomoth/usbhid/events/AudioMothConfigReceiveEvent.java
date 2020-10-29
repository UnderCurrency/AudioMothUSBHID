package com.undercurrency.audiomoth.usbhid.events;

import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

public class AudioMothConfigReceiveEvent {
    private RecordingSettings rs;

    public RecordingSettings getRecordingSettings(){
        return this.rs;
    }

    public AudioMothConfigReceiveEvent(RecordingSettings rs){
        this.rs = rs;
    }
}
