package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class PositionManager implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PositionManager";
    private static final float uiUpdateDistanceMeters = 100f;
    private static final float MS_TO_KMH = 3.6f;

    private Context context;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location refLocation, currLocation, lastLocation;
    private long updateIntervalMs = 100;
    private boolean updateEnabled = false;
    private boolean firstEntry = true;
    private ArrayList<Location> locList;
    private List<PositionListener> registeredListeners;

    public interface PositionListener {

        public void onPositionUpdate (Location location);
        public void onRawPositionUpdate (Location location);
    }

    public PositionManager (Activity activity) {

        context = activity.getApplicationContext();
        registeredListeners = new ArrayList<PositionListener>();

        locList = new ArrayList<Location>();

        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    public void setOnPositionChangedListener (PositionListener listener) {

        registeredListeners.add(listener);
    }

    @Override
    public void onConnectionSuspended (int i) {}

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {}

    @Override
    public void onConnected (Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(updateIntervalMs);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged (Location location) {

        if (firstEntry) {

            refLocation = location;
            firstEntry = false;
        }

        lastLocation = currLocation;
        currLocation = location;

        for (PositionListener listener : registeredListeners) {

            listener.onRawPositionUpdate(location);

            if (currLocation.distanceTo(refLocation) > uiUpdateDistanceMeters) {

                refLocation = currLocation;
                listener.onPositionUpdate(location);
            }
        }

        locList.add(location);
    }

    public boolean isMoving() {

        int ptsNum = 10;
        double avgSpeed = 0;
        boolean moving = false;

        if (locList.size() >= ptsNum) {

            List<Location> tailList = locList.subList((locList.size() - ptsNum), locList.size());

            for (Location loc : tailList) {

                if (loc.hasSpeed()) {

                    avgSpeed += (loc.getSpeed() * MS_TO_KMH);
                }
                else {

                    ptsNum--;
                }
            }

            avgSpeed = (avgSpeed / (float) ptsNum);

            if (avgSpeed > 5f) {

                moving = true;
            }
        }

        return moving;
    }

    public int getLastSpeed() {

        float speed = (currLocation.getSpeed() * MS_TO_KMH);
        return (int) speed;
    }

    public void changeUpdateInterval (long ms) {

        boolean enabled = updateEnabled;

        if (enabled) {

            enableUpdates(false);
        }

        locationRequest.setInterval(ms);
        updateIntervalMs = ms;

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

        if (enable) {

            if (!updateEnabled) {

                locList.clear();

                checkPermission();
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            } else {

                if (updateEnabled) {

                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                }
            }
        }

        updateEnabled = enable;
    }
}
