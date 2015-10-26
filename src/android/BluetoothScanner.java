package com.mbientlab.metawear.cordova;

import android.util.Log;
import android.os.Handler;
import org.apache.cordova.PluginResult;
import com.mbientlab.metawear.cordova.MWDevice;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import android.content.Context;

/**
 *
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/11/2015.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 *
 */

public class BluetoothScanner{
    private final static long DEFAULT_SCAN_PERIOD = 5000L;
    private MWDevice mwDevice;
    private Handler scannerHandler;
    private boolean isScanning = false;
    private BluetoothAdapter btAdapter;
    private JSONObject boards;

    public BluetoothScanner(MWDevice device){
        mwDevice = device;
        scannerHandler = new Handler();
        btAdapter= ((BluetoothManager) mwDevice.cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        if (btAdapter == null) {
            throw new RuntimeException("Metawear Cordova Plugin:  No bluetooth Adapter found!");
        }
   }

    private final BluetoothAdapter.LeScanCallback scanCallback= new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
            ///< Service UUID parsing code taking from stack overflow= http://stackoverflow.com/a/24539704
            JSONObject resultObject = new JSONObject();
            try {
                resultObject.put("address",  bluetoothDevice.getAddress());
                resultObject.put("rssi", String.valueOf(rssi));
                boards.put(bluetoothDevice.getAddress(), resultObject);
            } catch (JSONException e){
                Log.e("Metawear Cordova Error: ", e.toString());
            }
            Log.i("Metawear Cordova scan devices,  device found:", resultObject.toString());
        }
    };

    public void startBleScan() {
        boards = new JSONObject();
        isScanning= true;
        scannerHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopBleScan();
                }
            }, DEFAULT_SCAN_PERIOD);

        ///< TODO: Use startScan method instead from API 21
        btAdapter.startLeScan(scanCallback);
    }

    public void stopBleScan() {
        if (isScanning) {
            ///< TODO: Use stopScan method instead from API 21
            btAdapter.stopLeScan(scanCallback);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                                         boards);
            pluginResult.setKeepCallback(true);
            mwDevice.getMwCallbackContexts().get(mwDevice.SCAN_FOR_DEVICES).sendPluginResult(pluginResult);
            isScanning= false;
        }
    }
}
