 === Setup project ===

1. Download Android SDK, http://developer.android.com/sdk/index.html
2. Install SDK
3. Get ilearnrw project from git if you dont have it already
4. Import projects, "ilearnrw" and "ilearnrw-reader"
5. Add "ilearnrw" project to "ilearnrw-reader" build path
  a. Right-Click project in package explorer
  b. Choose "Properties"
  c. Choose "Java Build Path"
  d. Click "Add" and select "ilearnrw" project and finally click "Ok"
  e. Then just click "Ok" again to leave properties and you're done.

These directions are assuming that you downloaded Android sdk through the given link at 1.



 === Running your app ===

There are two ways you can test your app out. Either run it through an emulator or connect an android device to your computer.

 -- Emulator --
Running an app through the emulator can be really slow

 1. Open Eclipse
 2. Open menu "Window" 
 3. Choose "Android Virtual Device Manager"
 4. Click "New"
 5. Fill in required fields and click "Ok"
 6. Run project and a "Android Device Chooser" should pop up
  a. Pick your device and click "Ok"
  

 -- Device -- 
 Running your app with a device requires that you enable "Developer Options" 
 and how you do this is different depending on what kind of device you own.
 
On a Samsung Galaxy tab 3 10.1" you go to "Settings" and go the "About device" section and tap "Build number" 7 times

1. Activate "Developer Options"
2. Enable "USB Debugging" in the "Developer Options"
3. Connect your device to your computer using the USB wire
4. Try to run your app
  a. If you can see your device in the "Android Device Chooser", pick it and click "Ok"
  b. If you can't see your device in the "Android Device Chooser"
    There can be alot possibilities for why this is happening but the two most common are
    * Faulty USB wire
    * Missing USB Driver, http://developer.android.com/training/basics/firstapp/running-app.html#RealDevice
      = Windows 8 =
      1. Open Windows Explorer
      2. Right-Click "This PC" and choose either "Manage" or "Properties"
        a. If you chose "Manage" click "Device Manager" under "System Tools"
        b. If you chose "Properties" click "Device Manager"
      3. Choose "Universal Serial Bus controllers" and find your device USB connection, most likely the one with a exclamation mark by it (!)
      4. Right-Click the connection and select "Update Driver Software"
      5. Choose "Browse my computer for driver software"
      6. Go to you android sdk install location.
      7. Then go to "/sdk/extras/google/usb_driver/" and select appropriate driver
      8. Install the driver and now you should be able to run your app