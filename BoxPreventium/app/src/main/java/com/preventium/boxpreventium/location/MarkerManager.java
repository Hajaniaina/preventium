package com.preventium.boxpreventium.location;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.preventium.boxpreventium.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;
import java.util.Iterator;

public class MarkerManager {

    private static final String TAG = "MarkerManager";
    private static final int NEAR_MARKER_DISTANCE_TH = 1000;

    private ArrayList<CustomMarker> markersList = null;
    private GoogleMap map = null;
    private Activity activity;

    public MarkerManager (Activity activity) {

        this.activity = activity;
        markersList = new ArrayList<>();
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

    public void addAlertCircle(CustomMarker marker) {

        marker.addAlertCircle(map);
    }

    public void showAlertCircle (CustomMarker marker, boolean show) {

        marker.showAlertCircle(show);
    }

    public void fetchNearMarkers (LatLng currPos) {

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.isAlertEnabled()) {

                    float[] results = new float[3];
                    Location.distanceBetween(currPos.latitude, currPos.longitude, customMarker.getPos().latitude, customMarker.getPos().longitude, results);

                    Log.d("ALERT", "Marker: " + customMarker.getTitle() + " is " + results[0] + "m from us");

                    if (results[0] < NEAR_MARKER_DISTANCE_TH) {

                        Log.d("ALERT", "Marker: " + customMarker.getTitle() + " is near");
                        customMarker.setAsNear(true);
                    }
                    else {

                        customMarker.setAsNear(false);
                    }
                }
            }
        }
    }

    public CustomMarker findClosestAlertMarker (LatLng currPos) {

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.isAlertEnabled() && !customMarker.isAlertAlreadyActivated() && customMarker.isNear()) {

                    float[] results = new float[3];
                    Location.distanceBetween(currPos.latitude, currPos.longitude, customMarker.getPos().latitude, customMarker.getPos().longitude, results);

                    if (results[0] <= customMarker.getAlertRadius()) {

                        return customMarker;
                    }
                }
            }
        }

        return null;
    }

    public void hideAllAlertCircles() {

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();
                customMarker.showAlertCircle(false);
            }
        }
    }

    public Marker addMarker (String title, LatLng currPos, int type, boolean editable) {

        CustomMarker customMarker = new CustomMarker();

        customMarker.setEditable(editable);
        customMarker.setPos(currPos);
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

    public boolean removeMarker (int markerType) {

        boolean found = false;

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.getType() == markerType) {

                    customMarker.removeAlertCircle();
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

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.equals(marker)) {

                    customMarker.removeAlertCircle();
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

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();
                customMarker.removeAlertCircle();
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
