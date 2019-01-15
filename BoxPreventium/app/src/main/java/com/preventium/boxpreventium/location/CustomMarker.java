package com.preventium.boxpreventium.location;

import android.support.v4.internal.view.SupportMenu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.module.Load.LoadImage;

import java.util.ArrayList;
import java.util.Random;

public class CustomMarker {
    public static final int ACCELERATION_1 = 30;
    public static final int ACCELERATION_2 = 31;
    public static final int ACCELERATION_3 = 32;
    public static final int ACCELERATION_4 = 33;
    public static final int ACCELERATION_5 = 34;
    public static final int BRAKING_1 = 35;
    public static final int BRAKING_2 = 36;
    public static final int BRAKING_3 = 37;
    public static final int BRAKING_4 = 38;
    public static final int BRAKING_5 = 39;
    public static final int MARKER_AZURE = 5;
    public static final int MARKER_BLUE = 6;
    public static final int MARKER_CYAN = 4;
    public static final int MARKER_DANGER = 14;
    public static final int MARKER_GREEN = 3;
    private static final float[] MARKER_HUES = new float[]{0.0f, BitmapDescriptorFactory.HUE_ORANGE, BitmapDescriptorFactory.HUE_YELLOW, BitmapDescriptorFactory.HUE_GREEN, BitmapDescriptorFactory.HUE_CYAN, BitmapDescriptorFactory.HUE_AZURE, BitmapDescriptorFactory.HUE_BLUE, BitmapDescriptorFactory.HUE_VIOLET, BitmapDescriptorFactory.HUE_MAGENTA, BitmapDescriptorFactory.HUE_ROSE};
    public static final int MARKER_INFO = 13;
    public static final int MARKER_MAGENTA = 8;
    public static final int MARKER_ORANGE = 1;
    public static final int MARKER_PAUSE = 17;
    public static final int MARKER_RANDOM = 10;
    public static final int MARKER_RED = 0;
    public static final int MARKER_RESUME = 18;
    public static final int MARKER_ROSE = 9;
    public static final int MARKER_START = 15;
    public static final int MARKER_STOP = 16;
    public static final int MARKER_VIOLET = 7;
    public static final int MARKER_YELLOW = 2;
    private static final String TAG = "CustomMarker";
    public static final int TURN_LEFT_1 = 20;
    public static final int TURN_LEFT_2 = 21;
    public static final int TURN_LEFT_3 = 22;
    public static final int TURN_LEFT_4 = 23;
    public static final int TURN_LEFT_5 = 24;
    public static final int TURN_RIGHT_1 = 25;
    public static final int TURN_RIGHT_2 = 26;
    public static final int TURN_RIGHT_3 = 27;
    public static final int TURN_RIGHT_4 = 28;
    public static final int TURN_RIGHT_5 = 29;
    private boolean alert;
    private ArrayList<String> alertAttachments;
    private Circle alertCircle;
    private String alertMsg;
    private int alertRadius;
    private boolean alertReqSignature;
    private boolean alertWasActivated;
    private boolean editable;
    private Marker marker;
    private boolean near;
    private boolean newCreated;
    private MarkerOptions opt;
    private LatLng pos;
    private boolean shared;
    private String title;
    private int type;
    private boolean isDiapo;
    private float time;

    CustomMarker() {
        this.opt = null;
        this.marker = null;
        this.type = 13;
        this.editable = true;
        this.pos = null;
        this.title = "";
        this.shared = false;
        this.newCreated = false;
        this.alert = false;
        this.alertWasActivated = false;
        this.alertRadius = 0;
        this.near = false;
        this.alertMsg = null;
        this.alertReqSignature = false;
        this.alertAttachments = null;
        this.alertCircle = null;
        this.newCreated = true;
        this.opt = new MarkerOptions();
    }

