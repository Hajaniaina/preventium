package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.Toast;

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

    private Context context;

    SpeedView (Activity activity) {

        init(activity);
        levelMap.put(SPEED_t.IN_STRAIGHT_LINE, null);
        levelMap.put(SPEED_t.IN_CORNERS, null);
        levelMap.put(SPEED_t.MAX_LIMIT, null);

        speedMap.put(SPEED_t.IN_STRAIGHT_LINE, 0);
        speedMap.put(SPEED_t.IN_CORNERS, 0);
        speedMap.put(SPEED_t.MAX_LIMIT, 0);

        viewMap.get(SPEED_t.MAX_LIMIT).setVisibility(View.GONE);
    }
    public  SpeedView(Context ctx,int iq){
        context = ctx;
    }

    protected SpeedView (Parcel in) {

        visible = in.readByte() != 0;
        viewMap = (HashMap<SPEED_t, CircleProgressView>) in.readSerializable();
        levelMap = (HashMap<SPEED_t, LEVEL_t>) in.readSerializable();
        speedMap = (HashMap<SPEED_t, Integer>) in.readSerializable();
    }

    private void init (Activity activity) {

        appColor = new AppColor(activity.getApplicationContext());

        viewMap = new HashMap<>();
        levelMap = new HashMap<>();
        speedMap = new HashMap<>();

        viewMap.put(SPEED_t.IN_CORNERS, ((CircleProgressView) activity.findViewById(R.id.speed1_view)));
        viewMap.put(SPEED_t.IN_STRAIGHT_LINE,((CircleProgressView) activity.findViewById(R.id.speed2_view)));
        viewMap.put(SPEED_t.MAX_LIMIT,((CircleProgressView) activity.findViewById(R.id.speed3_view)));

        for (CircleProgressView view : viewMap.values()) {

            view.setValue(100);
            view.setTextMode(TextMode.TEXT);
            view.setText("0");

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick (View v) {

                    CircleProgressView tempView = (CircleProgressView) v;

                    if (tempView.isUnitVisible()) {

                        tempView.setUnitVisible(false);
                    }
                    else {

                        tempView.setUnitVisible(true);
                    }
                }
            });
        }

    }

    public void restore (Activity activity) {

        init(activity);

        setSpeed(SPEED_t.IN_CORNERS, levelMap.get(SPEED_t.IN_CORNERS), speedMap.get(SPEED_t.IN_CORNERS), true);
        setSpeed(SPEED_t.IN_STRAIGHT_LINE, levelMap.get(SPEED_t.IN_STRAIGHT_LINE), speedMap.get(SPEED_t.IN_STRAIGHT_LINE), true);
        setSpeed(SPEED_t.MAX_LIMIT, levelMap.get(SPEED_t.MAX_LIMIT), speedMap.get(SPEED_t.MAX_LIMIT), true);

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

                    view.setVisibility(View.INVISIBLE);
                }
                else {

                    view.setVisibility(View.VISIBLE);
                }
            }
        }

        visible = !hide;
    }

    public void setSpeed (SPEED_t speedId, LEVEL_t level, Integer speed, boolean speedValid) {

        if (speed < 0) {

            speed = 0;
        }

        if (!visible) {
            // hide(false);
        }

        Toast.makeText(PreventiumApplication.getContext(), "Vitesse unconnu" + String.valueOf(speedId), Toast.LENGTH_LONG);

        switch (speedId) {

            case IN_CORNERS:
            case IN_STRAIGHT_LINE:
            case MAX_LIMIT:

                viewMap.get(speedId).setText(speed.toString());
                viewMap.get(speedId).setBarColor(appColor.getColor(level));

                if (speedValid) {

                    viewMap.get(speedId).setTextColor(Color.BLACK);
                }
                else {

                    viewMap.get(speedId).setTextColor(appColor.getColor(AppColor.RED));
                }

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

        dest.writeByte(visible ? (byte) 1 : (byte) 0);
        dest.writeSerializable(viewMap);
        dest.writeSerializable(levelMap);
        dest.writeSerializable(speedMap);
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
