package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

public class AccForceView implements Parcelable {

    private static final String TAG = "AccForceView";

    private ImageView accForceView = null;
    private AppColor appColor = null;
    private FORCE_t lastForce = FORCE_t.UNKNOW;
    private LEVEL_t lastLevel = LEVEL_t.LEVEL_UNKNOW;

    private boolean visible = true;

    public AccForceView (Activity activity) {

        accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        appColor = new AppColor(activity);
    }

    protected AccForceView (Parcel in) {

        int tmpLastForce = in.readInt();
        lastForce = tmpLastForce == -1 ? null : FORCE_t.values()[tmpLastForce];

        int tmpLastLevel = in.readInt();
        lastLevel = tmpLastLevel == -1 ? null : LEVEL_t.values()[tmpLastLevel];

        visible = in.readByte() != 0;
    }

    public void restore (Activity activity) {

        accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        appColor = new AppColor(activity);

        setValue(lastForce, lastLevel);

        if (visible) {

            hide(false);
        }
        else {

            hide(true);
        }
    }

    public void hide (boolean hide) {

        if (accForceView == null) {

            Log.d(TAG, "accForceView == null");
            return;
        }

        if (hide) {

            accForceView.setVisibility(View.INVISIBLE);
        }
        else {

            accForceView.setVisibility(View.VISIBLE);
        }

        visible = !hide;
    }

    public void setValue (FORCE_t force, LEVEL_t level) {

        if (accForceView == null) {

            Log.d(TAG, "accForceView == null");
            return;
        }

        switch (force) {

            case UNKNOW:
                accForceView.setImageDrawable(null);
                break;

            case TURN_LEFT:
                accForceView.setImageResource(R.drawable.ic_arrow_left);
                break;

            case TURN_RIGHT:
                accForceView.setImageResource(R.drawable.ic_arrow_right);
                break;

            case ACCELERATION:
                accForceView.setImageResource(R.drawable.ic_arrow_up);
                break;

            case BRAKING:
                accForceView.setImageResource(R.drawable.ic_arrow_down);
                break;
        }

        Drawable drawable = accForceView.getBackground();
        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
        accForceView.setBackground(drawable);

        lastForce = force;
        lastLevel = level;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeInt(this.lastForce == null ? -1 : this.lastForce.ordinal());
        dest.writeInt(this.lastLevel == null ? -1 : this.lastLevel.ordinal());
        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
    }

    public static final Parcelable.Creator<AccForceView> CREATOR = new Parcelable.Creator<AccForceView>() {

        @Override
        public AccForceView createFromParcel (Parcel source) {

            return new AccForceView(source);
        }

        @Override
        public AccForceView[] newArray (int size) {

            return new AccForceView[size];
        }
    };
}
