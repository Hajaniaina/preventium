package com.preventium.boxpreventium.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;

public class AccForceView extends Object {

    private static final String TAG = "AccForceView";

    private Context context;
    private ImageView accForceView;
    private int disableColor;
    private FORCE_t lastForce = FORCE_t.UNKNOW;
    private LEVEL_t lastLevel = LEVEL_t.LEVEL_UNKNOW;

    public AccForceView (Activity activity) {

        context = activity.getApplicationContext();
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

            accForceView.setEnabled(true);
            setAcc(lastForce, lastLevel);
        }

        accForceView.setBackground(drawable);
    }

    public void setAcc (FORCE_t force, LEVEL_t level) {

        lastForce = force;
        lastLevel = level;

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

        int color = 0;

        switch (level) {

            case LEVEL_UNKNOW:
                color = ContextCompat.getColor(context, R.color.colorAppGrey);
                break;

            case LEVEL_1:
                color = ContextCompat.getColor(context, R.color.colorAppGreen);
                break;

            case LEVEL_2:
                color = ContextCompat.getColor(context, R.color.colorAppBlue);
                break;

            case LEVEL_3:
                color = ContextCompat.getColor(context, R.color.colorAppYellow);
                break;

            case LEVEL_4:
                color = ContextCompat.getColor(context, R.color.colorAppOrange);
                break;

            case LEVEL_5:
                color = ContextCompat.getColor(context, R.color.colorAppRed);
                break;
        }

        Drawable drawable = accForceView.getBackground();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        accForceView.setBackground(drawable);
    }
}
