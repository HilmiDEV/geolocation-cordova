package com.geomatys.android.location;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.*;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by christophem on 12/06/15.
 */
public class PublishService extends Service {
    /**
     * intanciate the receiver message from Location Service
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Location location = (Location) bundle.get(LocationService.LOCATION);
                Log.i(LocationService.TAG, "(Service) Accuracy=" + location.getAccuracy());
                Log.i(LocationService.TAG, "(Service) Altitude=" + location.getAltitude());
                Log.i(LocationService.TAG, "(Service) Provider=" + location.getProvider());
//                Toast.makeText(context, R.string.changed_location_detected
//                        , Toast.LENGTH_LONG).show();
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * call when service start
     * @param intent the starter intent
     * @param flags params flags
     * @param startId params startId tag
     * @return START_NOT_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LocationService.TAG, "publish service started");
        registerReceiver(receiver, new IntentFilter(LocationService.PUBLISH_NOTIFICATION));
        return Service.START_NOT_STICKY;
    }


}
