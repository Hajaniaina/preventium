package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.preventium.boxpreventium.R;

public class AccForceView extends Object {

    private static final String TAG = "AccForceView";

    public static final int ACC_UP    = 0;
    public static final int ACC_DOWN  = 1;
    public static final int ACC_LEFT  = 2;
    public static final int ACC_RIGHT = 3;

    private Context context;
    private ImageView accForceView;
    private int[] accIcons;
    private int disableColor;
    int lastIcon, lastColor;

    public AccForceView (Activity activity) {

        context = activity.getApplicationContext();

        accIcons = new int[4];
        accIcons[0] = context.getResources().getIdentifier("ic_arrow_up" , "drawable", context.getPackageName());
        accIcons[1] = context.getResources().getIdentifier("ic_arrow_down" , "drawable", context.getPackageName());
        accIcons[2] = context.getResources().getIdentifier("ic_arrow_left" , "drawable", context.getPackageName());
        accIcons[3] = context.getResources().getIdentifier("ic_arrow_right" , "drawable", context.getPackageName());

        accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        disableColor = ContextCompat.getColor(context, R.color.colorAppGrey);

        hide(true);
    }

    public void hide (boolean hide) {

        if (hide) {

            accForceView.setVisibility(View.GONE);
        }
        else {

            accForceView.setVisibility(View.VISIBLE);
        }
    }

    public void disable (boolean disable) {

        Drawable drawable = accForceView.getBackground();

        if (disable) {

            drawable.setColorFilter(disableColor, PorterDuff.Mode.SRC_ATOP);
            accForceView.setImageResource(android.R.color.transparent);
            accForceView.setEnabled(false);
        }
        else {

            drawable.setColorFilter(lastColor, PorterDuff.Mode.SRC_ATOP);
            accForceView.setImageResource(lastIcon);
            accForceView.setEnabled(true);
        }

        accForceView.setBackground(drawable);
    }

    public void setAcc (int id, int value) {

        if (id > ACC_RIGHT) {

            id = ACC_RIGHT;
        }

        accForceView.setImageResource(accIcons[id]);

        lastIcon = accIcons[id];
        lastColor = ContextCompat.getColor(context, R.color.colorAppGreen);
    }
}
