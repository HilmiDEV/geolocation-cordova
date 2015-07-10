package com.geomatys.android.location;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.ConnectActionListener;

import java.util.Date;


/**
 * Created by christophem on 12/06/15.
 */
public class PublishService extends Service {

    private final IBinder binder = new LocalBinder();

    private MqttAndroidClient client;

    private Date publishDate = null;


    public class LocalBinder extends Binder {
        PublishService getService() {
            return PublishService.this;
        }
    }

    @Override
    public void onDestroy() {
        try {
            client.disconnect();
            Log.i(LocationService.TAG, "publish Servive destroy");
        } catch (MqttException e) {
            Log.e(LocationService.TAG, "on Destroy MqttException", e);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            client.disconnect();
            Log.i(LocationService.TAG, "publish Servive unBind");
        } catch (MqttException e) {
            Log.e(LocationService.TAG, "on Unbind MqttException", e);
        }
        return true;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LocationService.TAG, "on bind publish service");
        client = new MqttAndroidClient(getApplicationContext(), intent.getStringExtra("url"), intent.getStringExtra("identifier"));
        try {
            client.connect();
        } catch (MqttException e) {
            Log.e(LocationService.TAG,"can't connect mqtt broker",e);
        }
        return binder;
    }


    public Date push(final Location location) {
        Log.i(LocationService.TAG, "(PublishService) Accuracy=" + location.getAccuracy());
        Log.i(LocationService.TAG, "(PublishService) Altitude=" + location.getAltitude());
        Log.i(LocationService.TAG, "(PublishService) Provider=" + location.getProvider());
        try {
            if (client.isConnected()) {
                publishDate = publishLocation(location);
            } else {
                client.connect();
            }
        } catch (MqttPersistenceException e) {
            Log.e(LocationService.TAG, "MqttPersistenceException", e);
        } catch (MqttException e) {
            Log.e(LocationService.TAG, "MqttException", e);
        }
        return publishDate;
    }

    private Date publishLocation(Location location) throws MqttException {
        MqttMessage message = new MqttMessage(
                LocationPlugin.createLocationEvent(
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAccuracy(),
                        new Date(),
                        client.getClientId()
                ).toString().getBytes()
        );
        final IMqttDeliveryToken token = client.publish("/location", message);
        token.waitForCompletion();
        return new Date();

    }


}
