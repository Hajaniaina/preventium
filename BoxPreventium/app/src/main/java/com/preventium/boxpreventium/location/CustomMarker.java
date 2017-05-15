package com.preventium.boxpreventium.location;

import android.graphics.Color;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.preventium.boxpreventium.R;

import java.util.ArrayList;
import java.util.Random;

public class CustomMarker {

    private static final String TAG = "CustomMarker";

    private static final float[] MARKER_HUES = new float[] {

            BitmapDescriptorFactory.HUE_RED,
            BitmapDescriptorFactory.HUE_ORANGE,
            BitmapDescriptorFactory.HUE_YELLOW,
            BitmapDescriptorFactory.HUE_GREEN,
            BitmapDescriptorFactory.HUE_CYAN,
            BitmapDescriptorFactory.HUE_AZURE,
            BitmapDescriptorFactory.HUE_BLUE,
            BitmapDescriptorFactory.HUE_VIOLET,
            BitmapDescriptorFactory.HUE_MAGENTA,
            BitmapDescriptorFactory.HUE_ROSE,
    };

    public static final int MARKER_RED     = 0;
    public static final int MARKER_ORANGE  = 1;
    public static final int MARKER_YELLOW  = 2;
    public static final int MARKER_GREEN   = 3;
    public static final int MARKER_CYAN    = 4;
    public static final int MARKER_AZURE   = 5;
    public static final int MARKER_BLUE    = 6;
    public static final int MARKER_VIOLET  = 7;
    public static final int MARKER_MAGENTA = 8;
    public static final int MARKER_ROSE    = 9;
    public static final int MARKER_RANDOM  = 10;

    public static final int MARKER_INFO    = 13;
    public static final int MARKER_DANGER  = 14;

    private MarkerOptions opt = null;
    private Marker marker = null;
    private int type = MARKER_INFO;
    private boolean editable = true;
    private LatLng pos = null;
    private String title = "";
    private boolean shared = false;
    private boolean newCreated = false;
    private boolean alert = false;
    private boolean alertWasActivated = false;
    private int alertRadius = 0;
    private boolean near = false;
    private String alertMsg = null;
    private boolean alertReqSignature = false;
    private ArrayList<String> alertAttachments = null;
    private Circle alertCircle = null;

    CustomMarker() {

        newCreated = true;
        opt = new MarkerOptions();
    }

    CustomMarker (CustomMarkerData data) {

        opt = new MarkerOptions();

        setPos(data.position);
        setTitle(data.title);
        setType(data.type);
        share(data.shared);
        enableAlert(data.alert);
        setAlertMsg(data.alertMsg);
        setAlertRadius(data.alertRadius);
        setAlertSignatureReq(data.alertReqSignature);
        setAlertAttachments(data.alertAttachments);
        setEditable(false);
    }

    public Marker getMarker() {

        return marker;
    }

    public String toString() {

        if (marker != null) {

            String str = marker.getTitle() + " " + String.valueOf(type) + " : " + marker.getPosition().toString();
            return str;
        }

        return "null";
    }

    public Marker addToMap (GoogleMap map) {

        setType(type);

        marker = map.addMarker(opt);
        pos = opt.getPosition();

        return marker;
    }

    public void addAlertCircle (GoogleMap map) {

        if (alertCircle != null) {

            if (alertCircle.getRadius() != alertRadius) {

                alertCircle.setRadius(alertRadius);
            }
        }
        else {

            CircleOptions circleOpt = new CircleOptions();

            circleOpt.center(pos);
            circleOpt.radius(alertRadius);
            circleOpt.strokeWidth(2);
            circleOpt.strokeColor(Color.RED);

            alertCircle = map.addCircle(circleOpt);
        }
    }

    public void showAlertCircle (boolean show) {

        if (alertCircle != null) {

            alertCircle.setVisible(show);
        }
    }

    public void removeAlertCircle() {

        if (alertCircle != null) {

            alertCircle.remove();
        }
    }

    public void setType (int type) {

        BitmapDescriptor bitmap = null;
        this.type = type;

        switch (type) {

            case MARKER_RANDOM:
                bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[new Random().nextInt(MARKER_HUES.length)]);
                break;

            case MARKER_INFO:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_info);
                opt.anchor(0.5f, 0.5f);
                break;

            case MARKER_DANGER:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_danger);;
                opt.anchor(0.5f, 0.5f);
                break;

            default:
                if (type < MARKER_RANDOM) bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[type]);
                break;
        }

        if (bitmap != null) {

            opt.icon(bitmap);

            if (marker != null) {

                marker.setIcon(bitmap);
            }
        }
    }

    public int getType() {

        return type;
    }

    public void setPos (LatLng pos) {

        if (pos != null) {

            opt.position(pos);

            if (marker != null) {

                marker.setPosition(pos);
            }
        }
    }

    public LatLng getPos() {

        return pos;
    }

    public void setSnippet (String snippet) {

        if (snippet != null && snippet.length() > 0) {

            opt.snippet(snippet);

            if (marker != null) {

                marker.setSnippet(snippet);
            }
        }
    }

    public void setTitle (String title) {

        if (title != null && title.length() > 0) {

            this.title = title;
            opt.title(title);

            if (marker != null) {

                marker.setTitle(title);
            }
        }
    }

    public String getTitle() {

        if (marker != null) {

            return marker.getTitle();
        }

        return null;
    }

    public void setIcon (BitmapDescriptor bitmap) {

        if (bitmap != null) {

            opt.icon(bitmap);

            if (marker != null) {

                marker.setIcon(bitmap);
            }
        }
    }

    public void setAsNewCreated (boolean created) {

        newCreated = created;
    }

    public boolean isNewCreated() {

        return newCreated;
    }

    public void setAlertRadius (int meters) {

        alertRadius = meters;
    }

    public int getAlertRadius() {

        return alertRadius;
    }

    public void enableAlert (boolean enable) {

        alert = enable;
    }

    public boolean isAlertEnabled() {

        return alert;
    }

    public boolean isAlertAlreadyActivated() {

        return alertWasActivated;
    }

    public void setAsActivated (boolean activated) {

        alertWasActivated = activated;
    }

    public void setEditable (boolean editable) {

        this.editable = editable;
    }

    public boolean isEditable() {

        return editable;
    }

    public void share (boolean share) {

        shared = share;
    }

    public boolean isShared() {

        return shared;
    }

    public void setAlertMsg (String msg) {

        alertMsg = msg;
    }

    public String getAlertMsg() {

        return alertMsg;
    }

    public void setAlertAttachments (ArrayList<String> urls) {

        alertAttachments = urls;
    }

    public ArrayList<String> getAlertAttachments() {

        return alertAttachments;
    }

    public int getTotalAttachmentsNum() {

        int num = 0;

        if (alertMsg != null)
        {
            if (alertMsg.length() > 1)
            {
                num += 1;
            }
        }

        num += getAlertAttachNumber();

        return num;
    }

    public void setAlertSignatureReq (boolean request) {

        alertReqSignature = request;
    }

    public boolean isAlertSignatureRequired() {

        return alertReqSignature;
    }

    public boolean isNear() {

        return near;
    }

    public void setAsNear (boolean near) {

        this.near = near;
    }

    public int getAlertAttachNumber() {

        if (alertAttachments != null) {

            return alertAttachments.size();
        }

        return 0;
    }
}
