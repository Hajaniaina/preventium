package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.LEVEL_t;

public class AppColor extends Object {

    public static final int RED    = 0;
    public static final int ORANGE = 1;
    public static final int YELLOW = 2;
    public static final int BLUE   = 3;
    public static final int GREEN  = 4;
    public static final int VIOLET = 5;
    public static final int GREY   = 6;

    private int[] appColors;

    public AppColor (Activity activity) {

        Context context = activity.getApplicationContext();

        appColors = new int[7];

        appColors[RED]    = ContextCompat.getColor(context, R.color.colorAppRed);
        appColors[ORANGE] = ContextCompat.getColor(context, R.color.colorAppOrange);
        appColors[YELLOW] = ContextCompat.getColor(context, R.color.colorAppYellow);
        appColors[BLUE]   = ContextCompat.getColor(context, R.color.colorAppBlue);
        appColors[GREEN]  = ContextCompat.getColor(context, R.color.colorAppGreen);
        appColors[VIOLET] = ContextCompat.getColor(context, R.color.colorAppViolet);
        appColors[GREY]   = ContextCompat.getColor(context, R.color.colorAppGrey);
    }

    public int getColor (LEVEL_t level) {

        int color = -1;

        switch (level) {

            case LEVEL_UNKNOW: color = appColors[GREY];
                break;

            case LEVEL_1: color = appColors[RED];
                break;

            case LEVEL_2: color = appColors[ORANGE];
                break;

            case LEVEL_3: color = appColors[YELLOW];
                break;

            case LEVEL_4: color = appColors[BLUE];
                break;

            case LEVEL_5: color = appColors[GREEN];
                break;
        }

        return color;
    }

    public int getColor (int id) {

        if (id < RED || id > GREY) {

            return -1;
        }

        return appColors[id];
    }
}
