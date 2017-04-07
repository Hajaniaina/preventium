package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;

import java.util.HashMap;

public class ScoreView implements Parcelable {

    private static final String TAG = "ScoreView";

    private AppColor appColor;
    private boolean visible = true;
    private HashMap<SCORE_t, TextView> viewMap;
    private HashMap<SCORE_t, LEVEL_t> levelMap;

    ScoreView (Activity activity) {

        appColor = new AppColor(activity.getApplicationContext());

        viewMap = new HashMap<>();
        levelMap = new HashMap<>();

        viewMap.put(SCORE_t.CORNERING, ((TextView) activity.findViewById(R.id.corner_note_view)));
        viewMap.put(SCORE_t.BRAKING, ((TextView) activity.findViewById(R.id.brake_note_view)));
        viewMap.put(SCORE_t.ACCELERATING,((TextView) activity.findViewById(R.id.acc_note_view)));
        viewMap.put(SCORE_t.AVERAGE, ((TextView) activity.findViewById(R.id.avg_note_view)));
        viewMap.put(SCORE_t.FINAL, ((TextView) activity.findViewById(R.id.driving_score_view)));

        levelMap.put(SCORE_t.CORNERING, null);
        levelMap.put(SCORE_t.BRAKING, null);
        levelMap.put(SCORE_t.ACCELERATING, null);
        levelMap.put(SCORE_t.AVERAGE, null);
        levelMap.put(SCORE_t.FINAL, null);

        setScore(SCORE_t.CORNERING, LEVEL_t.LEVEL_UNKNOW);
        setScore(SCORE_t.BRAKING, LEVEL_t.LEVEL_UNKNOW);
        setScore(SCORE_t.ACCELERATING, LEVEL_t.LEVEL_UNKNOW);
        setScore(SCORE_t.AVERAGE, LEVEL_t.LEVEL_UNKNOW);
        setFinalScore(LEVEL_t.LEVEL_UNKNOW, LEVEL_t.LEVEL_UNKNOW, 0);
    }

    protected ScoreView (Parcel in) {

        visible = in.readByte() != 0;
        levelMap = (HashMap<SCORE_t, LEVEL_t>) in.readSerializable();
    }

    public void restore (Activity activity) {

        appColor = new AppColor(activity);
        viewMap = new HashMap<>();
        levelMap = new HashMap<>();

        viewMap.put(SCORE_t.CORNERING, ((TextView) activity.findViewById(R.id.corner_note_view)));
        viewMap.put(SCORE_t.BRAKING, ((TextView) activity.findViewById(R.id.brake_note_view)));
        viewMap.put(SCORE_t.ACCELERATING, ((TextView) activity.findViewById(R.id.acc_note_view)));
        viewMap.put(SCORE_t.AVERAGE, ((TextView) activity.findViewById(R.id.avg_note_view)));
        viewMap.put(SCORE_t.FINAL, ((TextView) activity.findViewById(R.id.driving_score_view)));

        setScore(SCORE_t.CORNERING, levelMap.get(SCORE_t.CORNERING));
        setScore(SCORE_t.BRAKING, levelMap.get(SCORE_t.BRAKING));
        setScore(SCORE_t.ACCELERATING, levelMap.get(SCORE_t.ACCELERATING));
        setScore(SCORE_t.AVERAGE, levelMap.get(SCORE_t.AVERAGE));
        setFinalScore(levelMap.get(SCORE_t.FINAL), LEVEL_t.LEVEL_UNKNOW, 0);

        if (visible) {

            hide(false);
        }
        else {

            hide(true);
        }
    }

    public void hide (boolean hide) {

        for (TextView view : viewMap.values()) {

            if (hide) {

                view.setVisibility(View.INVISIBLE);
            }
            else {

                view.setVisibility(View.VISIBLE);
            }
        }

        visible = !hide;
    }

    public void setScore (SCORE_t scoreId, LEVEL_t level) {

        switch (scoreId) {

            case CORNERING:
            case BRAKING:
            case ACCELERATING:
            case AVERAGE:

                Drawable drawable = viewMap.get(scoreId).getBackground();
                drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
                viewMap.get(scoreId).setBackground(drawable);

                break;

            default: break;
        }

        levelMap.put(scoreId, level);
    }

    public void setFinalScore (LEVEL_t level, LEVEL_t levelAvg, int value) {

        if (value < 0) value = 0;
        if (value > 20) value = 20;

        viewMap.get(SCORE_t.FINAL).setText(String.valueOf(value));
        viewMap.get(SCORE_t.FINAL).setTextColor(appColor.getColor(level));

        Drawable drawable = viewMap.get(SCORE_t.FINAL).getBackground();
        drawable.setColorFilter(appColor.getColor(levelAvg), PorterDuff.Mode.SRC_ATOP);
        viewMap.get(SCORE_t.FINAL).setBackground(drawable);
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest, int flags) {

        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.levelMap);
    }

    public static final Parcelable.Creator<ScoreView> CREATOR = new Parcelable.Creator<ScoreView>() {

        @Override
        public ScoreView createFromParcel (Parcel source) {

            return new ScoreView(source);
        }

        @Override
        public ScoreView[] newArray (int size) {

            return new ScoreView[size];
        }
    };
}
