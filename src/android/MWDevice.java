package com.mbientlab.metawear.cordova;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONObject;
import android.content.ServiceConnection;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.ComponentName;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.util.Log;
import java.util.HashMap;
import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.IBinder;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.RouteManager.MessageHandler;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.UnsupportedModuleException;

/**
 *
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/11/2015.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 *
 */

public class MWDevice extends CordovaPlugin implements ServiceConnection{
    public static final String TAG = "com.mbientlab.metawear.cordova";

    public static final String INITIALIZE = "initialize";
    public static final String CONNECT = "connect";
    public static final String DISCONNECT = "disconnect";
    public static final String READ_RSSI = "readRssi";
    public static final String START_ACCELEROMETER = "startAccelerometer";
    public static final String STOP_ACCELEROMETER = "stopAccelerometer";
    private MetaWearBleService.LocalBinder serviceBinder;
    private CallbackContext callbackContext;
    private String mwMacAddress;
    private MetaWearBoard mwBoard;
    private HashMap<String, CallbackContext> mwCallbackContexts;
    
    /**
     * Constructor.
     */
    public MWDevice() {}
    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mwCallbackContexts = new HashMap<String, CallbackContext>(); 
        Context applicationContext = cordova.getActivity().getApplicationContext();
        applicationContext.bindService(
                                       new Intent(cordova.getActivity(),
                                                  MetaWearBleService.class),
                                       this, Context.BIND_AUTO_CREATE
                                       );
        Log.v(TAG,"Init Device");
    }

    public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        final int duration = Toast.LENGTH_SHORT;
        // Shows a toast
        Log.v(TAG,"mwDevice received:"+ action);
        if(action.equals(INITIALIZE)){
            mwCallbackContexts.put(INITIALIZE, callbackContext);
            return true;
        } else if(action.equals(CONNECT)){
            mwCallbackContexts.put(CONNECT, callbackContext);
            mwMacAddress = (String) args.get(0);
            mwBoard = retrieveBoard(); 
            connectBoard();
            return true;
        } else if(action.equals(DISCONNECT)){
            mwBoard.disconnect();
            return true;
        } else if(action.equals(READ_RSSI)){
            mwCallbackContexts.put(READ_RSSI, callbackContext);
            readRssi();
            return true;
        } else if(action.equals(START_ACCELEROMETER)){
            mwCallbackContexts.put(START_ACCELEROMETER, callbackContext);
            startAccelerometer();
            return true;
        } else if(action.equals(STOP_ACCELEROMETER)){
            stopAccelerometer();
            return true;
        }
        else{
            return false;}
    }

    @Override
    public void onDestroy(){
        cordova.getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service){
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        Log.i("MWDevice", "Service Connected");
        mwCallbackContexts.get(INITIALIZE).sendPluginResult(new PluginResult(PluginResult.Status.OK,
                                                                             "initialized"));
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {}



    public MetaWearBoard retrieveBoard() {
        final BluetoothManager btManager= 
            (BluetoothManager) cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice= 
            btManager.getAdapter().getRemoteDevice(mwMacAddress);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard= serviceBinder.getMetaWearBoard(remoteDevice);
        return(mwBoard);
    }

    private final AsyncOperation.CompletionHandler<RouteManager> accelerometerHandler =
        new AsyncOperation.CompletionHandler<RouteManager>(){
            @Override
            public void success(RouteManager result){
                result.subscribe("accel_stream_key", new MessageHandler() {
                        @Override
                        public void process(Message msg){
                            CartesianFloat axes = msg.getData(CartesianFloat.class);
                            JSONObject resultObject = new JSONObject();
                            try {
                            resultObject.put("x", axes.x());
                            resultObject.put("y", axes.y());
                            resultObject.put("z", axes.z());
                            } catch (JSONException e){
                                Log.e("Metawear Cordova Error: ", e.toString());
                            }
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                                         resultObject);
                            pluginResult.setKeepCallback(true);
                            mwCallbackContexts.get(START_ACCELEROMETER).sendPluginResult(pluginResult);
                            Log.i("Metawear Cordova Axis", axes.toString());
                        }
                    });
            }
        };

    private Accelerometer getAccelerometer(){
        Accelerometer accelModule = null;

        try {
            accelModule= mwBoard.getModule(Accelerometer.class);
        }catch(UnsupportedModuleException e){
            Log.e("Metawear Cordova Error: ", e.toString());
        }
        return accelModule;
    }

    private void startAccelerometer(){
        Accelerometer accelModule = getAccelerometer();
        accelModule.routeData()
            .fromAxes().stream("accel_stream_key")
            .commit().onComplete(accelerometerHandler);


        // Set the sampling frequency to 50Hz, or closest valid ODR
        accelModule.setOutputDataRate(50.f);
        // Set the measurement range to +/- 4g, or closet valid range
        accelModule.setAxisSamplingRange(4.0f);

        // enable axis sampling
        accelModule.enableAxisSampling();

        // Switch the accelerometer to active mode
        accelModule.start();
    }

    private void stopAccelerometer(){
        getAccelerometer().stop();
    }

    private final AsyncOperation.CompletionHandler<Integer> readRssiHandler =
        new AsyncOperation.CompletionHandler<Integer>() {
            @Override
            public void success(final Integer result){
                Log.i("Metawear Cordova RSSI: ", result.toString());
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                             result.toString());
                mwCallbackContexts.get(READ_RSSI).sendPluginResult(pluginResult);
            }

            @Override
            public void failure(Throwable error){
                Log.e("Metawear Cordova Error: ", error.toString());
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                                             "ERROR");
                mwCallbackContexts.get(READ_RSSI).sendPluginResult(pluginResult);
            }
        };
        
    public void readRssi() {
        AsyncOperation<Integer> result = mwBoard.readRssi();
        result.onComplete(readRssiHandler);
    }
    
    private final ConnectionStateHandler stateHandler = new ConnectionStateHandler() {
            @Override
            public void connected() {
                Log.i("MainActivity", "Connected");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                             "CONNECTED");
                pluginResult.setKeepCallback(true);
                mwCallbackContexts.get(CONNECT).sendPluginResult(pluginResult);
            }

            @Override
            public void disconnected() {
                Log.i("MainActivity", "Connected Lost");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                             "DISCONNECTED");
                pluginResult.setKeepCallback(true);
                mwCallbackContexts.get(CONNECT).sendPluginResult(pluginResult);
            }

            @Override
            public void failure(int status, Throwable error) {
                Log.e("MainActivity", "Error connecting", error);
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                             "ERROR");
                pluginResult.setKeepCallback(true);
                mwCallbackContexts.get(CONNECT).sendPluginResult(pluginResult);
            }
        };

    public void connectBoard() {
        mwBoard.setConnectionStateHandler(stateHandler);
        mwBoard.connect();
    }
}
