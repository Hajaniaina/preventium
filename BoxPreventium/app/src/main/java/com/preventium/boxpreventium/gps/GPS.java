package com.preventium.boxpreventium.gps;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Franck on 09/08/2016.
 */

public class GPS {

    private final static String TAG = "GPS";

    private Context ctx;
    private LocationManager locationManager;
    private Intent intentLocation, intentAlert;
    PendingIntent pendingLocation, pendingAlert;

    String provider;

    public GPS(Context context) {
        ctx = context;
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        intentLocation = new Intent(context, GPSUpdateReceiver.class);
        intentAlert = new Intent(context, GPSAlertReceiver.class);

        pendingLocation = PendingIntent.getBroadcast(context, 0, intentLocation, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingAlert = PendingIntent.getBroadcast(context, 0, intentAlert, PendingIntent.FLAG_UPDATE_CURRENT);

//        ArrayList<LocationProvider> providers = new ArrayList<LocationProvider>();
//        List<String> names = locationManager.getProviders(true);
//        for (String name : names)
//            providers.add(locationManager.getProvider(name));

        Criteria critere = new Criteria();
        // Pour indiquer la précision voulue
        // On peut mettre ACCURACY_FINE pour une haute précision ou ACCURACY_COARSE pour une moins bonne précision
        critere.setAccuracy(Criteria.ACCURACY_FINE);
        // Est-ce que le fournisseur doit être capable de donner une altitude ?
        critere.setAltitudeRequired(true);
        // Est-ce que le fournisseur doit être capable de donner une direction ?
        critere.setBearingRequired(true);
        // Est-ce que le fournisseur peut être payant ?
        critere.setCostAllowed(false);
        // Pour indiquer la consommation d'énergie demandée
        // Criteria.POWER_HIGH pour une haute consommation, Criteria.POWER_MEDIUM pour une consommation moyenne et Criteria.POWER_LOW pour une basse consommation
        critere.setPowerRequirement(Criteria.POWER_HIGH);
        // Est-ce que le fournisseur doit être capable de donner une vitesse ?
        critere.setSpeedRequired(true);
        provider = locationManager.getBestProvider(critere, true);

    }

    public boolean addProximity(double latitude, double longitude, float radius) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission ACCESS_FINE_LOCATION !!!");
            // On ajoute une alerte de proximité si on s'approche ou s'éloigne du bâtiment de Simple IT
            return false;
        }
        locationManager.addProximityAlert(latitude, longitude, radius, -1, pendingAlert);
        return true;
    }

    public boolean start() {
        Log.d(TAG,"Trying to start GPS...");
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission ACCESS_FINE_LOCATION !!!");
            return false;
        }
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(provider, 1000, 15, pendingLocation);
        return true;
    }

    public void stop() {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission ACCESS_FINE_LOCATION !!!");
            return;
        }
        locationManager.removeUpdates( pendingLocation );
        locationManager.removeProximityAlert( pendingLocation );
    }
}
