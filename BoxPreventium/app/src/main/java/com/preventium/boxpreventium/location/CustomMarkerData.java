package com.preventium.boxpreventium.location;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.api.model.StringList;

public class CustomMarkerData {

    public int type = 0;
    public LatLng position = null;
    public String title = null;
    public boolean alert = false;
    public int perimeter = 0;

    public boolean signature = false;
    public boolean shared = false;
    public StringList documents = null;
    public String message = null;
}
