package com.preventium.boxpreventium.gps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Franck on 09/08/2016.
 */

public class GetLocation extends Service implements LocationListener {

    private final static String TAG = "GetLocation";

    private Context Context;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    int flag = 0;
    Location location;
    Location mylocation = new Location("");
    Location dest_location = new Location("");
    public double latitude;
    double longitude;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 40;// 40 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 2;
    protected LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "on create");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "destroyed");
        flag = 0;
        stopSelf();
        stopUsingGPS();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {
        // TODO Auto-generated method stub
        Context = this;
        Log.i("tag", "on start");
        mylocation = getLocation(Context);

        Double msg = mylocation.getLatitude();
        Log.i("my long", msg.toString());

        Double dest_lat = intent.getDoubleExtra("lat", 0.0);
        Double dest_lon = intent.getDoubleExtra("lon", 0.0);
        Log.i("get lat", dest_lat.toString());
        Log.i("get lon", dest_lon.toString());

        this.dest_location.setLatitude(dest_lat);
        this.dest_location.setLongitude(dest_lon);
        Log.i("get lon", dest_lon.toString());

        return START_NOT_STICKY;
    }

    public Location getLocation(Context Context) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG,"Missing permisions ACCESS_FINE_LOCATION !!!");
            return location;
        }

        try {
            locationManager = (LocationManager) Context
                    .getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.i(TAG, "No gps and No Network is enabled enable either one of them");
                Toast.makeText(this, "Enable either Network or GPS", Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d(TAG, "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d(TAG, "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public void stopUsingGPS() {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG,"Missing permisions ACCESS_FINE_LOCATION !!!");
                return;
            }
            locationManager.removeUpdates(GetLocation.this);
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        mylocation = getLocation(Context);
        Log.i("Tag", "location changed");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}