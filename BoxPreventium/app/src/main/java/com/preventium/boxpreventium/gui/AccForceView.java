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

public class AccForceView {

    private static final String TAG = "AccForceView";

    private ImageView accForceView;
    private AppColor appColor;
    private FORCE_t lastForce = FORCE_t.UNKNOW;
    private LEVEL_t lastLevel = LEVEL_t.LEVEL_UNKNOW;

    public AccForceView (Activity activity) {

        accForceView = (ImageView) activity.findViewById(R.id.acc_force_view);
        appColor = new AppColor(activity);

        hide(true);
    }

    public void hide (boolean hide) {

        if (hide) {

            accForceView.setVisibility(View.INVISIBLE);
        }
        else {

            accForceView.setVisibility(View.VISIBLE);
        }
    }

    public void disable (boolean disable) {

        Drawable drawable = accForceView.getBackground();

        if (disable) {

            drawable.setColorFilter(appColor.getColor(AppColor.GREY), PorterDuff.Mode.SRC_ATOP);
            accForceView.setImageResource(android.R.color.transparent);
            accForceView.setEnabled(false);
        }
        else {

            accForceView.setEnabled(true);
            setValue(lastForce, lastLevel);
        }

        accForceView.setBackground(drawable);
    }

    public void setValue (FORCE_t force, LEVEL_t level) {

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

        Drawable drawable = accForceView.getBackground();
        drawable.setColorFilter(appColor.getColor(level), PorterDuff.Mode.SRC_ATOP);
        accForceView.setBackground(drawable);
    }
}
