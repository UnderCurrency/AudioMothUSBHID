# AudioMothUSBHID

Android aar module to write configuration to AudioMoth via Android USB


Just add aar to your project and call the initialization of the service
Next build a RecordingSettings object with some data
Then write it to AudioMoth

Portions from https://github.com/452/USBHIDTerminal/
    
# Quick Start Guide

 1. Clone this repo.
 2. Open it in Android Studio
 3. Connect your phone to WiFi
 4. Connect your phone to PC
 5. Open adb (from command line)
 6. Issue the command  >adb tcpip 5555
 7. Issue the command >adb connect  &#91;your-phohe-ip-address&#93;:5555	
 8. Open testapp
 9. Press debug button
 10. Connect your AudioMoth to the mini-usb to the phone
 11. Press detect button, select a device and accept the permission request
 12. Press configure button
 13. Press set date button 
 14. On Android Studio press Alt+6 Logcat console
 15. Select your phone if it's not already selected
 16. Select the running process com.undercurrency.audiomoth.test
 17. Then, you will see the log messages, try to notice the byte arrays printed on the log, this byte arrays confirm the correct send/receive of the configuration

 #Developer Guide
 The audiomoth-usbhid library works as an Android Service, this means that the only way to use it is througth events, events in and events out.
 If you send an event in, this event have its corresponding event out

 | Event In | Event Out  |
 |--|--|
 |PrepareDevicesListEvent  | AudioMothPacketReceiveEvent  |
 |AudioMothSetDateEvent|AudioMothSetDateReceiveEvent|
 |AudioMothConfigEvent|AudioMothConfigReceiveEvent|

This library also have the capability to seriallize and deserialize an AudioMoth binary arrays to the corresponding Java Object called RecordingSettings
Please review the RecordingSettings class and their correspondin methods:
serializeToBytes and the corresponding RecordingSettings(byte[] array) constructor.

---


This code includes a Test App, it contains a main activity with 4 buttons to test all the functions
included in the audiomoth-usbhid aar library.
Please follow this four steps if you want to use the library for your own purposes:
In an Activity

1. Add an event bus in onCreate method
2. Write a function called startService with a new Intent to all USBHidTool.class this call start up
the android service responsible for calling AudioMoth
3. In onStart method add a call to startService() and a call to eventBus.register(this)
4. The way to interact with audiomoth is by calling events to configure it,
If you want to receive the date, firmware and serial number you must send a
 eventBus.post(PrepareDevicesListEvent()) and write the corresponding code to receive the data
 on(AudioMothPacketReceiveEvent event), inside this event lives an object called DeviceConfig.

If you want to set the Date to the device, you must send the corresponding event
eventBus.post(new AudioMothSetDateEvent(new Date())); and write the corresponding code to receive
the response event onEvent(AudioMothSetDateReceiveEvent event) , inside this event lives the Date
from the device.

If you want to set the AudioMothConfig , you must send the corresponding event
eventBus.post(new AudioMothConfigEvent(rs)); with the RecordingSettings object set,
you must create a RecordingSettings object with all the values
The RecordingSettings must include an instance of DeviceInfo, so  by example,
before that you need to call at least the PrepareDevicesListEvent and receive the DeviceConfig
and then set it to the new RecordingSettings. Then you must receive
the event onEvent(AudioMothConfigReceiveEvent event),
inside this event lives a regenerated RecordingSettings reconstructed from the response back
from AudioMoth