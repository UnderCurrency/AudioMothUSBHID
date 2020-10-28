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

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.undercurrency.audiomoth.usbhid.events.PrepareDevicesListEvent;
import com.undercurrency.audiomoth.usbhid.events.SelectDeviceEvent;
import com.undercurrency.audiomoth.usbhid.events.USBDataSendEvent;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class AbstractUSBHIDService extends Service {

    private static final String TAG = AbstractUSBHIDService.class.getCanonicalName();

    public static final int REQUEST_GET_REPORT = 0x01;
    public static final int REQUEST_SET_REPORT = 0x09;
    public static final int REPORT_TYPE_INPUT = 0x0100;
    public static final int REPORT_TYPE_OUTPUT = 0x0200;
    public static final int REPORT_TYPE_FEATURE = 0x0300;

    private USBThreadDataReceiver usbThreadDataReceiver;

    private final Handler uiHandler = new Handler();

    private List<UsbInterface> interfacesList = null;

    private UsbManager mUsbManager;
    private UsbDeviceConnection connection;
    private UsbDevice device;

    private IntentFilter filter;
    private PendingIntent mPermissionIntent;

    private boolean sendedDataType;

    static class USBUtils{
        public static int toInt(byte b) {
            return (int) b & 0xFF;
        }

        public static byte toByte(int c) {
            return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
        }
    }

    protected EventBus eventBus = EventBus.getDefault();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(getString(R.string.ACTION_USB_PERMISSION)), 0);
        filter = new IntentFilter(getString(R.string.ACTION_USB_PERMISSION));
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(getString(R.string.ACTION_USB_SHOW_DEVICES_LIST));
        filter.addAction(getString(R.string.ACTION_USB_DATA_TYPE));
        registerReceiver(mUsbReceiver, filter);
        eventBus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (getString(R.string.ACTION_USB_DATA_TYPE).equals(action)) {
            sendedDataType = intent.getBooleanExtra(getString(R.string.ACTION_USB_DATA_TYPE), false);
        }
        onCommand(intent, action, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
        if (usbThreadDataReceiver != null) {
            usbThreadDataReceiver.stopThis();
        }
        unregisterReceiver(mUsbReceiver);
    }

    private class USBThreadDataReceiver extends Thread {

        private volatile boolean isStopped;

        public USBThreadDataReceiver() {
        }

        @Override
        public void run() {
            try {
                if (connection != null) {
                    while (!isStopped) {
                        for (UsbInterface intf: interfacesList) {
                            for (int i = 0; i < intf.getEndpointCount(); i++) {
                                UsbEndpoint endPointRead = intf.getEndpoint(i);
                                if (UsbConstants.USB_DIR_IN == endPointRead.getDirection()) {
                                    final byte[] buffer = new byte[endPointRead.getMaxPacketSize()];
                                    final int status = connection.bulkTransfer(endPointRead, buffer, buffer.length, 100);
                                    if (status > 0) {
                                        uiHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                onUSBDataReceive(buffer);
                                            }
                                        });
                                    } else {
                                        int transfer = connection.controlTransfer(0xA0, REQUEST_GET_REPORT, REPORT_TYPE_OUTPUT, 0x00, buffer, buffer.length, 100);
                                        if (transfer > 0) {
                                            uiHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    onUSBDataReceive(buffer);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in receive thread", e);
            }
        }

        public void stopThis() {
            isStopped = true;
        }
    }

    public void onEventMainThread(USBDataSendEvent event){
        Log.v(TAG,"UsbDataSendEvent");
        Log.d(TAG,"USBDataSendEvent");
        sendData(event.getData(), sendedDataType);
    }

    public void onEvent(SelectDeviceEvent event) {
        device = (UsbDevice) mUsbManager.getDeviceList().values().toArray()[event.getDevice()];
        mUsbManager.requestPermission(device, mPermissionIntent);
    }

    public void onEventMainThread(PrepareDevicesListEvent event) {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<CharSequence> list = new LinkedList<CharSequence>();
        for (UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            list.add(onBuildingDevicesList(usbDevice));
        }
        final CharSequence devicesName[] = new CharSequence[mUsbManager.getDeviceList().size()];
        list.toArray(devicesName);
        onShowDevicesList(devicesName);
    }

    protected void sendData(byte[] data, boolean sendAsString) {
        Log.v(TAG,"sendData");
        if (device != null && mUsbManager.hasPermission(device) && data.length>0) {
            // mLog(connection +"\n"+ device +"\n"+ request +"\n"+
            // packetSize);
            for (UsbInterface intf: interfacesList) {
                for (int i = 0; i < intf.getEndpointCount(); i++) {
                    UsbEndpoint endPointWrite = intf.getEndpoint(i);
                    if (UsbConstants.USB_DIR_OUT == endPointWrite.getDirection()) {
                        int status = connection.bulkTransfer(endPointWrite, data, data.length, 250);
                        onUSBDataSended(status, data);
                        status = connection.controlTransfer(0x21, REQUEST_SET_REPORT, REPORT_TYPE_OUTPUT, 0x02, data, data.length, 250);
                        onUSBDataSended(status, data);
                    }
                }
            }
        }
    }

    /**
     * receives the permission request to connect usb devices
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (getString(R.string.ACTION_USB_PERMISSION).equals(action)) {
                setDevice(intent);
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                setDevice(intent);
                if (device != null) {
                    onDeviceConnected(device);
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (device != null) {
                    device = null;
                    if (usbThreadDataReceiver != null) {
                        usbThreadDataReceiver.stopThis();
                    }
                    onDeviceDisconnected(device);
                }
            }
        }

        private void setDevice(Intent intent) {
            device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                onDeviceSelected(device);
                connection = mUsbManager.openDevice(device);
                if (connection == null) {
                    return;
                }
                interfacesList = new LinkedList();
                for(int i = 0; i < device.getInterfaceCount(); i++) {
                    UsbInterface intf = device.getInterface(i);
                    if(intf.getInterfaceClass()==3) {
                        connection.claimInterface(intf, true);
                        interfacesList.add(intf);
                    }
                }
                usbThreadDataReceiver = new USBThreadDataReceiver();
                usbThreadDataReceiver.start();
                onDeviceAttached(device);
            }
        }
    };

    public void onCommand(Intent intent, String action, int flags, int startId) {
    }

    public void onUSBDataReceive(byte[] buffer) {
    }

    public void onDeviceConnected(UsbDevice device) {
    }

    public void onDeviceDisconnected(UsbDevice device) {
    }

    public void onDeviceSelected(UsbDevice device) {
    }

    public void onDeviceAttached(UsbDevice device) {
    }

    public void onShowDevicesList(CharSequence[] deviceName) {
    }

    public CharSequence onBuildingDevicesList(UsbDevice usbDevice) {
        return null;
    }

    public void onUSBDataSending(String data) {
    }

    public void onUSBDataSended(int status, byte[] out) {
    }

    public void onSendingError(Exception e) {
    }



}
