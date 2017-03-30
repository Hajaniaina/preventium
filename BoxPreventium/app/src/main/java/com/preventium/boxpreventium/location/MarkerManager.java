package com.preventium.boxpreventium.location;

import android.app.Activity;

import com.preventium.boxpreventium.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Iterator;

public class MarkerManager {

    private static final String TAG = "MarkerManager";

    private ArrayList<CustomMarker> markersList = null;
    private GoogleMap map = null;
    private Activity activity;

    public MarkerManager (Activity activity) {

        this.activity = activity;
        markersList = new ArrayList<CustomMarker>();
        markersList.clear();
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

    public CustomMarker getMarker (Marker marker) {

        CustomMarker foundedMarker = null;

        if (markersList.size() > 0) {

            for (CustomMarker customMarker : markersList) {

                if (marker.getPosition().equals(customMarker.getPos())) {

                    foundedMarker = customMarker;
                }
            }
        }

        return foundedMarker;
    }

    public Marker addMarker (String title, LatLng pos, int type, boolean editable) {

        CustomMarker customMarker = new CustomMarker();

        customMarker.setEditable(editable);
        customMarker.setPos(pos);
        customMarker.setTitle(title);
        customMarker.setType(type);

        if (editable) {

            customMarker.setSnippet(activity.getString(R.string.marker_snippet_string));
        }

        Marker marker = customMarker.addToMap(map);
        markersList.add(customMarker);

        return marker;
    }

    public Marker addMarker (CustomMarkerData data) {

        CustomMarker customMarker = new CustomMarker(data);
        customMarker.setEditable(false);

        Marker marker = customMarker.addToMap(map);
        markersList.add(customMarker);

        return marker;
    }

    public boolean removeMarker (int markerType)
    {
        boolean found = false;

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.getType() == markerType) {

                    iterator.remove();
                    found = true;
                }
            }
        }

        return found;
    }

    public boolean removeMarker (CustomMarker marker) {

        boolean found = false;

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext(); ) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.equals(marker)) {

                    iterator.remove();
                    found = true;
                }
            }
        }

        return found;
    }

    public boolean removeMarker (Marker marker) {

        return removeMarker(getMarker(marker));
    }

    public void removeAllMarkers() {

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext(); ) {

                CustomMarker customMarker = iterator.next();
                iterator.remove();
            }
        }
    }

    public ArrayList<CustomMarkerData> getUserMarkersData() {

        ArrayList<CustomMarkerData> markersDataList = new ArrayList<CustomMarkerData>();

        for (CustomMarker customMarker : markersList) {

            if (customMarker.isEditable()) {

                CustomMarkerData markerData = new CustomMarkerData();

                markerData.alert = customMarker.isAlertEnabled();
                markerData.position = customMarker.getPos();
                markerData.alertRadius = customMarker.getAlertRadius();
                markerData.title = customMarker.getTitle();
                markerData.type = customMarker.getType();

                markersDataList.add(markerData);
            }
        }

        return markersDataList;
    }
}
