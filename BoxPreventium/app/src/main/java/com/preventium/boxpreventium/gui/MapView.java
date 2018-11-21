package com.preventium.boxpreventium.gui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.preventium.boxpreventium.R;

/**
 * Created by tog on 06/11/2018.
 */

public class MapView implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnInfoWindowClickListener {
    // variable
    private Context context;
    private MainActivity main;

    // constructor
    public MapView (Context context) {
        this.context = context;
        main = getMain(context);
    }

    private MainActivity getMain(Context context) {
        return (MainActivity) context;
    }

    /* long clique sur la carte */
    @Override
    public void onMapLongClick(final LatLng latLng) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(context.getString(R.string.validate_marker_create_string));
        alertBuilder.setNegativeButton(context.getString(R.string.cancel_string), null);
        alertBuilder.setPositiveButton(context.getString(R.string.create_string), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                main.showMarkerEditDialog(main.getMarkerManager().addMarker(main.getGoogleMap(), "", "", latLng,
                        13, false), true);
            }
        });
        alertBuilder.create().show();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        main.getMarkerManager().hideAllAlertCircles(); // à modifier
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (main.getMarkerManager().getMarker(marker).isEditable()) {
            main.showInfoDialog(marker);
        }
    }
}
