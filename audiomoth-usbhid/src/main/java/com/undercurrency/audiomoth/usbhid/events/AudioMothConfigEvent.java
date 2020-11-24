package com.undercurrency.audiomoth.usbhid.events;

import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

public class AudioMothConfigEvent {
    private RecordingSettings rs;

    public RecordingSettings getRecordingSettings(){
        return this.rs;
    }

    public AudioMothConfigEvent(RecordingSettings rs){
        this.rs = rs;
    }
}
