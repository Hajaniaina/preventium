package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.preventium.boxpreventium.R;

public class ScoreViewManager extends Object {

    private static final String TAG = "ScoreViewManager";

    public static final int CORNER = 0;
    public static final int BRAKE  = 1;
    public static final int ACC    = 2;
    public static final int AVG    = 3;

    private ImageView[] scoreViewArr;
    private int[] colorScoreArr;
    private int[] scoreArr;
    private int disableColor;

    ScoreViewManager (Activity activity) {

        Context context = activity.getApplicationContext();

        scoreArr = new int[5];
        colorScoreArr = new int[5];

        colorScoreArr[0] = ContextCompat.getColor(context, R.color.colorAppRed);
        colorScoreArr[1] = ContextCompat.getColor(context, R.color.colorAppOrange);
        colorScoreArr[2] = ContextCompat.getColor(context, R.color.colorAppYellow);
        colorScoreArr[3] = ContextCompat.getColor(context, R.color.colorAppBlue);
        colorScoreArr[4] = ContextCompat.getColor(context, R.color.colorAppGreen);
        disableColor = ContextCompat.getColor(context, R.color.colorAppGrey);

        scoreViewArr = new ImageView[4];

        scoreViewArr[0] = (ImageView) activity.findViewById(R.id.corner_note_view);
        scoreViewArr[1] = (ImageView) activity.findViewById(R.id.brake_note_view);
        scoreViewArr[2] = (ImageView) activity.findViewById(R.id.acc_note_view);
        scoreViewArr[3] = (ImageView) activity.findViewById(R.id.avg_note_view);

        setScore(ScoreViewManager.CORNER, 4);
        setScore(ScoreViewManager.BRAKE, 4);
        setScore(ScoreViewManager.ACC, 4);
        setScore(ScoreViewManager.AVG, 4);
    }

    public void hide (boolean hide) {

        for (int i = 0; i < scoreViewArr.length; i++) {

            if (hide) {

                scoreViewArr[i].setVisibility(View.GONE);
            }
            else {

                scoreViewArr[i].setVisibility(View.VISIBLE);
            }
        }
    }

    public void disable (boolean disable) {

        for (int i = 0; i < scoreViewArr.length; i++) {

            if (disable) {

                Drawable drawable = scoreViewArr[i].getBackground();
                drawable.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);

                scoreViewArr[i].setBackground(drawable);
                scoreViewArr[i].setEnabled(false);
            }
            else {

                scoreViewArr[i].setEnabled(true);
                setScore(i, scoreArr[i]);
            }
        }
    }

    public void setScore (int id, int score) {

        if (id > AVG || id < 0) {

            id = AVG;
        }

        if (score > 4 || score < 0) {

            score = 4;
        }

        scoreArr[id] = score;

        Drawable drawable = scoreViewArr[id].getBackground();
        drawable.setColorFilter(colorScoreArr[score], PorterDuff.Mode.SRC_ATOP);
        scoreViewArr[id].setBackground(drawable);
    }
}