    CustomMarker(CustomMarkerData data) {
        this.marker = null;
        this.type = 13;
        this.editable = true;
        this.pos = null;
        this.title = "";
        this.shared = false;
        this.newCreated = false;
        this.alert = false;
        this.alertWasActivated = false;
        this.alertRadius = 0;
        this.near = false;
        this.alertMsg = null;
        this.alertReqSignature = false;
        this.alertAttachments = null;
        this.alertCircle = null;
        this.opt = new MarkerOptions();
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

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public String toString() {
        if (this.marker != null) {
            return this.marker.getTitle() + " " + String.valueOf(this.type) + " : " + this.marker.getPosition().toString();
        }
        return "null";
    }

    public Marker addToMap(GoogleMap map) {
        setType(this.type);
        this.marker = map.addMarker(this.opt);
        this.pos = this.opt.getPosition();
        return this.marker;
    }

    public void addAlertCircle(GoogleMap map) {
        if (this.alertCircle == null) {
            CircleOptions circleOpt = new CircleOptions();
            circleOpt.center(this.pos);
            circleOpt.radius((double) this.alertRadius);
            circleOpt.strokeWidth(2.0f);
            circleOpt.strokeColor(SupportMenu.CATEGORY_MASK);
            this.alertCircle = map.addCircle(circleOpt);
        } else if (this.alertCircle.getRadius() != ((double) this.alertRadius)) {
            this.alertCircle.setRadius((double) this.alertRadius);
        }
    }

    public void showAlertCircle(boolean show) {
        if (this.alertCircle != null) {
            this.alertCircle.setVisible(show);
        }
    }

    public void removeAlertCircle() {
        if (this.alertCircle != null) {
            this.alertCircle.remove();
        }
    }

    public void setType(int type) {
        BitmapDescriptor bitmap = null;
        this.type = type;
        switch (type) {
            case 10:
                bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[new Random().nextInt(MARKER_HUES.length)]);
                break;
            case 13:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_marker_info);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 14:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_marker_danger);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 15:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_go);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 16:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_stop);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 17:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_pause);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 18:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_resume);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 20:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_left0);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 21:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_left1);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 22:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_left2);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 23:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_left3);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 24:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_left4);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 25:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_right0);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 26:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_right1);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 27:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_right2);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 28:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_right3);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 29:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_right4);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 30:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_acc0);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 31:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_acc1);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 32:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_acc2);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 33:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_acc3);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 34:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_acc4);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 35:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_brake0);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 36:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_brake1);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 37:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_brake2);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 38:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_brake3);
                this.opt.anchor(0.5f, 0.5f);
                break;
            case 39:
                bitmap = getMarkerIconFromDrawable(R.drawable.ic_little_arrow_brake4);
                this.opt.anchor(0.5f, 0.5f);
                break;
            default:
                if (type < 10) {
                    bitmap = BitmapDescriptorFactory.defaultMarker(MARKER_HUES[type]);
                    break;
                }
                break;
        }
        if (bitmap != null) {
            this.opt.icon(bitmap);
            if (this.marker != null) {
                this.marker.setIcon(bitmap);
            }
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(int res) {
        return BitmapDescriptorFactory.fromBitmap(LoadImage.drawableToBitmap(res));
    }

    public int getType() {
        return this.type;
    }

    public void setPos(LatLng pos) {
        if (pos != null) {
            this.opt.position(pos);
            if (this.marker != null) {
                this.marker.setPosition(pos);
            }
        }
    }

    public LatLng getPos() {
        return this.pos;
    }

    public void setSnippet(String snippet) {
        if (snippet != null && snippet.length() > 0) {
            this.opt.snippet(snippet);
            if (this.marker != null) {
                this.marker.setSnippet(snippet);
            }
        }
    }

    public void setTitle(String title) {
        if (title != null && title.length() > 0) {
            this.title = title;
            this.opt.title(title);
            if (this.marker != null) {
                this.marker.setTitle(title);
            }
        }
    }

    public String getTitle() {
        if (this.marker != null) {
            return this.marker.getTitle();
        }
        return null;
    }

    public void setIcon(BitmapDescriptor bitmap) {
        if (bitmap != null) {
            this.opt.icon(bitmap);
            if (this.marker != null) {
                this.marker.setIcon(bitmap);
            }
        }
    }

    public void setAsNewCreated(boolean created) {
        this.newCreated = created;
    }

    public boolean isNewCreated() {
        return this.newCreated;
    }

    public void setAlertRadius(int meters) {
        this.alertRadius = meters;
    }

    public int getAlertRadius() {
        return this.alertRadius;
    }

    public void enableAlert(boolean enable) {
        this.alert = enable;
    }

    public boolean isAlertEnabled() {
        return this.alert;
    }

    public boolean isAlertAlreadyActivated() {
        return this.alertWasActivated;
    }

    public void setAsActivated(boolean activated) {
        this.alertWasActivated = activated;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void share(boolean share) {
        this.shared = share;
    }

    public boolean isShared() {
        return this.shared;
    }

    public void setAlertMsg(String msg) {
        this.alertMsg = msg;
    }

    public String getAlertMsg() {
        return this.alertMsg;
    }

    public void setAlertAttachments(ArrayList<String> urls) {
        this.alertAttachments = urls;
    }

    public ArrayList<String> getAlertAttachments() {
        return this.alertAttachments;
    }

    public int getTotalAttachmentsNum() {
        int num = 0;
        if (this.alertMsg != null && this.alertMsg.length() > 1) {
            num = 0 + 1;
        }
        return num + getAlertAttachNumber();
    }

    public void setAlertSignatureReq(boolean request) {
        this.alertReqSignature = request;
    }

    public boolean isAlertSignatureRequired() {
        return this.alertReqSignature;
    }

    public boolean isNear() {
        return this.near;
    }

    public void setAsNear(boolean near) {
        this.near = near;
    }

    public int getAlertAttachNumber() {
        if (this.alertAttachments != null) {
            return this.alertAttachments.size();
        }
        return 0;
    }

    public int setDrawable(int type) {
        switch (type) {
            case 20:
                return R.drawable.ic_little_arrow_left0;
            case 21:
                return R.drawable.ic_little_arrow_left1;
            case 22:
                return R.drawable.ic_little_arrow_left2;
            case 23:
                return R.drawable.ic_little_arrow_left3;
            case 24:
                return R.drawable.ic_little_arrow_left4;
            case 25:
                return R.drawable.ic_little_arrow_right0;
            case 26:
                return R.drawable.ic_little_arrow_right1;
            case 27:
                return R.drawable.ic_little_arrow_right2;
            case 28:
                return R.drawable.ic_little_arrow_right3;
            case 29:
                return R.drawable.ic_little_arrow_right4;
            case 30:
                return R.drawable.ic_little_arrow_acc0;
            case 31:
                return R.drawable.ic_little_arrow_acc1;
            case 32:
                return R.drawable.ic_little_arrow_acc2;
            case 33:
                return R.drawable.ic_little_arrow_acc3;
            case 34:
                return R.drawable.ic_little_arrow_acc4;
            case 35:
                return R.drawable.ic_little_arrow_brake0;
            case 36:
                return R.drawable.ic_little_arrow_brake1;
            case 37:
                return R.drawable.ic_little_arrow_brake2;
            case 38:
                return R.drawable.ic_little_arrow_brake3;
            case 39:
                return R.drawable.ic_little_arrow_brake4;
            default:
                return 0;
        }
    }
}
