package com.undercurrency.audiomoth.testapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.undercurrency.audiomoth.usbhid.USBHidTool;
import com.undercurrency.audiomoth.usbhid.events.PrepareDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.SelectDeviceEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataSendEvent;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Button btnDispositivo;
    private Button btnConfigurar;
    protected EventBus eventBus;
    private Intent usbhidService;
    private RecordingSettings rs;
    private DeviceInfo deviceInfo;
    private boolean deviceSelected=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
        } catch (EventBusException e) {
            eventBus = EventBus.getDefault();
        }
        initUI();
    }

    private void initUI(){
        btnDispositivo = (Button) findViewById(R.id.btnDispositivo);
        btnDispositivo.setOnClickListener(this);
        btnConfigurar = (Button) findViewById(R.id.btnConfigurar);
        btnConfigurar.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService();
        eventBus.register(this);
    }

    @Override
    protected void onStop() {
        eventBus.unregister(this);
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        if (v ==  btnConfigurar) {
            if(deviceSelected) {
                eventBus.post(new USBDataSendEvent(rs.serializeToBytes()));
            }
        } else if (v == btnDispositivo) {
            eventBus.post(new PrepareDevicesListEvent());
        }
    }

    private void startService(){
        usbhidService = new Intent(this, USBHidTool.class);
    }


    public void onEvent(ShowDevicesListEvent event) {
        showListOfDevices(event.getCharSequenceArray());
    }

    void showListOfDevices(CharSequence devicesName[]) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (devicesName.length == 0) {
            builder.setTitle(getString(R.string.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE));
        } else {
            builder.setTitle(getString(R.string.MESSAGE_SELECT_YOUR_USB_HID_DEVICE));
        }

        builder.setItems(devicesName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventBus.post(new SelectDeviceEvent(which));
            }
        });
        builder.setCancelable(true);
        builder.show();
    }
}