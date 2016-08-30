package com.preventium.boxpreventium.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Franck on 09/08/2016.
 */

public class GPSUpdateReceiver extends BroadcastReceiver {

    private final static String TAG = "GPSUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Location location = (Location)intent.getParcelableExtra(LocationManager.KEY_LOCATION_CHANGED);
        Log.d(TAG,"Location received:" + locationStringFromLocation(location) );
    }

    public static String locationStringFromLocation(final Location location) {
        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
    }
}
