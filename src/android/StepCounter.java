package com.mbientlab.metawear.cordova;

import com.mbientlab.metawear.AsyncOperation;

import android.util.Log;

import org.apache.cordova.PluginResult;

import com.mbientlab.metawear.cordova.MWDevice;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.RouteManager.MessageHandler;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.mbientlab.metawear.module.Bmi160Accelerometer;
import com.mbientlab.metawear.UnsupportedModuleException;

import com.mbientlab.metawear.module.Logging;

import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Lance Gleason of Polyglot Programming LLC. on 10/11/2015.
 * http://www.polyglotprogramminginc.com
 * https://github.com/lgleasain
 * Twitter: @lgleasain
 */


public class StepCounter {

    private MWDevice mwDevice;

    public StepCounter(MWDevice device) {
        mwDevice = device;
    }

    private final AsyncOperation.CompletionHandler<RouteManager> stepCountHandler =
            new AsyncOperation.CompletionHandler<RouteManager>() {
                @Override
                public void success(RouteManager result) {
                    Log.i("step counter", "setup callbacks");
                    result.subscribe("step_counter_stream_key", new MessageHandler() {
                        @Override
                        public void process(Message msg) {
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,
                                    "TOOK_A_STEP");
                            pluginResult.setKeepCallback(true);
                            mwDevice.getMwCallbackContexts().get(mwDevice.START_STEP_COUNTER).sendPluginResult(pluginResult);
                            Log.i("Metawear Cordova step", msg.toString());
                        }
                    });
                }
            };

    private Bmi160Accelerometer getAccelerometer() {
        Bmi160Accelerometer accelModule = null;

        try {
            accelModule = mwDevice.getMwBoard().getModule(Bmi160Accelerometer.class);
        } catch (UnsupportedModuleException e) {
            Log.e("Metawear Cordova Error:", e.toString());
        }
        return accelModule;
    }

    public void startStepCounter() {
        Bmi160Accelerometer accelModule = getAccelerometer();
        accelModule.enableStepDetection();
        accelModule.routeData()
                .fromStepDetection().stream("step_counter_stream_key")
                .commit().onComplete(stepCountHandler);

        accelModule.configureStepDetection().commit();
        // Switch the accelerometer to active mode
        accelModule.start();
        Log.i("Step Counter", "Started Detection");
    }

    public void stopStepCounter() {
        getAccelerometer().stop();
    }


    public void startStepCounterLogs(boolean overwrite) {
        try {
            Logging logging = mwDevice.getMwBoard().getModule(Logging.class);
            logging.startLogging(overwrite);

            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            mwDevice.getMwCallbackContexts().get(mwDevice.START_STEP_COUNTER).sendPluginResult(pluginResult);

        } catch (UnsupportedModuleException e) {
            Log.e("Metawear Cordova Error:", e.toString());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            mwDevice.getMwCallbackContexts().get(mwDevice.START_STEP_COUNTER).sendPluginResult(pluginResult);
        }
    }

    public void stopStepCounterLogs() {
        try {
            Logging logging = mwDevice.getMwBoard().getModule(Logging.class);
            logging.stopLogging();

            logging.downloadLog(100, new Logging.DownloadHandler() {
                @Override
                public void onProgressUpdate(int nEntriesLeft, int totalEntries) {
                    super.onProgressUpdate(nEntriesLeft, totalEntries);
                }

                @Override
                public void receivedUnknownLogEntry(byte logId, Calendar timestamp, byte[] data) {
                    super.receivedUnknownLogEntry(logId, timestamp, data);

                    Log.i("MainActivity", String.format("Unknown log entry: {id: %d, data: %s}", logId, Arrays.toString(data)));
                }

                @Override
                public void receivedUnhandledLogEntry(Message logMessage) {
                    super.receivedUnhandledLogEntry(logMessage);
                    Log.i("Metawear Cordova", String.format("Unhandled message: %s", logMessage.toString()));
                }

            }).onComplete(new AsyncOperation.CompletionHandler<Integer>() {
                @Override
                public void success(Integer result) {
                    super.success(result);

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                    pluginResult.setKeepCallback(true);
                    mwDevice.getMwCallbackContexts().get(mwDevice.DOWNLOAD_STEP_COUNTER_LOGS).sendPluginResult(pluginResult);
                }

                @Override
                public void failure(Throwable error) {
                    super.failure(error);
                    Log.e("Metawear Cordova Error:", error.toString());

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
                    mwDevice.getMwCallbackContexts().get(mwDevice.DOWNLOAD_STEP_COUNTER_LOGS).sendPluginResult(pluginResult);

                }
            });
        } catch (UnsupportedModuleException e) {
            Log.e("Metawear Cordova Error:", e.toString());
            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR);
            mwDevice.getMwCallbackContexts().get(mwDevice.DOWNLOAD_STEP_COUNTER_LOGS).sendPluginResult(pluginResult);
        }
    }
}
