package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.preventium.boxpreventium.R;

import az.plainpie.PieView;

public class SpeedViewManager {

    private static final String TAG = "SpeedViewManager";

    public static final int SPEED_CORNERS  = 0;
    public static final int SPEED_STRAIGHT = 1;
    public static final int SPEED_MAX      = 2;

    private PieView[] speedViewArr;
    private int[] lastColor;
    private int[] lastSpeed;
    private int disableColor;

    SpeedViewManager (Activity activity) {

        Context context = activity.getApplicationContext();

        speedViewArr = new PieView[3];
        lastColor = new int[3];
        lastSpeed = new int[3];

        disableColor = ContextCompat.getColor(context, R.color.colorAppGrey);

        speedViewArr[0] = (PieView) activity.findViewById(R.id.pie_view1);
        speedViewArr[1] = (PieView) activity.findViewById(R.id.pie_view2);
        speedViewArr[2] = (PieView) activity.findViewById(R.id.pie_view3);

        for (int i = 0; i < speedViewArr.length; i++) {

            speedViewArr[i].setPercentageBackgroundColor(ContextCompat.getColor(context, R.color.colorAppGreen));
            // speedViewArr[i].setInnerBackgroundColor(ContextCompat.getColor(context, R.color.colorAppRed));
            // speedViewArr[i].setTextColor(Color.BLACK);

            setSpeed(i, 0);
        }
    }

    public void hide (boolean hide) {

        for (int i = 0; i < speedViewArr.length; i++) {

            if (hide) {

                speedViewArr[i].setVisibility(View.GONE);
            }
            else {

                speedViewArr[i].setVisibility(View.VISIBLE);
            }
        }
    }

    public void disable (boolean disable) {

        for (int i = 0; i < speedViewArr.length; i++) {

            if (disable) {

                speedViewArr[i].setInnerBackgroundColor(disableColor);
                speedViewArr[i].setMainBackgroundColor(disableColor);
                speedViewArr[i].setPercentageBackgroundColor(disableColor);

                setSpeed(i, 0);
                speedViewArr[i].setEnabled(false);
            }
            else {

                speedViewArr[i].setEnabled(true);
                setSpeed(i, lastSpeed[i]);
            }
        }
    }

    public void setSpeed (int id, int speed) {

        if (id > SPEED_MAX) {

            id = SPEED_MAX;
        }

        if (speed < 0) {

            speed = 0;
        }

        lastSpeed[id] = speed;
        speedViewArr[id].setInnerText(String.valueOf(speed));
    }
}
