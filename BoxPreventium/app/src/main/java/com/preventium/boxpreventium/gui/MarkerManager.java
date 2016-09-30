package com.preventium.boxpreventium.gui;

import com.preventium.boxpreventium.gui.CustomMarker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MarkerManager {

    private static final String TAG = "MarkerManager";
    private ArrayList<CustomMarker> markersList = null;
    private GoogleMap map = null;

    MarkerManager() {

        markersList = new ArrayList<CustomMarker>();
        markersList.clear();
    }

    MarkerManager (GoogleMap map) {

        if (this.map == null) {

            this.map = map;
            markersList = new ArrayList<CustomMarker>();
            markersList.clear();
        }
    }

    public String toString() {

        if (markersList.size() > 0) {

            int i = 0;
            String str = "";

            for (CustomMarker customMarker : markersList) {

                str += String.valueOf(i) + ": [" + customMarker.toString() + "]\n";
                i++;
            }

            return str;
        }

        return "null";
    }

    public void setMap (GoogleMap map) {

        if (map != null) {

            this.map = map;
        }
    }

    public CustomMarker getCustomMarker (Marker marker) {

        CustomMarker foundedMarker = null;

        if (markersList.size() > 0) {

            for (CustomMarker customMarker : markersList) {

                if (customMarker.getMarker().equals(marker)) {

                    foundedMarker = customMarker;
                }
            }
        }

        return foundedMarker;
    }

    public CustomMarker addMarker (String title, LatLng pos, int type) {

        CustomMarker customMarker = new CustomMarker(map);

        customMarker.setPos(pos);
        customMarker.setTitle(title);
        customMarker.setType(type);
        customMarker.addToMap();

        markersList.add(customMarker);
        return customMarker;
    }

    public CustomMarker addMarker (CustomMarker marker) {

        if (marker != null) {

            markersList.add(marker);
            return marker;
        }

        return null;
    }

    public boolean remove (CustomMarker marker) {

        if (markersList.size() > 0) {

            for (CustomMarker customMarker : markersList) {

                if (customMarker.equals(marker)) {

                    markersList.remove(customMarker);
                    marker.getMarker().remove();

                    return true;
                }
            }
        }

        return false;
    }

    public boolean remove (Marker marker) {

        CustomMarker customMarker = getCustomMarker(marker);

        if (remove(customMarker))
        {
            return true;
        }

        return false;
    }
}
