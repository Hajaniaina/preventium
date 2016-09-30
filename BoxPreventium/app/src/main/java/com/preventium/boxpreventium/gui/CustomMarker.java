package com.preventium.boxpreventium.gui;

import com.preventium.boxpreventium.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

    public static final int MARKER_START  = 11;
    public static final int MARKER_FINISH = 12;
    public static final int MARKER_INFO   = 13;
    public static final int MARKER_DANGER = 14;

    private GoogleMap map = null;
    private MarkerOptions opt = null;
    private Marker marker = null;
    private int type = MARKER_INFO;
    private boolean editable = true;

    CustomMarker (GoogleMap map) {

        opt = new MarkerOptions();
        this.map = map;
    }

    public String toString() {

        if (marker != null) {

            String str = marker.getTitle() + " " + String.valueOf(type) + " : " + marker.getPosition().toString();
            return str;
        }

        return "null";
    }

    public void addToMap() {

        setType(type);

        if (editable) {

            opt.snippet("Tap here to edit this marker");
        }

        marker = map.addMarker(opt);
    }

    public void addToMap (String title, LatLng pos, int type) {

        opt.position(pos);
        opt.title(title);
        opt.flat(false);
        this.type = type;

        addToMap();
    }

    public void setType (int type) {

        BitmapDescriptor bitmap = null;

        if (!editable) {

            return;
        }

        this.type = type;

        switch (type) {

            case MARKER_RANDOM:
                bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[new Random().nextInt(MARKER_HUES.length)]);
                break;

            case MARKER_START:
                editable = false;
                bitmap = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                break;

            case MARKER_FINISH:
                editable = false;
                bitmap = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                break;

            case MARKER_INFO:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_info);
                break;

            case MARKER_DANGER:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_danger);
                break;

            default:

                if (type < MARKER_RANDOM) {

                    bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[type]);
                }

                break;
        }

        if (bitmap != null) {

            opt.icon(bitmap);

            if (marker != null) {

                marker.setIcon(bitmap);
            }
        }
    }

    public void setPos (LatLng pos) {

        if (!editable) {

            return;
        }

        if (pos != null) {

            opt.position(pos);

            if (marker != null) {

                marker.setPosition(pos);
            }
        }
    }

    public void setSnippet (String snippet) {

        if (!editable) {

            return;
        }

        if (snippet != null && snippet.length() > 0) {

            opt.snippet(snippet);

            if (marker != null) {

                marker.setSnippet(snippet);
            }
        }
    }

    public void setTitle (String title) {

        if (!editable) {

            return;
        }

        if (title != null && title.length() > 0) {

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

        if (!editable) {

            return;
        }

        if (bitmap != null) {

            opt.icon(bitmap);

            if (marker != null) {

                marker.setIcon(bitmap);
            }
        }
    }

    public void setEditable (boolean editable) {

        this.editable = editable;
    }

    public boolean editable() {

        return editable;
    }

    public Marker getMarker() {

        return marker;
    }

    public MarkerOptions getOpt() {

        return opt;
    }
}
