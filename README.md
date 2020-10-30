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
 
