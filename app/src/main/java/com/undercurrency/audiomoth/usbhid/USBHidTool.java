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
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
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
import com.undercurrency.audiomoth.usbhid.events.LogMessageEvent;
import com.undercurrency.audiomoth.usbhid.events.ShowDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataReceiveEvent;
import com.undercurrency.audiomoth.usbhid.model.AudioMothOperations;
import com.undercurrency.audiomoth.usbhid.model.DateSettings;
import com.undercurrency.audiomoth.usbhid.model.DeviceInfo;
import com.undercurrency.audiomoth.usbhid.model.RecordingSettings;


public class USBHidTool extends AbstractUSBHIDService {

    private static final String TAG = "USBHidTool";

    private String delimiter;
    private DeviceInfo deviceInfo;
    private RecordingSettings recordingSettings;
    private AudioMothOperations lastOp;


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


    public void onEvent(AudioMothConfigEvent event) {
        lastOp = AudioMothOperations.USB_MSG_TYPE_SET_APP_PACKET;
        byte[] data = event.getRecordingSettings().serializeToBytes();
        byte[] payload = new byte[data.length + 1];
        payload[0] = lastOp.getOpcode();
        System.arraycopy(data, 0, payload, 1, data.length);
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, USBUtils.byteToHexString(payload));
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
        Log.d(TAG, USBUtils.byteToHexString(payload));
        sendData(payload);
    }

    public void onEvent(AudioMothPacketEvent event) {
        lastOp = AudioMothOperations.USB_MSG_TYPE_GET_APP_PACKET;
        byte[] payload = {lastOp.getOpcode()};
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, USBUtils.byteToHexString(payload));
        sendData(payload);
    }

    @Override
    public void onDeviceConnected(UsbDevice device) {
        mLog("device VID:0x" + Integer.toHexString(device.getVendorId()) + " PID:0x" + Integer.toHexString(device.getProductId()) + " " + device.getDeviceName() + " connected");
        eventBus.post(new DeviceAttachedEvent());
    }

    @Override
    public void onDeviceDisconnected(UsbDevice device) {
        mLog("device disconnected");
        eventBus.post(new DeviceDetachedEvent());
    }

    @Override
    public void onDeviceSelected(UsbDevice device) {
        mLog("Selected device VID:0x" + Integer.toHexString(device.getVendorId()) + " PID:0x" + Integer.toHexString(device.getProductId()));
        mLog("id " + showDecHex(device.getDeviceId()));
        mLog("name " + device.getDeviceName());
        mLog("manufacturer name " + device.getManufacturerName());
        mLog("serial number " + device.getSerialNumber());
        mLog("class " + showDecHex(device.getDeviceClass()));
        mLog("subclass " + showDecHex(device.getDeviceSubclass()));
        mLog("protocol " + showDecHex(device.getDeviceProtocol()));
        mLog("");
        mLog("interfaces count " + device.getInterfaceCount());
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            mLog("");
            mLog("interface " + i);
            UsbInterface dInterface = device.getInterface(i);
            mLog(" name " + dInterface.getName());
            mLog(" id " + showDecHex(dInterface.getId()));
            mLog(" class " + showDecHex(dInterface.getInterfaceClass()));
            mLog(" subclass " + showDecHex(dInterface.getInterfaceSubclass()));
            mLog(" protocol " + showDecHex(dInterface.getInterfaceProtocol()));
            mLog("");
            mLog(" endpoint count " + dInterface.getEndpointCount());
            for (int ien = 0; ien < dInterface.getEndpointCount(); ien++) {
                UsbEndpoint endpoint = dInterface.getEndpoint(ien);
                mLog("");
                mLog("  endpoint " + ien);
                mLog("  endpoint number " + endpoint.getEndpointNumber());
                mLog("  address " + showDecHex(endpoint.getAddress()));
                mLog("  type " + showDecHex(endpoint.getType()));
                mLog("  direction " + directionInfo(endpoint.getDirection()));
                mLog("  max packet size " + endpoint.getMaxPacketSize());
                mLog("  interval " + endpoint.getInterval());
                mLog("  attributes " + showDecHex(endpoint.getAttributes()));
            }
        }
        mLog("");
        mLog("configuration count " + device.getConfigurationCount());
        for (int i = 0; i < device.getConfigurationCount(); i++) {
            UsbConfiguration configuration = device.getConfiguration(i);
            mLog("");
            mLog("configuration " + i);
            mLog(" name " + configuration.getName());
            mLog(" id " + showDecHex(configuration.getId()));
            mLog(" max power " + configuration.getMaxPower());
            mLog(" is self powered " + configuration.isSelfPowered());
            mLog("");
            mLog("configuration interfaces count " + configuration.getInterfaceCount());
            for (int ic = 0; i < configuration.getInterfaceCount(); i++) {
                mLog("");
                mLog("configuration interface " + ic);
                UsbInterface cInterface = configuration.getInterface(i);
                mLog(" name " + cInterface.getName());
                mLog(" id " + showDecHex(cInterface.getId()));
                mLog(" class " + showDecHex(cInterface.getInterfaceClass()));
                mLog(" subclass " + showDecHex(cInterface.getInterfaceSubclass()));
                mLog(" protocol " + showDecHex(cInterface.getInterfaceProtocol()));
                mLog("");
                mLog(" configuration endpoint count " + cInterface.getEndpointCount());
                for (int ien = 0; ien < cInterface.getEndpointCount(); ien++) {
                    UsbEndpoint endpoint = cInterface.getEndpoint(ien);
                    mLog("");
                    mLog("  endpoint " + ien);
                    mLog("  endpoint number " + endpoint.getEndpointNumber());
                    mLog("  address " + showDecHex(endpoint.getAddress()));
                    mLog("  type " + showDecHex(endpoint.getType()));
                    mLog("  direction " + directionInfo(endpoint.getDirection()));
                    mLog("  max packet size " + endpoint.getMaxPacketSize());
                    mLog("  interval " + endpoint.getInterval());
                    mLog("  attributes " + showDecHex(endpoint.getAttributes()));
                }
            }
        }

    }

    @Override
    public void onDeviceAttached(UsbDevice device) {
        eventBus.post(new DeviceAttachedEvent());
    }

    @Override
    public void onShowDevicesList(CharSequence[] deviceName) {
        eventBus.post(new ShowDevicesListEvent(deviceName));
    }

    private String directionInfo(int data) {
        if (UsbConstants.USB_DIR_IN == data) {
            return "IN " + showDecHex(data);
        }
        if (UsbConstants.USB_DIR_OUT == data) {
            return "OUT " + showDecHex(data);
        }
        return "NA " + showDecHex(data);
    }

    private String showDecHex(int data) {
        return data + " 0x" + Integer.toHexString(data);
    }

    @Override
    public CharSequence onBuildingDevicesList(UsbDevice usbDevice) {
        return "VID:0x" + Integer.toHexString(usbDevice.getVendorId()) + " PID:0x" + Integer.toHexString(usbDevice.getProductId()) + " " + usbDevice.getDeviceName() + " devID:" + usbDevice.getDeviceId();
    }

    @Override
    public void onUSBDataSending(String data) {
        mLog("Sending: " + data);
    }

    @Override
    public void onUSBDataSended(int status, byte[] out) {
        if (status <= 0) {
            mLog("Unable to send");
        } else {
            mLog("Sended " + status + " bytes");
            for (int i = 0; i < out.length/* && out[i] != 0*/; i++) {
                mLog(getString(R.string.SPACE) + USBUtils.toInt(out[i]));
            }
        }
    }

    @Override
    public void onSendingError(Exception e) {
        mLog("Please check your bytes, sent as text");
    }

    @Override
    public void onUSBDataReceive(byte[] buffer) {
        Log.d(TAG, lastOp.toString());
        Log.d(TAG, USBUtils.byteToHexString(buffer));
        switch (lastOp) {
            case USB_MSG_TYPE_GET_APP_PACKET:
                DeviceInfo deviceInfo = new DeviceInfo(buffer);
                eventBus.post(new AudioMothPacketReceiveEvent(new DeviceInfo(buffer)));
                break;
            case USB_MSG_TYPE_SET_TIME:
                eventBus.post(new AudioMothSetDateReceiveEvent(buffer));
                break;
            case USB_MSG_TYPE_SET_APP_PACKET:
                eventBus.post(new AudioMothConfigReceiveEvent(new RecordingSettings(buffer)));
                break;
            default:
                eventBus.post(new USBDataReceiveEvent(buffer, buffer.length));
        }

    }

    private void mLog(String log) {
        eventBus.post(new LogMessageEvent(log));
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

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }


}
