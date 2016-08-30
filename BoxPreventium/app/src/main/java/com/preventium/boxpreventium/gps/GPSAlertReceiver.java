package com.preventium.boxpreventium.gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Franck on 09/08/2016.
 */

public class GPSAlertReceiver extends BroadcastReceiver {
    private final static String TAG = "GPSAlertReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enter = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);
        Log.d(TAG,"Received proximity alert:" + enter);
    }
}
