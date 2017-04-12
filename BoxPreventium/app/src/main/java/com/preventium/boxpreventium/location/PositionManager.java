package com.preventium.boxpreventium.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class PositionManager implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PositionManager";

    private static final int ACC_NONE = 0;
    private static final int ACC_POS = 1;
    private static final int ACC_NEG = 2;

    public static final float MS_TO_KMPH = 3.6f;
    private static final float MOVING_MIN_SPEED_KMPH = 5.0f;

    private static final float UPDATE_SPEED_MAX_MPS = (200.0f / MS_TO_KMPH);
    private static final float UPDATE_DISTANCE_MIN_M = 15.0f;
    private static final float UPDATE_DISTANCE_MAX_M = 1000.0f;
    private static final float UPDATE_DELTA_T_MAX_MS = (30.0f * 1000);

    private static final float GPS_SNR_AVG_MIN = 10.0f;
    private static final float GPS_SNR_TH = 15.0f;

    private Context context;
    private LocationRequest locationRequest;
    private GpsStatus gpsStatus;
    private LocationManager manager;
    private GoogleApiClient googleApiClient;
    private Location refLocation = null, currLocation;
    private long updateIntervalMs = 100;
    private boolean moving = false;
    private boolean updateEnabled = true;
    private ArrayList<Location> lastLocList;

    private List<PositionListener> posListeners;

    public interface PositionListener {

        public void onPositionUpdate(Location prevLoc, Location currLoc);
        public void onRawPositionUpdate(Location location);
        public void onGpsStatusChange(boolean gpsFix);
    }

    public PositionManager(Activity activity) {

        context = activity.getApplicationContext();

        posListeners = new ArrayList<PositionListener>();

        lastLocList = new ArrayList<Location>();

        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        checkPermission();
        manager.addGpsStatusListener(new GpsStatus.Listener() {

            @Override
            public void onGpsStatusChanged (int event) {

                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
                {
                    checkPermission();
                    gpsStatus = manager.getGpsStatus(gpsStatus);

                    int satNum = 0;
                    float snrAvg = 0;
                    boolean status = false;

                    Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();

                    for (GpsSatellite satelite : satellites) {

                        double snr = satelite.getSnr();

                        if (satelite.getSnr() > GPS_SNR_AVG_MIN) {

                            snrAvg += snr;
                            satNum++;
                        }
                    }

                    if (satNum > 0) {

                        snrAvg /= satNum;

                        if (snrAvg >= GPS_SNR_TH) {

                            status = true;
                        }
                    }

                    for (PositionListener posListener : posListeners) {

                        posListener.onGpsStatusChange(status);
                    }
                }
            }
        });

        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended (int i) {

        Log.d(TAG, "Google API connection suspended");
    }

    @Override
    public void onConnectionFailed (@NonNull ConnectionResult connectionResult) {

        Log.d(TAG, "Google API connection failed");
    }

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

        if (refLocation == null) {

            refLocation = location;
        }

        currLocation = location;

        if ((currLocation.getTime() - refLocation.getTime()) > UPDATE_DELTA_T_MAX_MS) {

            refLocation = currLocation;
        }

        if (refLocation.distanceTo(currLocation) < UPDATE_DISTANCE_MAX_M) {

            if ((!currLocation.hasSpeed()) || (currLocation.getSpeed() < UPDATE_SPEED_MAX_MPS)) {

                if (refLocation.distanceTo(currLocation) > UPDATE_DISTANCE_MIN_M) {

                    for (PositionListener posListener : posListeners) {

                        posListener.onPositionUpdate(refLocation, currLocation);
                    }

                    refLocation = currLocation;
                }

                for (PositionListener posListener : posListeners) {

                    posListener.onRawPositionUpdate(currLocation);
                }
            }
        }
    }

    public boolean isGpsAvailable() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "GPS permissions fail");
            return false;
        }

        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        return locationAvailability.isLocationAvailable();
    }

    public boolean isMoving() {

        return moving;
    }

    public void enableUpdates (boolean enable) {

        if (enable) {

            if (!updateEnabled) {

                lastLocList.clear();

                checkPermission();
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            } else {

                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
        }

        updateEnabled = enable;
    }

    public boolean isUpdatesEnabled() {

        return updateEnabled;
    }

    public int getInstantSpeed() {

        float speed = (currLocation.getSpeed() * MS_TO_KMPH);
        return (int) speed;
    }

    public void setUpdateInterval(long ms) {

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

            Log.d(TAG, "ACCESS_FINE_LOCATION permission disabled");
            return false;
        }

        return true;
    }

    public void setPositionChangedListener (PositionListener listener) {

        posListeners.add(listener);
    }

    public static boolean isLocationEnabled (Context context) {

        boolean enabled = false;

        try {

            if (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF) {

                enabled = true;
            }
        }
        catch (Settings.SettingNotFoundException e) {

            e.printStackTrace();
        }

        return enabled;
    }
}
