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
import com.undercurrency.audiomoth.usbhid.ByteJugglingUtils;
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
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.byteToHexString;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String pathToJson = "test-case-02.config";
    private static final String TAG = "TestAudoMoth";
    protected EventBus eventBus;
    private Button btnDispositivo;
    private Button btnConfigurar;
    private Button btnFecha;
    private Button btnSerialize;
    private TextView tvJson;
    private Intent usbhidService;
    private RecordingSettings rs;
    private DeviceInfo deviceInfo;
    private boolean deviceSelected = false;

    private RecordingSettings rsIn;
    private RecordingSettings rsOut;

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

    /**
     * First step:
     * Add an event bus in onCreate method
     * @param savedInstanceState
     */
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
        btnDispositivo = findViewById(R.id.btnDispositivo);
        btnDispositivo.setOnClickListener(this);
        btnConfigurar = findViewById(R.id.btnConfigurar);
        btnConfigurar.setOnClickListener(this);
        btnFecha = findViewById(R.id.btnFecha);
        btnFecha.setOnClickListener(this);
        btnSerialize = findViewById(R.id.btnSerialize);
        btnSerialize.setOnClickListener(this);
        tvJson = findViewById(R.id.tvJson);
    }

    /**
     * Second step: write a function called startService with a new Intent to all USBHidTool.class
     * this call start up the android service responsible for calling audio moth
     */
    private void startService() {
        usbhidService = new Intent(this, USBHidTool.class);
        startService(usbhidService);
    }

    /**
     * Third step:
     * In onStart method add a call to startService() and a call to eventBus.register(this)
     *
     */
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

    /**
     * Fourth step:
     * The way to interact with audiomoth is by calling events to configure it,
     *
     * If you want to receive the date, firmware and serial number you must send a
     * eventBus.post(PrepareDevicesListEvent()) and write the corresponding code to receive the data
     * on(AudioMothPacketReceiveEvent event), inside this event lives an object called DeviceConfig.
     *
     * If you want to set the Date to the device, you must send the corresponding event
     * eventBus.post(new AudioMothSetDateEvent(new Date())); and write the corresponding code to receive
     * the response event onEvent(AudioMothSetDateReceiveEvent event) , inside this event lives the Date from the device.
     *
     * If you want to set the AudioMothConfig , you must send the corresponding event
     * eventBus.post(new AudioMothConfigEvent(rs)); with the RecordingSettings object set,
     * you must create a RecordingSettings object with all the values
     * The RecordingSettings must include an instance of DeviceInfo, so  by example,
     * before that you need to call at least the PrepareDevicesListEvent and receive the DeviceConfig
     * and then set it to the new RecordingSettings. Then you must receive
     * the event onEvent(AudioMothConfigReceiveEvent event),
     * inside this event lives a regenerated RecordingSettings reconstructed from the response back from AudioMoth
     *
     */
    @Override
    public void onClick(View v) {
        if (v == btnConfigurar) {
            if (deviceSelected) {
                String ultrasonic = getJsonFromAssets(this, pathToJson);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                rs = gson.fromJson(ultrasonic, RecordingSettings.class);
                rsIn = gson.fromJson(ultrasonic,RecordingSettings.class);
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
        } else if (v == btnSerialize) {
            String ultrasonic = getJsonFromAssets(this, "Ultrasonico.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            RecordingSettings rsTest = gson.fromJson(ultrasonic, RecordingSettings.class);
            rsTest.setDeviceInfo(new DeviceInfo("CAFEBABE", "1.4.4", "4.5", new Date()));
            byte[] arr = rsTest.serializeToBytes();
            Log.d(TAG,"Original JSON-ByteArray");
            Log.d(TAG,byteToHexString(arr));
            RecordingSettings rsDeserialize = new RecordingSettings(arr);
            rsDeserialize.setDeviceInfo(new DeviceInfo("CAFEBABE", "1.4.4", "4.5", new Date()));
            Log.d(TAG,"Same JSON-ByteArray");
            Log.d(TAG,byteToHexString(arr));
            String json = gson.toJson(rsDeserialize);
            Log.d("json rsDeserialize", json);
            Log.v(TAG, byteToHexString(arr));
            Log.v(TAG, "Equals = " + (rsDeserialize.equals(rsTest)?"YES":"NO"));

        }
    }


    /**
     *
     * Write a onEvent call  to detect the DeviceAttachedEvent
     * @param event
     */
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

    public void onEvent(AudioMothConfigReceiveEvent event) {
        Log.d(TAG, "AudioMothConfigReceiveEvent");
        if (event.getRecordingSettings() != null) {
            rsOut = event.getRecordingSettings();
            Gson gson = new Gson();
            String json = gson.toJson(event.getRecordingSettings());
            Log.d(TAG, json);
            tvJson.append(json);
            if(rsIn.equals(rsOut)){
                tvJson.append("************************\n");
                tvJson.append("         Config OK\n");
                tvJson.append("************************\n");
            }

        }
    }

    public void onEvent(AudioMothSetDateReceiveEvent event) {
        Log.d(TAG, "AudioMothSetDateReceiveEvent");
        if (event.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss zz");
            Log.d(TAG, sdf.format(event.getDate()));
            tvJson.append(sdf.format(event.getDate()));
        }
    }

    public void onEvent(USBDataReceiveEvent event) {
        Log.v(TAG, "USBDataReceiveEvent " + event.getBytesCount());
        Log.d(TAG, ByteJugglingUtils.byteToHexString(event.getData()));

    }

    public void onEvent(ShowDevicesListEvent event) {
        showListOfDevices(event.getCharSequenceArray());
    }

    void showListOfDevices(CharSequence[] devicesName) {
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