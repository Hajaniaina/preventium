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
import android.widget.Toast;

/**
 * Created by Franck on 10/08/2016.
 */

public class GPSLocationService extends Service {

    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1000; // in  Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 60000; // in Milliseconds
    protected LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "GPS Service created ...", Toast.LENGTH_LONG).show();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(GPSLocationService.this, "MISSING PERMISSION !!!", Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATES,
                MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new GPSLocationListener());
        Location location = locationManager
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        float latitude = (float) location.getLatitude();
        float longitude = (float) location.getLongitude();
        String message = String.format(
                "Current Location \n Longitude: %1$s \n Latitude: %2$s",
                location.getLongitude(), location.getLatitude());
        Toast.makeText(GPSLocationService.this, message, Toast.LENGTH_LONG).show();

    }

    public class GPSLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(), location.getLatitude());
            Toast.makeText(GPSLocationService.this, message, Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Toast.makeText(GPSLocationService.this, "Provider status changed",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(GPSLocationService.this,
                    "Provider disabled by the user. GPS turned off",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(GPSLocationService.this,
                    "Provider disabled by the user. GPS turned off",
                    Toast.LENGTH_LONG).show();
        }
    }
}
