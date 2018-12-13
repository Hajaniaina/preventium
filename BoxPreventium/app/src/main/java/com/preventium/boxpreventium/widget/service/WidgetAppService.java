package com.preventium.boxpreventium.widget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.preventium.boxpreventium.R;
import com.preventium.boxpreventium.enums.FORCE_t;
import com.preventium.boxpreventium.enums.LEVEL_t;
import com.preventium.boxpreventium.gui.AppColor;
import com.preventium.boxpreventium.module.Load.LoadImage;
import com.preventium.boxpreventium.widget.AppWidget;
import com.preventium.boxpreventium.widget.Widget;

/**
 * Created by tog on 31/10/2018.
 */

public class WidgetAppService extends Service {

    private Handler handler = new Handler();
    /* task */
    private class Task implements Runnable {
        private int index = 0;
        private Context context;
        public Task (Context context) {
            this.context = context;
        }

        public int Color (final LEVEL_t color) {
            int _return = R.drawable.text_border_gray;

            switch(color) {
                case LEVEL_5:
                    _return = R.drawable.text_border_red;
                    break;
                case LEVEL_4 :
                    _return = R.drawable.text_border_orange;
                    break;
                case LEVEL_3 :
                    _return = R.drawable.text_border_yellow;
                    break;
                case LEVEL_2 :
                    _return = R.drawable.text_border_blue;
                    break;
                case LEVEL_1 :
                    _return = R.drawable.text_border_green;
                    break;
                default:
                    _return = R.drawable.text_border_gray;
                    break;
            }
            return _return;
        }

        public Drawable Force (final FORCE_t force) {
            Drawable drawables = null;
            switch (force) {
                case UNKNOW:
                    drawables = null;
                    break;
                case TURN_LEFT:
                    drawables = context.getResources().getDrawable(R.drawable.ic_arrow_left);
                    break;
                case TURN_RIGHT:
                    drawables = context.getResources().getDrawable(R.drawable.ic_arrow_right);
                    break;
                case ACCELERATION:
                    drawables = context.getResources().getDrawable(R.drawable.ic_arrow_up);
                    break;
                case BRAKING:
                    drawables = context.getResources().getDrawable(R.drawable.ic_arrow_down);
                    break;
            }
            return drawables;
        }

        @Override
        public void run() {
            // widget
            AppColor appColor = new AppColor(context);
            Widget widget = Widget.get();
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
            views.setInt(R.id.parent, "setBackgroundColor", (int)(0.4 * 0xFF) << 24);

        /* pour note */
            LEVEL_t color = widget.getNoteColor();
            LEVEL_t color_avg = widget.getNoteColorAVG();
            int note = widget.getNote();

            Log.w("Widget !!", "note widget: " + String.valueOf(note));

            views.setInt(R.id.driving_score_view, "setBackgroundResource", Color(color_avg));
            views.setTextColor(R.id.driving_score_view, appColor.getColor(color));
            views.setTextViewText(R.id.driving_score_view, String.valueOf(note));
        /* end note */

        /* pour force */
            LEVEL_t colorForce = widget.getForceColor();
            FORCE_t force = widget.getForce();
            Drawable drawables = Force(force);

            if( drawables != null ) {
                drawables.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                views.setImageViewBitmap(R.id.acc_force_view, LoadImage.drawableToBitmap(drawables));
                views.setInt(R.id.acc_force_view, "setBackgroundColor", appColor.getColor(colorForce));
            }

            /* AppWidgetManager */
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            ComponentName componentName = new ComponentName(getApplicationContext(), AppWidget.class);
            appWidgetManager.updateAppWidget(componentName, views);
            /* end AppWidgetManager */

            // Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG).show();
            handler.postDelayed(this, 500);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler.post(new Task(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
