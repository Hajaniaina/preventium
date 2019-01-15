package com.preventium.boxpreventium.location;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.preventium.boxpreventium.manager.DataMarker;

import java.util.ArrayList;
import java.util.Iterator;

public class MarkerManager {
    private static final int NEAR_MARKER_DISTANCE_TH = 1000;
    private static final String TAG = "MarkerManager";
    private Context context;
    private ArrayList<DataMarker> data = null;
    private ArrayList<CustomMarker> markersList = null;

    public MarkerManager(Context context) {
        this.context = context;
        this.markersList = new ArrayList();
        this.data = new ArrayList();
    }

    public String toString() {
        if (this.markersList.size() <= 0) {
            return "null";
        }
        int i = 0;
        String str = "";
        Iterator it = this.markersList.iterator();
        while (it.hasNext()) {
            str = str + String.valueOf(i) + ": [" + ((CustomMarker) it.next()).toString() + "]\n";
            i++;
        }
        return str;
    }

    public CustomMarker getMarker(Marker marker) {
        CustomMarker foundedMarker = null;
        if (this.markersList.size() > 0) {
            Iterator it = this.markersList.iterator();
            while (it.hasNext()) {
                CustomMarker customMarker = (CustomMarker) it.next();
                if (marker.getPosition().equals(customMarker.getPos())) {
                    foundedMarker = customMarker;
                }
            }
        }
        return foundedMarker;
    }

    public void addAlertCircle(GoogleMap map, CustomMarker marker) {
        marker.addAlertCircle(map);
    }

    public void showAlertCircle(CustomMarker marker, boolean show) {
        marker.showAlertCircle(show);
    }

    public void fetchNearMarkers(LatLng currPos) {
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                CustomMarker customMarker = (CustomMarker) iterator.next();
                // if (customMarker.isAlertEnabled() && !customMarker.isAlertAlreadyActivated()) {
                    float[] results = new float[3];
                    Location.distanceBetween(currPos.latitude, currPos.longitude, customMarker.getPos().latitude, customMarker.getPos().longitude, results);
                    float rayon = customMarker.getAlertRadius() > 0.0 ? customMarker.getAlertRadius() : 1000.0f;
                    if (results[0] < rayon) {
                        customMarker.setAsNear(true);
                        customMarker.setTime(System.currentTimeMillis());
                    } else {
                        customMarker.setAsNear(false);
                    }
                // }
            }
        }
    }

    public CustomMarker findClosestAlertMarker(LatLng currPos) {
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                CustomMarker customMarker = (CustomMarker) iterator.next();
                //if (/*customMarker.isAlertEnabled() &&  !customMarker.isAlertAlreadyActivated() && */ customMarker.isNear()) {
                    float[] results = new float[3];
                    Location.distanceBetween(currPos.latitude, currPos.longitude, customMarker.getPos().latitude, customMarker.getPos().longitude, results);
                    if (results[0] <= ((float) customMarker.getAlertRadius())) {
                        return customMarker;
                    }
                //}
            }
        }
        return null;
    }

    public void hideAllAlertCircles() {
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                ((CustomMarker) iterator.next()).showAlertCircle(false);
            }
        }
    }

    public Marker addMarker(GoogleMap map, String title, String info, LatLng currPos, int type, boolean editable) {
        Marker marker;
        CustomMarker customMarker = new CustomMarker();
        DataMarker datamarker = new DataMarker(customMarker, info, type);
        customMarker.setEditable(editable);
        customMarker.setPos(currPos);
        customMarker.setTitle(title);
        customMarker.setType(type);
        if (editable) {
            marker = customMarker.addToMap(map);
            this.markersList.add(customMarker);
            this.data.add(datamarker);
        } else {
            marker = customMarker.addToMap(map);
            this.markersList.add(customMarker);
            this.data.add(datamarker);
        }
        return marker;
    }

    public Marker addMarker(GoogleMap map, String title, String info, LatLng currPos, int type, boolean editable, int radius) {
        Marker marker;
        CustomMarker customMarker = new CustomMarker();
        DataMarker datamarker = new DataMarker(customMarker, info, type);
        customMarker.setEditable(editable);
        customMarker.setPos(currPos);
        customMarker.setTitle(title);
        customMarker.setType(type);
        customMarker.setAlertRadius(radius);
        if (editable) {
            marker = customMarker.addToMap(map);
            this.markersList.add(customMarker);
            this.data.add(datamarker);
        } else {
            marker = customMarker.addToMap(map);
            this.markersList.add(customMarker);
            this.data.add(datamarker);
        }
        return marker;
    }

    public String getDataMarker(CustomMarker marker) {
        String info = "";
        Iterator itr = this.data.iterator();
        while (itr.hasNext()) {
            DataMarker dm = (DataMarker) itr.next();
            if (dm.getMarker().equals(marker)) {
                info = dm.getData();
            }
        }
        return info;
    }

    public int getTypeMarker(CustomMarker marker) {
        int type = 0;
        CustomMarker customMarker = new CustomMarker();
        Iterator itr = this.data.iterator();
        while (itr.hasNext()) {
            DataMarker dm = (DataMarker) itr.next();
            if (dm.getMarker().equals(marker)) {
                type = customMarker.setDrawable(dm.getType());
            }
        }
        return type;
    }

    public Marker addMarker(GoogleMap map, CustomMarkerData data) {
        CustomMarker customMarker = new CustomMarker(data);
        Marker marker = customMarker.addToMap(map);
        this.markersList.add(customMarker);
        return marker;
    }

    public boolean removeMarker(int markerType, boolean onlyOldMarkers) {
        boolean found = false;
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                CustomMarker customMarker = (CustomMarker) iterator.next();
                if (customMarker.getType() == markerType) {
                    if (!onlyOldMarkers) {
                        iterator.remove();
                        customMarker.removeAlertCircle();
                        customMarker.getMarker().remove();
                        found = true;
                    } else if (!customMarker.isNewCreated()) {
                        iterator.remove();
                        customMarker.removeAlertCircle();
                        customMarker.getMarker().remove();
                        found = true;
                    }
                }
            }
        }
        return found;
    }

    public boolean removeMarker(CustomMarker marker) {
        boolean found = false;
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                CustomMarker customMarker = (CustomMarker) iterator.next();
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

    public boolean removeMarker(Marker marker) {
        return removeMarker(getMarker(marker));
    }

    public void removeAllMarkers() {
        if (this.markersList.size() > 0) {
            Iterator<CustomMarker> iterator = this.markersList.iterator();
            while (iterator.hasNext()) {
                CustomMarker customMarker = (CustomMarker) iterator.next();
                iterator.remove();
                customMarker.removeAlertCircle();
                customMarker.getMarker().remove();
            }
        }
        this.markersList.clear();
    }

    public CustomMarkerData getUserMarkerData(CustomMarker customMarker) {
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

    public ArrayList<CustomMarkerData> fetchAllUserMarkersData() {
        ArrayList<CustomMarkerData> markersDataList = new ArrayList();
        Iterator it = this.markersList.iterator();
        while (it.hasNext()) {
            CustomMarker customMarker = (CustomMarker) it.next();
            if (customMarker.isEditable() && customMarker.isNewCreated()) {
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
                customMarker.setAsNewCreated(false);
                customMarker.setEditable(false);
            }
        }
        return markersDataList;
    }
}
