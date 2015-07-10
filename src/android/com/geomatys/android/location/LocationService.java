package com.geomatys.android.location;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;


/**
 * Created by christophem on 12/06/15.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "location-service";
    public static final String PUBLISH_NOTIFICATION = "com.geomatys.android.location.publish" ;
    public static final String LOCATION = "location";
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    /**
     * Represents the location request
     */
    private LocationRequest mLocationRequest;
    /**
     * Define "normal" polling frequency
     */
    private static final long POLLING_FREQ = 1000 * 30;
    /**
     * Define "Fastest" polling frequency
     */
    private static final long FASTEST_UPDATE_FREQ = 1000 * 5;

    private final IBinder binder = new LocalBinder();

    private PublishService publishService;


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            PublishService.LocalBinder binder = (PublishService.LocalBinder) service;
            LocationService.this.publishService = binder.getService();
            Log.i(LocationService.TAG,"publish service binded");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void initLocation(JSONArray data) {
        Log.i(TAG,"init location");
        Log.i(TAG,"params="+data);
        mGoogleApiClient.connect();
    }

    public void startLocation(JSONArray data) {
        Log.i(TAG, "start location");
        Log.i(TAG,"params="+data);
        try {
            if (data.length() > 0 && data.getString(0).equalsIgnoreCase("mqtt")) {
                Intent intent = new Intent(this.getApplicationContext(), PublishService.class);
                intent.putExtra("url",data.getString(1));
                intent.putExtra("identifier",data.getString(2));
                this.getApplicationContext().startService(intent);
                this.bindService(intent,connection, Context.BIND_AUTO_CREATE);
            }
        } catch (JSONException ex){
            Log.e(TAG,"couldn't get publish param",ex);
        }
        if (!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(POLLING_FREQ);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_FREQ);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (mLastLocation != null) {
            LocationPlugin.sendNotification(mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAccuracy());
        }
    }

    public void startgeofencing(){

    }

    public void stopLocation() {
        mGoogleApiClient.disconnect();
        Log.i(TAG,"stop location");
    }




    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
    /**
     * Starter service method
     * @param intent the starter intent
     * @param flags params flags
     * @param startId params startId tag
     * @return START_NOT_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "service start command");
        return Service.START_NOT_STICKY;


    }


    @Override
    public void onCreate() {
        Log.i(TAG, "service created");
        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }



    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    /**
     * Destroy Service method
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            stopLocation();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "google API connected");

    }

    /**
     * Runs when a GoogleApiClient object fail to connect.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Runs when a GoogleApiClient object recieve connection suspended .
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Runs when location Change Detected
     * @param location the current location spawned
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Loc changed");
        if (publishService != null){
            final Date publishDate = publishService.push(location);
            LocationPlugin.sendNotification(location.getLatitude(), location.getLongitude(), location.getAccuracy(), publishDate);
        } else {
            LocationPlugin.sendNotification(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }



    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"on bind service");

        return binder;
    }
}
