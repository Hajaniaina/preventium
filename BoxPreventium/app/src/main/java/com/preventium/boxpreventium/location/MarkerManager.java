package com.preventium.boxpreventium.location;

import android.content.Context;
import android.location.Location;
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
    private Context context;

    public MarkerManager (Context context) {

        this.context = context;
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

    public void addAlertCircle (GoogleMap map, CustomMarker marker) {

        marker.addAlertCircle(map);
    }

    public void showAlertCircle (CustomMarker marker, boolean show) {

        marker.showAlertCircle(show);
    }

    public void fetchNearMarkers (LatLng currPos) {

        if (markersList.size() > 0) {

            for (Iterator<CustomMarker> iterator = markersList.iterator(); iterator.hasNext();) {

                CustomMarker customMarker = iterator.next();

                if (customMarker.isAlertEnabled() && !customMarker.isAlertAlreadyActivated()) {

                    float[] results = new float[3];
                    Location.distanceBetween(currPos.latitude, currPos.longitude, customMarker.getPos().latitude, customMarker.getPos().longitude, results);

                    if (results[0] < NEAR_MARKER_DISTANCE_TH) {

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

    public Marker addMarker (GoogleMap map, String title, LatLng currPos, int type, boolean editable) {

        CustomMarker customMarker = new CustomMarker();

        customMarker.setEditable(editable);
        customMarker.setPos(currPos);
        customMarker.setTitle(title);
        customMarker.setType(type);

        if (editable) {

            customMarker.setSnippet(context.getString(R.string.marker_snippet_string));
        }

        Marker marker = customMarker.addToMap(map);
        markersList.add(customMarker);

        return marker;
    }

    public Marker addMarker (GoogleMap map, CustomMarkerData data) {

        CustomMarker customMarker = new CustomMarker(data);
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

                    iterator.remove();
                    customMarker.removeAlertCircle();
                    customMarker.getMarker().remove();
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

                    iterator.remove();
                    customMarker.removeAlertCircle();
                    customMarker.getMarker().remove();
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
                iterator.remove();
                customMarker.removeAlertCircle();
                customMarker.getMarker().remove();
            }
        }

        markersList.clear();
    }

    public CustomMarkerData getUserMarkerData (CustomMarker customMarker) {

        CustomMarkerData data = new CustomMarkerData();

        data.position = customMarker.getPos();
        data.title = customMarker.getTitle();
        data.type = customMarker.getType();
        data.shared = customMarker.isShared();
        data.alert = customMarker.isAlertEnabled();
        data.alertRadius = customMarker.getAlertRadius();
        data.alertMsg = customMarker.getAlertMsg();
        data.alertAttachments = customMarker.getAlertAttachments();
        data.alertReqSignature = customMarker.isAlertSignatureRequired();

        return data;
    }

    public ArrayList<CustomMarkerData> getAllUserMarkersData() {

        ArrayList<CustomMarkerData> markersDataList = new ArrayList<CustomMarkerData>();

        for (CustomMarker customMarker : markersList) {

            if (customMarker.isEditable()) {

                CustomMarkerData markerData = new CustomMarkerData();

                markerData.position = customMarker.getPos();
                markerData.title = customMarker.getTitle();
                markerData.type = customMarker.getType();
                markerData.shared = customMarker.isShared();
                markerData.alert = customMarker.isAlertEnabled();
                markerData.alertRadius = customMarker.getAlertRadius();
                markerData.alertMsg = customMarker.getAlertMsg();
                markerData.alertAttachments = customMarker.getAlertAttachments();
                markerData.alertReqSignature = customMarker.isAlertSignatureRequired();

                markersDataList.add(markerData);
            }
        }

        return markersDataList;
    }
}
