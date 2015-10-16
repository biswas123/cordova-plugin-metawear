Cordova Plugin
==============

Cordova plugin for the MetaWear API.  This is currently a work in progress.  The API is very fluid and breaking changes are likely until things are a bit more feature complete.

##Getting Started:

*Create your Cordova project
```
cordova create <your project>
```
*Add in the plugin
```
cordova plugin add http://www.github.com/mbientlab-projects/cordova-plugin-metawear.html
```
*Add in your platform
```
cordova platform add android
cordova platform add ios
```
*Load Device.js in your javascript.


##Using The Plugin

Currently the project supports the following functions:

* Connecting and Disconnecting to the board
* Reading the RSSI for the device.
* Streaming accelerometer data.

Currently only Android is supported,  but IOS is on the roadmap.

Methods are called from the metawear.mwdevice object.  Callbacks take the form of

```Javascript
var mycallback = function(result){
   // your code here
}
```

##Supported Methods

###Initialize

Initialize takes a success and failure callback and parameters.  The result value in the callbacks are string values indicating a success or failure of the command.

```Javascript
var success = function(result){
  console.log("Initialized with status: " + result);
}

var failure = function(result){
  console.log("Initialize Error: " + result);
}

mbientlab.mwdevice.initialize(success, failure);
```

###Connect

Connects to the specified board.  The success callback is also used for disconnect.

```Javacript
mbientlab.mwdevice.connect(macAddressOfBoard, succesCallback, failureCallback);
```

###Disconnect

Disconnects from the board.  Sends a disconnect message to the success callback of connect.

```Javascript
mbientlab.mwdevice.disconnect();
```

###readRssi

Reads the RSSI value you are currently getting from the board.  The RSSI is returned as a string in the success callback.

```Javascript
mbientlab.mwdevice.readRssi(successCallback, failureCallback);
```

###startAccelerometer

Starts the accelerometer on the board and streams the data to the callback until it is stopped.  The callback result is an object with x,y and z values from the accelerometer.

```Javascript
var failure = function(result){
}

var success = function(result){
   console.log("x: " + result.x + " y: " + result.y + " z: " + result.z);
}

mbientlab.mwdevice.startAccelerometer(success, failure);
```

###stopAccelerometer

Stops the accelerometer and stops streaming data.

```Javascript
mbientlab.mwdevice.stopAccelerometer();
```