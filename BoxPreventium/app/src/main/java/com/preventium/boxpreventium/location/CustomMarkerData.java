package com.preventium.boxpreventium.location;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class CustomMarkerData {

    public int type = 0;
    public LatLng position = null;
    public String title = null;
    public boolean alert = false;
    public int alertRadius = 0;
    public ArrayList<String> alertAttachments = null;
    public String alertMsg = null;
    public boolean alertReqSignature = false;
    public boolean shared = false;
}
