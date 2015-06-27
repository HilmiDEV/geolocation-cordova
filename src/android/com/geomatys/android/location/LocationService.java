package com.geomatys.android.location;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;



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

    public void initLocation() {
        Log.i(TAG,"start location");
        mGoogleApiClient.connect();
    }

    public void startLocation() {
        Log.i(TAG, "start location");
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
            LocationPlugin.sendNotification(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()), String.valueOf(mLastLocation.getAccuracy()));
        }
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
        LocationPlugin.sendNotification(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),String.valueOf(location.getAccuracy()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"on bind service");

        return binder;
    }
}
