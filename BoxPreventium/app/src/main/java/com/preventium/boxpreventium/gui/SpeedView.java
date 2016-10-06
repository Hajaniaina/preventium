package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SPEED_t;

import java.util.HashMap;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class SpeedView implements Parcelable {

    private static final String TAG = "SpeedView";

    private AppColor appColor;
    private boolean visible = true;
    private HashMap<SPEED_t, CircleProgressView> viewMap;
    private HashMap<SPEED_t, LEVEL_t> levelMap;
    private HashMap<SPEED_t, Integer> speedMap;

    SpeedView (Activity activity) {

        init(activity);

        levelMap.put(SPEED_t.IN_CORNERS, null);
        levelMap.put(SPEED_t.IN_STRAIGHT_LINE, null);
        levelMap.put(SPEED_t.MAX_LIMIT, null);

        speedMap.put(SPEED_t.IN_CORNERS, 0);
        speedMap.put(SPEED_t.IN_STRAIGHT_LINE, 0);
        speedMap.put(SPEED_t.MAX_LIMIT, 0);
    }

    protected SpeedView (Parcel in) {

        visible = in.readByte() != 0;
        viewMap = (HashMap<SPEED_t, CircleProgressView>) in.readSerializable();
        levelMap = (HashMap<SPEED_t, LEVEL_t>) in.readSerializable();
        speedMap = (HashMap<SPEED_t, Integer>) in.readSerializable();
    }

    private void init (Activity activity) {

        appColor = new AppColor(activity);

        viewMap = new HashMap<>();
        levelMap = new HashMap<>();
        speedMap = new HashMap<>();

        viewMap.put(SPEED_t.IN_CORNERS, ((CircleProgressView) activity.findViewById(R.id.speed1_view)));
        viewMap.put(SPEED_t.IN_STRAIGHT_LINE,((CircleProgressView) activity.findViewById(R.id.speed2_view)));
        viewMap.put(SPEED_t.MAX_LIMIT,((CircleProgressView) activity.findViewById(R.id.speed3_view)));

        for (CircleProgressView view : viewMap.values()) {

            view.setValue(100);
            view.setTextMode(TextMode.TEXT);
            view.setTextSize(54);
            view.setText("0");
        }

        viewMap.get(SPEED_t.MAX_LIMIT).setVisibility(View.GONE);
    }

    public void restore (Activity activity) {

        init(activity);

        setSpeed(SPEED_t.IN_CORNERS, levelMap.get(SPEED_t.IN_CORNERS), speedMap.get(SPEED_t.IN_CORNERS));
        setSpeed(SPEED_t.IN_STRAIGHT_LINE, levelMap.get(SPEED_t.IN_STRAIGHT_LINE), speedMap.get(SPEED_t.IN_STRAIGHT_LINE));
        setSpeed(SPEED_t.MAX_LIMIT, levelMap.get(SPEED_t.MAX_LIMIT), speedMap.get(SPEED_t.MAX_LIMIT));

        if (visible) {

            hide(false);
        }
        else {

            hide(true);
        }
    }

    public void hide (boolean hide) {

        for (CircleProgressView view : viewMap.values()) {

            if (!view.equals(viewMap.get(SPEED_t.MAX_LIMIT))) {

                if (hide) {

                    view.setVisibility(View.GONE);
                }
                else {

                    view.setVisibility(View.VISIBLE);
                }
            }
        }

        visible = !hide;
    }

    public void setSpeed (SPEED_t speedId, LEVEL_t level, Integer speed) {

        if (speed < 0) {

            speed = 0;
        }

        if (!visible) {

            hide(false);
        }

        switch (speedId) {

            case IN_CORNERS:
            case IN_STRAIGHT_LINE:
            case MAX_LIMIT:

                viewMap.get(speedId).setText(speed.toString());
                viewMap.get(speedId).setBarColor(appColor.getColor(level));

                break;

            default: break;
        }

        levelMap.put(speedId, level);
        speedMap.put(speedId, speed);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.viewMap);
        dest.writeSerializable(this.levelMap);
        dest.writeSerializable(this.speedMap);
    }

    public static final Parcelable.Creator<SpeedView> CREATOR = new Parcelable.Creator<SpeedView>() {

        @Override
        public SpeedView createFromParcel (Parcel source) {

            return new SpeedView(source);
        }

        @Override
        public SpeedView[] newArray (int size) {

            return new SpeedView[size];
        }
    };
}
