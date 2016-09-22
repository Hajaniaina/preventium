package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class PositionManager implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PositionManager";

    private Context context;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private PositionListener posListener;
    private LatLng currPos, lastPos;
    private long updateInterval = 5000;
    private long updateDistance = 0;
    private boolean updateEnabled = false;

    public interface PositionListener {

        public void onPositionChanged (Location location);
    }

    public PositionManager (Activity activity) {

        context = activity.getApplicationContext();
        posListener = null;

        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    public void setOnPositionChangedListener (PositionListener listener) {

        this.posListener = listener;
    }

    @Override
    public void onConnectionSuspended (int i) {}

    @Override
    public void onConnectionFailed (@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onConnected (@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(updateInterval);
        locationRequest.setSmallestDisplacement(updateDistance);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged (Location location) {

        lastPos = currPos;
        currPos = new LatLng(location.getLatitude(), location.getLongitude());

        if (posListener != null) {

            posListener.onPositionChanged(location);
        }

        Log.d(TAG, "Location changed");
    }

    public void changeUpdateInterval (long ms) {

        boolean enabled = updateEnabled;

        if (enabled) {

            enableUpdates(false);
        }

        locationRequest.setInterval(ms);
        updateInterval = ms;

        if (enabled) {

            enableUpdates(true);
        }
    }

    public void changeUpdateDistance (long meters) {

        boolean enabled = updateEnabled;

        if (enabled) {

            enableUpdates(false);
        }

        locationRequest.setSmallestDisplacement(updateDistance);
        updateDistance = meters;

        if (enabled) {

            enableUpdates(true);
        }
    }

    public boolean checkPermission() {

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            Log.d("POS", "ACCESS_FINE_LOCATION permission disabled");
            return false;
        }

        return true;
    }

    public void enableUpdates (boolean enable) {

        updateEnabled = true;

        if (enable) {

            if (!updateEnabled) {

                if (googleApiClient != null) {

                    checkPermission();
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                }
            } else {

                if (updateEnabled) {

                    if (googleApiClient != null) {

                        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                    }
                }
            }
        }
    }
}
