package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

    private static final int ACC_NONE = 0;
    private static final int ACC_POS  = 1;
    private static final int ACC_NEG  = 2;

    private static final float MS_TO_KMH = 3.6f;
    private static final float MOVING_MIN_SPEED_KMH = 5.0f;
    private static final float UPDATE_DISTANCE_METERS = 10.0f;

    private Context context;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location refLocation = null, currLocation;
    private long updateIntervalMs = 1000;
    private boolean updateEnabled = false;
    private boolean moving = false;
    private ArrayList<Location> lastLocList;

    private List<PositionListener> posListeners;
    private List<MovingStateListener> movListeners;
    private List<AccStateListener> accListeners;

    public interface PositionListener {

        public void onPositionUpdate (Location prevLoc, Location currLoc);
        public void onRawPositionUpdate (Location location);
    }

    public interface MovingStateListener {

        public void onStartMoving();
        public void onStopMoving();
    }

    public interface AccStateListener {

        public void onAccelerating();
        public void onDeAccelerating();
    }

    public PositionManager (Activity activity) {

        context = activity.getApplicationContext();

        posListeners = new ArrayList<PositionListener>();
        movListeners = new ArrayList<MovingStateListener>();
        accListeners = new ArrayList<AccStateListener>();

        lastLocList = new ArrayList<Location>();

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

        for (PositionListener posListener : posListeners) {

            if (refLocation.distanceTo(currLocation) >= UPDATE_DISTANCE_METERS) {

                Log.d(TAG, "Position Update");
                posListener.onPositionUpdate(refLocation, currLocation);
                refLocation = currLocation;
            }

            Log.d(TAG, "Raw Position Update");
            posListener.onRawPositionUpdate(currLocation);
        }

        /*
        boolean movingState = checkMovingState(5);

        if (movingState) {

            if (!moving) {

                moving = true;

                for (MovingStateListener movListener : movListeners) {

                    movListener.onStartMoving();
                }
            }
        }
        else {

            if (moving) {

                moving = false;

                for (MovingStateListener movListener : movListeners) {

                    movListener.onStopMoving();
                }
            }
        }

        int accState = checkAccState(3);

        if (accState == ACC_POS) {

            for (AccStateListener accListener : accListeners) {

                accListener.onAccelerating();
            }
        }
        else if (accState == ACC_NEG) {

            for (AccStateListener accListener : accListeners) {

                accListener.onDeAccelerating();
            }
        }

        boolean constSpeed = checkConstSpeed(5);

        if (lastLocList.size() > 5) {

            lastLocList.remove(0);
        }

        lastLocList.add(location);
        */
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

                if (updateEnabled) {

                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                }
            }
        }

        updateEnabled = enable;
    }

    public int getInstantSpeed() {

        float speed = (currLocation.getSpeed() * MS_TO_KMH);
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

    public void setPositionChangedListener(PositionListener listener) {

        posListeners.add(listener);
    }

    public void setMovingListener (MovingStateListener listener) {

        movListeners.add(listener);
    }

    public void setAccListener (AccStateListener listener) {

        accListeners.add(listener);
    }

    private boolean checkMovingState (int points) {

        boolean movingState = false;

        if (lastLocList.size() >= points) {

            double avgSpeed = 0.0;

            for (int i = 0; i < points; i++) {

                avgSpeed += (lastLocList.get(i).getSpeed() * MS_TO_KMH);
            }

            avgSpeed = (avgSpeed / (double) points);

            if (avgSpeed >= MOVING_MIN_SPEED_KMH) {

                Log.d(TAG, "Start Moving. Speed: " + String.valueOf(avgSpeed) + " km/h");
                movingState = true;
            }
            else {

                Log.d(TAG, "Stop Moving. Speed: " + String.valueOf(avgSpeed) + " km/h");
            }
        }

        return movingState;
    }

    private int checkAccState (int points) {

        float errMargin = 1.0f;

        if (lastLocList.size() > points) {

            List<Location> tailList = lastLocList.subList((lastLocList.size() - points), lastLocList.size());
            float[] speeds = new float[points];

            for (int i = 0; i < tailList.size(); i++) {

                speeds[i] = tailList.get(i).getSpeed();
            }

            boolean accPos = true;

            for (int i = 0; i < (speeds.length - 1); i++) {

                if (speeds[i] < (speeds[i + 1] + errMargin)) {

                    accPos = false;
                }
            }

            if (accPos) {

                Log.d(TAG, "Pos Acc");
                return ACC_POS;
            }

            boolean accNeg = true;

            for (int i = 0; i < (speeds.length - 1); i++) {

                if ((speeds[i] + errMargin) >= speeds[i + 1]) {

                    accNeg = false;
                }
            }

            if (accNeg) {

                Log.d(TAG, "Neg Acc");
                return ACC_NEG;
            }
        }

        Log.d(TAG, "No Acc");
        return ACC_NONE;
    }

    private boolean checkConstSpeed (int points) {

        boolean stable = false;
        double errMargin = 2.0;

        if (lastLocList.size() >= points) {

            double[] speedList = new double[points];

            for (int i = 0; i < speedList.length; i++) {

                if (lastLocList.get(i).hasSpeed()) {

                    speedList[i] = 0.0;
                }
                else {

                    speedList[i] = lastLocList.get(i).getSpeed();
                }
            }

            double variance = getVariance(speedList, points);

            Log.d(TAG, "Speed Variance: " + String.valueOf(variance));

            if (variance <= errMargin) {

                Log.d(TAG, "Const Speed");
                stable = true;
            }
        }

        return stable;
    }

    private double getVariance (double values[], int size) {

        if (size < 1) {

            return 0.0;
        }

        double sum = 0.0;

        for (int i = 0; i < size; ++i) {

            sum += values[i];
        }

        double mean = sum / size;
        double sqDiffsum = 0.0;

        for (int i = 0; i < size; ++i) {

            double diff = values[i] - mean;
            sqDiffsum += diff * diff;
        }

        double variance = sqDiffsum / size;
        return variance;
    }

    private double getStdDeviation (double values[], int size) {

        if (size < 1) {

            return 0.0;
        }

        double variance = getVariance(values, size);
        return Math.sqrt(variance);
    }
}
