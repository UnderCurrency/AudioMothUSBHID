package com.undercurrency.audiomoth.testapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.undercurrency.audiomoth.usbhid.USBHidTool;
import com.undercurrency.audiomoth.usbhid.events.DeviceAttachedEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceDetachedEvent;
import com.undercurrency.audiomoth.usbhid.events.PrepareDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.SelectDeviceEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataSendEvent;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private static final String TAG = "TestAudoMoth";
    private Button btnDispositivo;
    private Button btnConfigurar;
    protected EventBus eventBus;
    private Intent usbhidService;
    private RecordingSettings rs;
    private DeviceInfo deviceInfo;
    private boolean deviceSelected = false;

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
                String ultrasonic =  getJsonFromAssets(this,"Ultrasonico.json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                rs =  gson.fromJson(ultrasonic, RecordingSettings.class);
                rs.setDeviceInfo(deviceInfo);
                byte[] packet = rs.serializeToBytes();

                eventBus.post(new USBDataSendEvent(packet));
            }
        } else if (v == btnDispositivo) {
            Log.v(TAG, "onClick btnDispositivo");
            eventBus.post(new PrepareDevicesListEvent());
        }
    }

    private void startService(){
        usbhidService = new Intent(this, USBHidTool.class);
        startService(usbhidService);
    }

    public void onEvent(DeviceAttachedEvent event){
        Toast.makeText(getApplicationContext(),getString(R.string.DEVICE_ATTACHED),Toast.LENGTH_LONG);
        byte[] packet ={0x05};
        eventBus.post(new USBDataSendEvent(packet));
        deviceSelected=true;
        btnConfigurar.setEnabled(true);
    }

    public void onEvent(DeviceDetachedEvent event) {
        btnConfigurar.setEnabled(false);
    }

    public void onEvent(USBDataReceiveEvent event) {
        Toast.makeText(getApplicationContext(), "USBDataReceiveEvent "+event.getBytesCount(),Toast.LENGTH_LONG);
        Log.v(TAG,"USBDataReceiveEvent "+event.getBytesCount());
       if( event.getBytesCount() == 64 ) {
           deviceInfo = new DeviceInfo(event.getData());
           SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zz");
           Log.v(TAG,"deviceInfo "+deviceInfo.getBattery()+", serial "+deviceInfo.getDeviceId()+", firmware "+ deviceInfo.getFirmwareVersion()+" date "+sdf.format(deviceInfo.getDate()));
       }
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

    public static String getJsonFromAssets(Context ctx, String pathToJson){
        InputStream rawInput;
        ByteArrayOutputStream rawOutput = null;
        try {
            rawInput = ctx.getAssets().open(pathToJson);
            byte[] buffer = new byte[rawInput.available()];
            rawInput.read(buffer);
            rawOutput = new ByteArrayOutputStream();
            rawOutput.write(buffer);
            rawOutput.close();
            rawInput.close();
        } catch (IOException e) {
            Log.e("Error", e.toString());
        }
        return rawOutput.toString();
    }
}