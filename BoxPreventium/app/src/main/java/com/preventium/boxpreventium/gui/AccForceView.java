package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.utils.DeviceScreen;

//import com.preventium.boxpreventium.manager.JSONManager;

public class AccForceView implements Parcelable {
    public static final Creator<AccForceView> CREATOR = new C00251();
    private static final String TAG = "AccForceView";
    private ImageView accForceView = null;
    private Activity activity;
    MainActivity mainActivity;
    private AppColor appColor;
    private FORCE_t lastForce = FORCE_t.UNKNOW;
    private LEVEL_t lastLevel = LEVEL_t.LEVEL_UNKNOW;
    private boolean visible = true;
    private SharedPreferences sharedPref;

    static class C00251 implements Creator<AccForceView> {
        C00251() {
        }

        public AccForceView createFromParcel(Parcel source) {
            return new AccForceView(source);
        }

        public AccForceView[] newArray(int size) {
            return new AccForceView[size];
        }
    }

    public AccForceView(Activity activity) {
        this.activity = activity;
        this.accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        this.appColor = new AppColor(activity.getApplicationContext());
    }

    protected AccForceView(Parcel in) {
        LEVEL_t lEVEL_t = null;
        int tmpLastForce = in.readInt();
        this.lastForce = tmpLastForce == -1 ? null : FORCE_t.values()[tmpLastForce];
        int tmpLastLevel = in.readInt();
        if (tmpLastLevel != -1) {
            lEVEL_t = LEVEL_t.values()[tmpLastLevel];
        }
        this.lastLevel = lEVEL_t;
        this.visible = in.readByte() != (byte) 0;
    }

    /*
    public void restore(Activity activity) {
        this.accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        this.appColor = new AppColor(activity);
        setValue(this.lastForce, this.lastLevel);
        if (this.visible) {
            hide(false);
        } else {
            hide(true);
        }
    }
*/
    public void hide(boolean hide, int bolmap, int bolScreen, int bolSeuil) {
//###bolmap = valeur de retour map
//###bolScreen = valeur de retour taille ecran
        this.activity = activity;
        boolean z = true;
        if (this.accForceView == null) {
            Log.d(TAG, "accForceView == null");
            return;
        }
        if (hide) {
            this.accForceView.setVisibility(View.GONE);
        } else {
            double deviceScreen = new DeviceScreen().getDeviceScreen(this.activity);
            //JSONManager jSONManager = new JSONManager(this.activity);
            //if (deviceScreen > ((double) Integer.valueOf(JSONManager.getScreen()).intValue())) {

          //  boolean isFirstRun = sharedPref.getBoolean(getString((R.string.firstrun_key)), true);

            if (deviceScreen > bolScreen) {
                //JSONManager jSONManager2 = new JSONManager(this.activity);
                if (bolmap == Integer.valueOf(QrScanActivity.SCAN_MODE_VEHICLE_DISABLED).intValue()) {
                    this.accForceView.setVisibility(View.GONE);
                } else {
                    this.accForceView.setVisibility(View.VISIBLE);
                }
                // MainActivity.instance().Alert("Force apparait", Toast.LENGTH_LONG);
            } else {
                hideAccForceView(this.activity, true, bolSeuil);
            }
        }
        if (hide) {
            z = false;
        }
        this.visible = z;
    }

    private void hideAccForceView(Activity activity, boolean hide, int bolSeuil) {
        if (hide) {
            LayoutParams params = (LayoutParams) this.accForceView.getLayoutParams();
            params.height = ((int) this.accForceView.getResources().getDimension(R.dimen.force_layout_height)) + 20;
            params.width = ((int) this.accForceView.getResources().getDimension(R.dimen.force_layout_width)) + 20;
            this.accForceView.setLayoutParams(params);
            //JSONManager jSONManager = new JSONManager(activity);

            //###bolSeuil = valeur de retour boolean seuil

            if (bolSeuil == Integer.valueOf(QrScanActivity.SCAN_MODE_VEHICLE_DISABLED).intValue()) {
                this.accForceView.setVisibility(View.GONE);
            } else {
                this.accForceView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setValue(FORCE_t force, LEVEL_t level) {
        if (this.accForceView == null) {
            Log.d(TAG, "accForceView == null");
            return;
        }
        switch (force) {
            case UNKNOW:
                this.accForceView.setImageDrawable(null);
                break;
            case TURN_LEFT:
                this.accForceView.setImageResource(R.drawable.ic_arrow_left);
                break;
            case TURN_RIGHT:
                this.accForceView.setImageResource(R.drawable.ic_arrow_right);
                break;
            case ACCELERATION:
                this.accForceView.setImageResource(R.drawable.ic_arrow_up);
                break;
            case BRAKING:
                this.accForceView.setImageResource(R.drawable.ic_arrow_down);
                break;
        }
        Drawable drawable = this.accForceView.getBackground();
        drawable.setColorFilter(this.appColor.getColor(level), Mode.SRC_ATOP);
        this.accForceView.setBackground(drawable);
        this.lastForce = force;
        this.lastLevel = level;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = -1;
        dest.writeInt(this.lastForce == null ? -1 : this.lastForce.ordinal());
        if (this.lastLevel != null) {
            i = this.lastLevel.ordinal();
        }
        dest.writeInt(i);
        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
    }
}
