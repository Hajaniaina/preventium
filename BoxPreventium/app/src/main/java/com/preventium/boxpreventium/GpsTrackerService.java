package com.preventium.boxpreventium;

import android.*;
import android.Manifest;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by Franck on 24/06/2016.
 */

public class GpsTrackerService extends Service implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "GpsTrackerService";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location lastLocation = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }
        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    private void startTracking() {
        Log.d(TAG, "startTracking");
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI == null) {
            Log.w(TAG, "GoogleApiAvailability instance is null !");
        } else {
            if (googleAPI.isGooglePlayServicesAvailable(MainApplication.getAppContext()) == ConnectionResult.SUCCESS) {

                googleApiClient = new GoogleApiClient.Builder(MainApplication.getAppContext())
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                if (!googleApiClient.isConnected() || !googleApiClient.isConnecting())
                    googleApiClient.connect();

            } else {
                Log.e(TAG, "Unable to connect to google play services.");
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d( TAG, "GoogleApiClient connection has been suspend." );
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d( TAG, "onConnectionFailed" );
        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        if( location != null ) {
            Log.d( TAG, "Position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy() );
            if( location.hasSpeed() ) Log.d( TAG, "Speed: " + location.getSpeed() ); // Get the speed if it is available, in meters/second over ground.
            //Log.d( TAG, "ElapsedRealtimeNanos: " + location.getElapsedRealtimeNanos() );

            if( lastLocation != null ){
                float distance = lastLocation.distanceTo(location); //Returns the approximate distance in meters between this location and the given location.
                Log.d( TAG, "Distance: " + distance );
                if( distance > 5.0f ){
                    Log.d(TAG,"NEW LOCATION");
                }
            } else {
                lastLocation = location;
                Log.d(TAG,"NEW LOCATION");
            }
        }
    }
}
