/*
 *
 *  (c)  Copyright 2020 Undercurrency
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.undercurrency.audiomoth.testapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.undercurrency.audiomoth.usbhid.USBHidTool;
import com.undercurrency.audiomoth.usbhid.USBUtils;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceAttachedEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceDetachedEvent;
import com.undercurrency.audiomoth.usbhid.events.PrepareDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.SelectDeviceEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataSendEvent;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TestAudoMoth";
    protected EventBus eventBus;
    private Button btnDispositivo;
    private Button btnConfigurar;
    private Button btnFecha;
    private TextView tvJson;
    private Intent usbhidService;
    private RecordingSettings rs;
    private DeviceInfo deviceInfo;
    private boolean deviceSelected = false;

    public static String getJsonFromAssets(Context ctx, String pathToJson) {
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

    private void initUI() {
        btnDispositivo = (Button) findViewById(R.id.btnDispositivo);
        btnDispositivo.setOnClickListener(this);
        btnConfigurar = (Button) findViewById(R.id.btnConfigurar);
        btnConfigurar.setOnClickListener(this);
        btnFecha = (Button) findViewById(R.id.btnFecha);
        btnFecha.setOnClickListener(this);
        tvJson = (TextView) findViewById(R.id.tvJson);
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
        if (v == btnConfigurar) {
            if (deviceSelected) {
                String ultrasonic = getJsonFromAssets(this, "Ultrasonico.json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                rs = gson.fromJson(ultrasonic, RecordingSettings.class);
                rs.setDeviceInfo(deviceInfo);
                //byte[] packet = rs.serializeToBytes();
                eventBus.post(new AudioMothConfigEvent(rs));
                //eventBus.post(new USBDataSendEvent(packet));
            }
        } else if (v == btnDispositivo) {
            Log.v(TAG, "onClick btnDispositivo");
            eventBus.post(new PrepareDevicesListEvent());
        } else if (v == btnFecha) {
            eventBus.post(new AudioMothSetDateEvent(new Date()));
        }
    }

    private void startService() {
        usbhidService = new Intent(this, USBHidTool.class);
        startService(usbhidService);
    }

    public void onEvent(DeviceAttachedEvent event) {
        Toast.makeText(getApplicationContext(), getString(R.string.DEVICE_ATTACHED), Toast.LENGTH_LONG);
        eventBus.post(new AudioMothPacketEvent());
        deviceSelected = true;
        btnConfigurar.setEnabled(true);
    }

    public void onEvent(DeviceDetachedEvent event) {
        btnConfigurar.setEnabled(false);
    }

    public void onEvent(AudioMothPacketReceiveEvent event) {
        Log.d(TAG, "AudioMothPacketReceiveEvent");
        if (event.getDeviceInfo() != null) {
            deviceInfo = event.getDeviceInfo();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zz");
            Log.d(TAG, "Battery " + deviceInfo.getBattery() + ", serial " + deviceInfo.getDeviceId() + ", firmware " + deviceInfo.getFirmwareVersion() + " date " + sdf.format(deviceInfo.getDate()));
            tvJson.append("Battery " + deviceInfo.getBattery() + ", serial " + deviceInfo.getDeviceId() + ", firmware " + deviceInfo.getFirmwareVersion() + " date " + sdf.format(deviceInfo.getDate()));

        }
    }

    public void onEvent(AudioMothConfigReceiveEvent event){
        Log.d(TAG,"AudioMothConfigReceiveEvent");
        if(event.getRecordingSettings()!=null){
            Gson gson = new Gson();
            String json = gson.toJson(event.getRecordingSettings());
            Log.d(TAG, json);
            tvJson.append(json);
        }
    }

    public void onEvent(AudioMothSetDateReceiveEvent event){
        Log.d(TAG,"AudioMothSetDateReceiveEvent");
        if(event.getDate()!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zz");
            Log.d(TAG,sdf.format(event.getDate()).toString());
            tvJson.append(sdf.format(event.getDate()).toString());
        }
    }

    public void onEvent(USBDataReceiveEvent event) {
        Log.v(TAG, "USBDataReceiveEvent " + event.getBytesCount());
        Log.d(TAG, USBUtils.byteToHexString(event.getData()));

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