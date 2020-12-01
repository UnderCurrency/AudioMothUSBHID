/*
 *  (c)  Copyright 2020 Undercurrency
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.undercurrency.audiomoth.usbhid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothConfigReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothPacketReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateEvent;
import com.undercurrency.audiomoth.usbhid.events.AudioMothSetDateReceiveEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceAttachedEvent;
import com.undercurrency.audiomoth.usbhid.events.DeviceDetachedEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataReceiveEvent;
import com.undercurrency.audiomoth.usbhid.model.AudioMothOperations;
import com.undercurrency.audiomoth.usbhid.model.DateSettings;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;

import static com.undercurrency.audiomoth.usbhid.ByteJugglingUtils.byteToHexString;

/**
 * The USBHidTool is a Java port for the USB-HID-Tool project from Open Acoustic Devices
 * @see <a href="https://github.com/OpenAcousticDevices/USB-HID-Tool">USB-HID-Tool</a>
 * It is implemented as an Android service so it is easy to use and extend.
 *
 * We like to thank to @see <a href="https://github.com/452">emetemunoy</a>  portions of
 * this code comes from his @see <a href="https://github.com/452/USBHIDTerminal">USBHIDTerminal</a>
 *
 * I just added some Audio Moth custom events to handle the communication between Audio Moth and the
 * top level interface.
 *
 */
public class USBHidTool extends AbstractUSBHIDService {

    private static final String TAG = "USBHidTool";

    private AudioMothOperations lastOp;
    private Boolean localTime=false;


    @Override
    public void onCreate() {
        super.onCreate();
        setupNotifications();
    }

    @Override
    public void onCommand(Intent intent, String action, int flags, int startId) {
        super.onCommand(intent, action, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Reacts to the AudioMothConfigEvent
     * @param event
     */
    public void onEvent(AudioMothConfigEvent event) {
        lastOp = AudioMothOperations.USB_MSG_TYPE_SET_APP_PACKET;
        byte[] data = event.getRecordingSettings().serializeToBytes();
        byte[] payload = new byte[data.length + 1];
        payload[0] = lastOp.getOpcode();
        System.arraycopy(data, 0, payload, 1, data.length);
        Log.d(TAG, lastOp.toString());
        Log.d(TAG,"Original JSON");
        Log.d(TAG, ByteJugglingUtils.byteToHexString(payload));
        sendData(payload);
    }

    public void onEvent(AudioMothSetDateEvent event) {
        DateSettings ds = new DateSettings(event.getDate());
        lastOp = AudioMothOperations.USB_MSG_TYPE_SET_TIME;
        byte[] data = ds.serializeToBytes();
        byte[] payload = new byte[data.length + 1];
        payload[0] = lastOp.getOpcode();
        System.arraycopy(data, 0, payload, 1, data.length);
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, ByteJugglingUtils.byteToHexString(payload));
        sendData(payload);
    }

    public void onEvent(AudioMothPacketEvent event) {
        lastOp = AudioMothOperations.USB_MSG_TYPE_GET_APP_PACKET;
        byte[] payload = {lastOp.getOpcode()};
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, ByteJugglingUtils.byteToHexString(payload));
        sendData(payload);
    }

    @Override
    public void onDeviceConnected(UsbDevice device) {
        Log.d(TAG,"onDeviceConnected");
        eventBus.post(new DeviceAttachedEvent(device));
    }

    @Override
    public void onDeviceDisconnected(UsbDevice device) {
        eventBus.post(new DeviceDetachedEvent());
    }

    @Override
    public void onDeviceAttached(UsbDevice device) {
        Log.d(TAG,"onDeviceAttached");
        eventBus.post(new DeviceAttachedEvent(device));
    }

    @Override
    public void onShowDevicesList(CharSequence[] deviceName) {
        eventBus.post(new ShowDevicesListEvent(deviceName));
    }


    @Override
    public CharSequence onBuildingDevicesList(UsbDevice usbDevice) {
        return "VID:0x" + Integer.toHexString(usbDevice.getVendorId()) + " PID:0x" + Integer.toHexString(usbDevice.getProductId()) + " " + usbDevice.getDeviceName() + " devID:" + usbDevice.getDeviceId();
    }

    @Override
    public void onUSBDataSended(int status, byte[] out) {
        if (status <= 0) {
            Log.d(TAG,"Unable to send");
        } else {
            Log.d(TAG,"Sended " + status + " bytes");
        }
    }

    @Override
    public void onSendingError(Exception e) {
        Log.e(TAG,"Please check your bytes, sent as text");
    }

    @Override
    public void onUSBDataReceive(byte[] buffer) {
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, ByteJugglingUtils.byteToHexString(buffer));
        switch (lastOp) {
            case USB_MSG_TYPE_GET_APP_PACKET:
                Log.d(TAG,"GET_APP_PACKET Received BUFFER");
                Log.d(TAG,byteToHexString(buffer));
                eventBus.post(new AudioMothPacketReceiveEvent(new DeviceInfo(buffer,localTime)));
                break;
            case USB_MSG_TYPE_SET_TIME:
                Log.d(TAG,"SET_TIME Received BUFFER");
                Log.d(TAG,byteToHexString(buffer));
                byte[] recTime = new byte[64];
                System.arraycopy(buffer,1,recTime,0,4);
                eventBus.post(new AudioMothSetDateReceiveEvent(recTime));
                break;
            case USB_MSG_TYPE_SET_APP_PACKET:
                Log.d(TAG,"Received BUFFER");
                Log.d(TAG,byteToHexString(buffer));
                byte[] rec = new byte[64];
                System.arraycopy(buffer,1,rec,0,58);

                eventBus.post(new AudioMothConfigReceiveEvent(new RecordingSettings(rec)));
                break;
            default:
                eventBus.post(new USBDataReceiveEvent(buffer, buffer.length));
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       if(intent.getExtras()!=null){
            localTime = (Boolean) intent.getExtras().get("localTime");
       }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupNotifications() { //called in onCreate()
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, USBHidTool.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);
        PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, USBHidTool.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .setAction(getString(R.string.USB_HID_TERMINAL_CLOSE_ACTION)),
                0);
        mNotificationBuilder
                .setSmallIcon(android.R.drawable.stat_notify_sdcard_usb)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getText(R.string.app_name))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.action_exit), pendingCloseIntent)
                .setOngoing(true);
        mNotificationBuilder
                .setTicker(getText(R.string.app_name))
                .setContentText(getText(R.string.app_name));
        if (mNotificationManager != null) {
            mNotificationManager.notify(R.integer.USB_HID_TERMINAL_NOTIFICATION, mNotificationBuilder.build());
        }
    }



}
