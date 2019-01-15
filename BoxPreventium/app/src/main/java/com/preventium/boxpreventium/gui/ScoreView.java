package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.enums.SCORE_t;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ScoreView implements Parcelable {

    private static final String TAG = "ScoreView";

    private AppColor appColor;
    private boolean visible = true;
    private HashMap<SCORE_t, LEVEL_t> levelMap;

    private WeakReference<Activity> activityWeakReference;

    ScoreView (Activity activity) {
        activityWeakReference = new WeakReference<Activity>(activity);
        initialize();
    }

    public void initialize()
    {
        Activity activity = activityWeakReference.get();
        if( activity != null ) {
            appColor = new AppColor( PreventiumApplication.getContext() );
            levelMap = new HashMap<>();

            final Context context = activity.getApplicationContext();
            final TextView driverScoreView = getById(SCORE_t.FINAL);
            if( driverScoreView != null ) {
                driverScoreView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String text = context.getString(R.string.driver_score_tooltip_string);

                        ViewTooltip.on(driverScoreView)
                                .autoHide(true, 5000)
                                .corner(10)
                                .position(ViewTooltip.Position.LEFT)
                                .text(text)
                                .show();
                    }
                });
            }

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
    }

    protected ScoreView (Parcel in) {
        visible = in.readByte() != 0;
        levelMap = (HashMap<SCORE_t, LEVEL_t>) in.readSerializable();
    }

    protected TextView getById(SCORE_t id) {
        Activity activity = activityWeakReference.get();
        if( activity != null )
        {
            if( id == SCORE_t.CORNERING )
                return ((TextView) activity.findViewById(R.id.corner_note_view));
            if( id == SCORE_t.BRAKING )
                return ((TextView) activity.findViewById(R.id.brake_note_view));
            if( id == SCORE_t.ACCELERATING )
                return ((TextView) activity.findViewById(R.id.acc_note_view));
            if( id == SCORE_t.AVERAGE )
                return ((TextView) activity.findViewById(R.id.avg_note_view));
            if( id == SCORE_t.FINAL )
                return ((TextView) activity.findViewById(R.id.driving_score_view));
        }
        return null;
    }

    public void setScore (SCORE_t scoreId, LEVEL_t level) {

        MainActivity main = (MainActivity) activityWeakReference.get();
        if( main != null ) {

            switch (scoreId) {

                case CORNERING:
                    if( getById(scoreId) != null )
                    {
                        Drawable drawable = getById(scoreId).getBackground();
                        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
                        getById(scoreId).setBackground(drawable);
                        main.color_v = appColor.getColorCode(level);
                    }
                    break;

                case BRAKING:
                    if( getById(scoreId) != null )
                    {
                        Drawable drawable = getById(scoreId).getBackground();
                        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
                        getById(scoreId).setBackground(drawable);
                        main.color_f = appColor.getColorCode(level);
                    }
                    break;

                case ACCELERATING:
                    if( getById(scoreId) != null )
                    {
                        Drawable drawable = getById(scoreId).getBackground();
                        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
                        getById(scoreId).setBackground(drawable);
                        main.color_a = appColor.getColorCode(level);
                    }
                    break;

                case AVERAGE:
                    if( getById(scoreId) != null )
                    {
                        Drawable drawable = getById(scoreId).getBackground();
                        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
                        getById(scoreId).setBackground(drawable);
                        main.color_m = appColor.getColorCode(level);
                    }
                    break;

                default:
                    break;
            }

            levelMap.put(scoreId, level);
        }
    }

    public void setFinalScore (LEVEL_t level, LEVEL_t levelAvg, int value) {

        if (value < 0) value = 0;
        if (value > 20) value = 20;

        TextView score_final = getById(SCORE_t.FINAL);
        if( score_final != null ) {
            score_final.setText(String.valueOf(value));
            score_final.setTextColor(appColor.getColor(level));

            Drawable drawable = score_final.getBackground();
            drawable.setColorFilter(appColor.getColor(levelAvg), PorterDuff.Mode.SRC_ATOP);
            score_final.setBackground(drawable);
        }
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
