package com.geomatys.android.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;


/**
 * Created by christophem on 23/06/15.
 */
public class LocationPlugin extends CordovaPlugin {

    private static JSONObject cachedLocationEvent;
    private static CallbackContext callbackContext;
    private static boolean foreground;

    private LocationService service;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            LocationPlugin.this.service = binder.getService();
            Log.i(LocationService.TAG,"loc service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.i(LocationService.TAG,"ccordova init");
        Intent intent = new Intent(cordova.getActivity(), LocationService.class);
        cordova.getActivity().getApplicationContext().startService(intent);
        cordova.getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        cordova.getActivity().unbindService(connection);
    }

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        try {
            return invokeService(new PluginCommand(action, data, callbackContext));
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            PrintWriter err = new PrintWriter(writer);
            e.printStackTrace(err);
            Log.e(LocationService.TAG, writer.toString());
            callbackContext.error(e.getMessage());
        }

        return false;
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        foreground = false;
    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        foreground = true;
    }

    private boolean invokeService(final PluginCommand pluginCommand) throws JSONException {
        if (service != null) {
            Log.i(LocationService.TAG, "invokeService");
            if ("initLoc".equals(pluginCommand.getAction())) {
                service.initLocation(pluginCommand.getData());
                callbackContext = pluginCommand.getCallbackContext();
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);


                return true;
            }
            if ("startLoc".equals(pluginCommand.getAction())) {
                service.startLocation(pluginCommand.getData());
                callbackContext = pluginCommand.getCallbackContext();
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);


                return true;
            }
            if ("stopLoc".equals(pluginCommand.getAction())) {
                service.stopLocation();
                callbackContext = pluginCommand.getCallbackContext();
                PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);


                return true;
            }
        }
        return true;
    }

    void fireLocationChangedEvent(final Intent intent) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendNotification(intent.getExtras());
            }
        });
    }

    public static void sendNotification(double lat, double lon, float accurancy) {
        sendNotification(createLocationEvent(lat, lon, accurancy, null, null));
    }

    public static void sendNotification(Bundle bundle) {
        if (bundle != null) {
            final double lat = Double.parseDouble(bundle.getString("lat"));
            final double lon = Double.parseDouble(bundle.getString("lon"));
            final int accurancy = Integer.parseInt(bundle.getString("accurancy"));
            sendNotification(createLocationEvent(lat, lon, accurancy, null, null));
        }
    }

    private static void sendNotification(JSONObject locationEvent) {
        if (callbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, locationEvent);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            cachedLocationEvent = locationEvent;
        }
    }

    public static JSONObject createLocationEvent(double lat, double lon, float accurancy, Date publishDate, String clientId) {
        JSONObject data = new JSONObject();
        JSONObject coords = new JSONObject();
        try {
            coords.put("latitude", lat);
            coords.put("longitude", lon);
            coords.put("accuracy", accurancy);
            data.put("coords",coords);
            if (publishDate != null) {
                data.put("publishDate", publishDate);
            }
            if (clientId != null){
                data.put("clientId", clientId);
            }


        } catch (JSONException e) {
            throw new RuntimeException("could not create json object", e);
        }
        return data;
    }

    public static void sendNotification(double latitude, double longitude, float accurancy, Date publishDate) {
        sendNotification(createLocationEvent(latitude, longitude, accurancy, publishDate, null));
    }

    private static class PluginCommand {
        private final String action;
        private final JSONArray data;
        private final CallbackContext callbackContext;

        private PluginCommand(String action, JSONArray data, CallbackContext callbackContext) {
            this.action = action;
            this.data = data;
            this.callbackContext = callbackContext;
        }

        public String getAction() {
            return action;
        }

        public JSONArray getData() {
            return data;
        }

        public CallbackContext getCallbackContext() {
            return callbackContext;
        }
    }
}
